/*
 * Copyright 2007 Google Inc.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.shell.profiler;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.URI;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * An HTTP proxy server that is used by the Profiler subsystem to capture ALL
 * HTTP traffic generated by hosted browsers. This includes "regular" http
 * traffic (HTML files, JavaScript, images - general resources), and RPC
 * traffic such as GWT RPC, RequestBuilder, JSON, etc...
 *
 */
public class ProxyServer {

  // For information on the HTTP spec and proxy requirements, see RFC 2616
  // http://www.faqs.org/rfcs/rfc2616.html

  // TODO(tobyr): Use NIO instead of java.io for a more optimal implementation

  /**
   * A Header and all of its values.
   */
  private static class Header {
    String key;
    ArrayList values;
    Header(String key) {
      this.key = key;
      this.values = new ArrayList();
    }
    Header(String key,String value) {
      this(key);
      values.add(value);
    }
    public String toString() {
      return key + " -> " + values;
    }
  }

  /**
   * A LinkedHashMap<String,Header> that contains the lower-cased header key
   * for easy lookups and the originally-cased header key within the Header
   * value. Not normally fond of subclassing containers, but it seems
   * appropriate here, in this private implementation.
   */
  private static class Headers extends LinkedHashMap {

    /**
     * Returns the set of headers from an HTTP request. Incoming order is
     * retained.
     *
     * @param reader The reader that is positioned just before the header body
     *
     * @return not-null headers
     *
     * @throws IOException if an error occurs while reading from reader
     */
    public static Headers parse(BufferedReader reader) throws IOException {
      // Should be guaranteed at least 1 header.
      Headers headers = new Headers();
      while ( true ) {
        String headerLine = reader.readLine();
        if ( headerLine.trim().equals( "" ) ) {
          return headers;
        }
        int keyEndIndex = headerLine.indexOf( ':' );
        if ( keyEndIndex == -1 ) {
          System.err.println( "ProxyServer: Expected a ':' in header: " + headerLine );
          continue;
        }
        String key = headerLine.substring(0, keyEndIndex);
        String value = headerLine.substring(keyEndIndex + 1).trim();
        headers.add(key, value);
      }
    }

    public void add(String key, String value) {
      Header header = (Header) get(key.toLowerCase());
      if (header == null) {
        header = new Header(key);
        put(key.toLowerCase(),header);
      }
      header.values.add(value);
    }

    public String getFirst(String key) {
      Header header = (Header) get(key.toLowerCase());
      if ( header == null ) {
        return null;
      }
      return (String) header.values.get(0);
    }

    public Object remove( Object key ) {
      return super.remove( ((String) key).toLowerCase() );
    }

    public void write( BufferedWriter writer ) throws IOException {
      for ( Iterator it = values().iterator(); it.hasNext();) {
        Header header = (Header) it.next();
        for (int i = 0; i < header.values.size(); ++i) {
          writer.write(header.key + ": " + header.values.get(i) + "\r\n");
        }
      }
      writer.write("\r\n");
    }
  }

  class ClientConnection extends ProxyConnection {

    private ProxyConnection server;
    private URI request;
    private int requestId;

    public ClientConnection(Socket fromSocket, Socket toSocket, URI request, int requestId, ProxyConnection server) {
      super(fromSocket, toSocket);
      this.request = request;
      this.requestId = requestId;
      this.server = server;
    }

    public void run() {
      super.run();

      if ( listener != null ) {
        try {
          server.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          int bytesFromServer = server.numBytesTransferred;
          listener.requestFinished(request, requestId, numBytesTransferred, bytesFromServer, error != null);
        }
      }
    }
  }

  private class ProxyConnection extends Thread {
    volatile int numBytesTransferred;
    Throwable error = null;

    private Socket fromSocket;
    private Socket toSocket;

    public ProxyConnection(Socket fromSocket, Socket toSocket) {
      this.fromSocket = fromSocket;
      this.toSocket = toSocket;
    }

    public void run() {
      try {
        InputStream fromSideInput = fromSocket.getInputStream();
        OutputStream toSideOutput = toSocket.getOutputStream();

        // Spin and pass data in one direction.
        int avail;
        byte[] bytes = new byte[32768];
        while (true) {
          // Read 'from' side
          avail = fromSideInput.read(bytes);
          if (avail > 0) {
            // Forward to 'to' side
            toSideOutput.write(bytes, 0, avail);
            numBytesTransferred += avail;
          } else if (avail == -1) {
            break;
          }
        }
      } catch (Throwable e) {
        error = e;
      } finally {
        try {
          fromSocket.close();
          toSocket.close();
        } catch (Throwable e) {
        }
      }
    }
  }

  // Perhaps the filters should be installable by the profiler?
  // But then how do our filters stack with the profilers' filters?
  /*
  public static class HTTPRequest {
    private int requestId;
    private URI request;
    private String contentType;
    private int numRequestBytes;
    private int numResponseBytes;
    private boolean failed;
  }

  public static class GWTRPCRequest extends HTTPRequest {

  }
  */

  /**
   * Listens in on requests that the proxy receives and executes.
   *
   */
  public interface RequestListener {

    /**
     * A request was finished.
     *
     * @param request A request which was previously started.
     * @param requestId A unique id representing the request.
     * @param numRequestBytes The total number of bytes transferred to the server.
     * @param numResponseBytes The total number of bytes transferred to the client.
     * @param failed true if the request failed due to an unexpected exception.
     */
    public void requestFinished(URI request, int requestId, int numRequestBytes, int numResponseBytes, boolean failed);

    /**
     * A request was started.
     *
     * @param request The fully-qualified, non-null request.
     * @param requestId A unique id representing the request.
     */
    public void requestStarted(URI request, int requestId);
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: ProxyServer <local-port>");
      return;
    }

    int localPort = Integer.parseInt(args[0]);
    ProxyServer proxy = new ProxyServer(localPort);
    proxy.run();
  }

  private int fromPort;
  private boolean end;

  private int connectionId;

  private RequestListener listener;

  public ProxyServer(int fromPort) {
    this.fromPort = fromPort;
  }

  public void end() {
    end = true;
  }

  public void run() {
    try {
      ServerSocket serverSocket = new ServerSocket(fromPort);
      while (!end) {
        try {
          Socket clientSideSocket = serverSocket.accept();
          clientSideSocket.setTcpNoDelay(true);
          ++connectionId;

          BufferedReader reader = new BufferedReader(new InputStreamReader(clientSideSocket.getInputStream()));

          String requestLine = null;

          // Read the request-line to see where our destination server is
          // See Section 5.1 of the RFC
          while ( true ) {
            requestLine = reader.readLine();
            if ( ! requestLine.trim().equals( "" ) ) {
              break;
            }
          }

          String[] requestParts = requestLine.split( " " );
          String method = requestParts[ 0 ];
          String requestURI = requestParts[ 1 ];
          String httpVersion = requestParts[ 2 ];

          Headers headers = Headers.parse( reader );

          /*
           We're not guaranteed that the URI is a full URL containing the
           destination host, because our hosted browsers aren't all sending
           us full URLs. (Eliding discussion about HTTP 1.1 requirements
           about full URLs in the request line and proxy servers).

           We pull the destination host out of the request header,
           because it should be guaranteed to be there.
          */
          String hostHeader = headers.getFirst( "host" );
          String host = hostHeader;
          String[] parts = hostHeader.split(":");
          int port = 80;
          if ( parts.length == 2 ) {
            host = parts[ 0 ];
            port = Integer.parseInt( parts[ 1 ] );
          }

          // Make sure the URI is an absolute URI to the correct server.
          URI uri = new URI(requestURI);

          if ( uri.getHost() == null ) {
            uri = uri.resolve( new URI( "http://" + hostHeader ) );
          } else if ( uri.getHost().equals("localhost") && port == fromPort) {
            // This can happen in the Safari case, where we've hooked the
            // request handler instead of configuring the proxy. In that case
            // it can end up using an absolute URL pointing to us instead
            // of the destination host.
            uri = new URI(uri.getScheme(), uri.getUserInfo(), host, port, uri.getPath(), uri.getQuery(), uri.getFragment());
          }

          if ( listener != null ) {
            listener.requestStarted(uri, connectionId);
          }

          /* We're not going to support persistent connections this rev,
           * because it simplifies several issues. For now, we'll just tell
           * the server no persistent connection.
           */
          headers.add( "Connection", "close" );
          // Non-standard version of Connection header, found in old Moz
          headers.remove( "proxy-connection" );

          Socket serverSideSocket = new Socket(host, port);
          serverSideSocket.setTcpNoDelay(true);

          /* -- Debug code
          System.out.println( "Headers: ");
          BufferedWriter outWriter = new BufferedWriter( new OutputStreamWriter( System.out ) );
          headers.write( outWriter );
          outWriter.flush();
          */

          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverSideSocket.getOutputStream()));
          writer.write(method + " " + uri + " " +  httpVersion + "\r\n");
          headers.write(writer);
          writer.flush();

          ProxyConnection serverCon = new ProxyConnection(serverSideSocket, clientSideSocket);
          ProxyConnection clientCon = new ClientConnection(clientSideSocket, serverSideSocket, uri, connectionId, serverCon);

          clientCon.start();
          serverCon.start();
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    } catch (Throwable e) {
      // Failed to even be able to listen.
      e.printStackTrace();
    }
  }

  public void setListener( RequestListener listener ) {
    this.listener = listener;
  }
}

