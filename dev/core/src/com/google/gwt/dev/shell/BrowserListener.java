/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.dev.shell;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.BrowserChannel.SessionHandler;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Listens for connections from OOPHM clients.
 */
public class BrowserListener {

  private ServerSocket listenSocket;

  private Thread listenThread;

  /**
   * Listens for new connections from browsers.
   */
  public BrowserListener(final TreeLogger logger, int port,
      final SessionHandler handler) {
    try {
      listenSocket = new ServerSocket(port);
      logger.log(TreeLogger.INFO, "Listening at: "
          + listenSocket.getLocalSocketAddress(), null);
      listenThread = new Thread() {
        @Override
        public void run() {
          while (true) {
            try {
              Socket sock = listenSocket.accept();
              TreeLogger branch = logger.branch(TreeLogger.INFO,
                  "Connection received from "
                      + sock.getInetAddress().getCanonicalHostName() + ":"
                      + sock.getPort());
              sock.setTcpNoDelay(true);
              BrowserChannelServer server = new BrowserChannelServer(branch,
                  sock, handler);
              /*
               * This object is special-cased by the SessionHandler, used for
               * methods needed by the client like hasMethod/hasProperty/etc.
               * handler is used for this object just to make sure it doesn't
               * conflict with some real object exposed to the client.
               */
              int id = server.getJavaObjectsExposedInBrowser().add(server);
              assert id == BrowserChannel.SPECIAL_SERVERMETHODS_OBJECT;
            } catch (IOException e) {
              logger.log(TreeLogger.ERROR, "Communications error", e);
            }
          }
        }
      };
      listenThread.setName("Hosted mode listener");
      listenThread.setDaemon(true);
    } catch (BindException e) {
      logger.log(TreeLogger.ERROR, "Unable to bind socket on port " + port
          + " -- is another session active?", e);
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Communications error", e);
    }
  }

  public String getEndpointIdentifier() {
    try {
      return InetAddress.getLocalHost().getHostAddress() + ":"
          + listenSocket.getLocalPort();
    } catch (UnknownHostException e) {
      throw new RuntimeException("Unable to determine my ip", e);
    }
  }

  public void start() {
    listenThread.start();
  }
}
