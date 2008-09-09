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

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cerrno>

#include "Debug.h"

#ifdef _WINDOWS
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/time.h>
#endif
#include <time.h>

#include "Platform.h"

#include "FreeValueMessage.h"
#include "HostChannel.h"
#include "LoadJsniMessage.h"
#include "InvokeMessage.h"
#include "InvokeSpecialMessage.h"
#include "QuitMessage.h"
#include "ReturnMessage.h"
#include "Value.h"
#include "scoped_ptr/scoped_ptr.h"

using namespace std;

bool HostChannel::connectToHost(char* host, unsigned port) {
  Debug::log(Debug::Info) << "HostChannel::connectToHost(host=" << host << ",port=" << port
      << ")" << Debug::flush;
  if (!port) {
    port = 9997;
  }
  if (!whitelist.isAllowed(host, port)) {
    Debug::log(Debug::Error) << "Permission to connect to " << host << ":" << port
        << " denied" << Debug::flush;
    return false;
  }
  return sock.connect(host, port);
}

bool HostChannel::disconnectFromHost() {
  Debug::log(Debug::Info) << "Disconnecting channel" << Debug::flush;
  if (!isConnected()) {
    Debug::log(Debug::Error) << "Disconnecting already disconnected channel" << Debug::flush;
    return false;
  }
  QuitMessage::send(*this);
  flush();
  sock.disconnect();
  return true;
}

bool HostChannel::readInt(int32_t& data) {
  int32_t d;
  if (!readBytes(&d, sizeof(d))) return false;
  data = ntohl(d);
  return true;
}

bool HostChannel::sendInt(int32_t data) {
  uint32_t d = htonl(data);
  return sendBytes(&d, sizeof(d));
}

bool HostChannel::readShort(short& data) {
  int16_t d;
  if (!readBytes(&d, sizeof(d))) return false;
  data = ntohs(d);
  return true;
}

bool HostChannel::sendShort(const short data) {
  uint16_t d = htons(data);
  return sendBytes(&d, sizeof(d));
}

bool HostChannel::readLong(long long& data) {
  // network is big-endian
  int32_t d[2];
  if (!readBytes(d, sizeof(d))) return false;
  data = static_cast<long long>((static_cast<uint64_t>(ntohl(d[0])) << 32) | ntohl(d[1]));
  return true;
}

bool HostChannel::sendLong(const long long data) {
  int32_t d[2] = {htonl(static_cast<int32_t>(data >> 32)), htonl(static_cast<int32_t>(data & 0xFFFFFFFF))};
  return sendBytes(d, sizeof(d));
}

// TODO: byte order?
bool HostChannel::readFloat(float& data) {
  return readInt(reinterpret_cast<int&>(data));
}

// TODO: byte order?
bool HostChannel::sendFloat(const float data) {
  return sendInt(*reinterpret_cast<const uint32_t*>(&data));
}

// TODO: byte order?
bool HostChannel::readDouble(double& data) {
  return readLong(reinterpret_cast<long long&>(data));
}

// TODO: byte order?
bool HostChannel::sendDouble(const double data) {
  return sendLong(*reinterpret_cast<const long long*>(&data));
}

bool HostChannel::readStringLength(uint32_t& data) {
  int32_t val;
  if (!readInt(val)) return false;
  // TODO: assert positive?
  data = val;
  return true;
}

bool HostChannel::readStringBytes(char* data, const uint32_t len) {
  return readBytes(data, len);
}

bool HostChannel::readString(std::string& strRef) {
  uint32_t len;
  if (!readStringLength(len)) {
    printf("readString: failed to read length\n");
    return false;
  }
  // Allocating variable-length arrays on the stack is a GCC feature,
  // and is vulnerable to stack overflow attacks.
  scoped_array<char> buf(new char[len]);
  if (!readStringBytes(buf.get(), len)) {
    printf("readString: failed to read %d bytes\n", len);
    return false;
  }
  strRef.assign(buf.get(), len);
  return true;
}

static inline double operator-(const struct timeval& end, const struct timeval& begin) {
  double us = end.tv_sec * 1000000.0 + end.tv_usec - begin.tv_sec * 1000000.0 - begin.tv_usec;
  return us;
}

ReturnMessage* HostChannel::reactToMessages(SessionHandler* handler, bool expectReturn) {
  char type;
  while (true) {
    flush();
     Debug::log(Debug::Spam) << "Waiting for response, flushed output" << Debug::flush;
    if (!readByte(type)) {
      Debug::log(Debug::Error) << "Failed to receive message type" << Debug::flush;
      return 0;
    }
    switch (type) {
      case MESSAGE_TYPE_INVOKE:
        {
          scoped_ptr<InvokeMessage> imsg(InvokeMessage::receive(*this));
          if (!imsg.get()) {
            Debug::log(Debug::Error) << "Failed to receive invoke message" << Debug::flush;
            return 0;
          }
          Value returnValue;
          bool exception = handler->invoke(*this, imsg->getThis(), imsg->getMethodName(),
              imsg->getNumArgs(), imsg->getArgs(), &returnValue);
          ReturnMessage::send(*this, exception, returnValue);
        }
        break;
      case MESSAGE_TYPE_INVOKESPECIAL:
        {
          // scottb: I think this is never used; I think server never sends invokeSpecial
          scoped_ptr<InvokeSpecialMessage> imsg(InvokeSpecialMessage::receive(*this));
          if (!imsg.get()) {
            Debug::log(Debug::Error) << "Failed to receive invoke special message" << Debug::flush;
            return 0;
          }
          Value returnValue;
          bool exception = handler->invokeSpecial(*this, imsg->getDispatchId(),
              imsg->getNumArgs(), imsg->getArgs(), &returnValue);
          ReturnMessage::send(*this, exception, returnValue);
        }
        break;
      case MESSAGE_TYPE_FREEVALUE:
        {
          scoped_ptr<FreeValueMessage> freeMsg(FreeValueMessage::receive(*this));
          if (!freeMsg.get()) {
            Debug::log(Debug::Error) << "Failed to receive free value message" << Debug::flush;
            return 0;
          }
          handler->freeValue(*this, freeMsg->getIdCount(), freeMsg->getIds());
        }
        // do not send a response
        break;
      case MESSAGE_TYPE_LOADJSNI:
        {
          scoped_ptr<LoadJsniMessage> loadMsg(LoadJsniMessage::receive(*this));
          if (!loadMsg.get()) {
            Debug::log(Debug::Error) << "Failed to receive load JSNI message" << Debug::flush;
            return 0;
          }
          handler->loadJsni(*this, loadMsg->getJs());
        }
        // do not send a response
        break;
      case MESSAGE_TYPE_RETURN:
        if (!expectReturn) {
          Debug::log(Debug::Error) << "Received unexpected RETURN" << Debug::flush;
        }
        return ReturnMessage::receive(*this);
      case MESSAGE_TYPE_QUIT:
        if (expectReturn) {
          Debug::log(Debug::Error) << "Received QUIT while waiting for return" << Debug::flush;
        }
    	  disconnectFromHost();
        return 0;
      default:
        // TODO(jat): error handling
        Debug::log(Debug::Error) << "Unexpected message type " << type
            << ", expectReturn=" << expectReturn << Debug::flush;
        disconnectFromHost();
        return 0;
    }
  }
}

bool HostChannel::readValue(Value& valueRef) {
  char typeBuf;
  if (!readByte(typeBuf)) return false;
  Value::ValueType type = Value::ValueType(typeBuf);
  switch (type) {
    case Value::NULL_TYPE:
      valueRef.setNull();
      return true;
    case Value::UNDEFINED:
      valueRef.setUndefined();
      return true;
    case Value::BOOLEAN:
      {
        char val;
        if (!readByte(val)) return false;
        valueRef.setBoolean(val != 0);
      }
      return true;
    case Value::BYTE:
      {
        char val;
        if (!readByte(val)) return false;
        valueRef.setByte(val);
      }
      return true;
    case Value::CHAR:
      {
        short val;
        if (!readShort(val)) return false;
        valueRef.setChar(val);
      }
      return true;
    case Value::SHORT:
      {
        short val;
        if (!readShort(val)) return false;
        valueRef.setShort(val);
      }
      return true;
    case Value::STRING:
      {
        std::string val;
        if (!readString(val)) return false;
        valueRef.setString(val);
      }
      return true;
    case Value::INT:
      {
        int val;
        if (!readInt(val)) return false;
        valueRef.setInt(val);
      }
      return true;
    case Value::LONG:
      {
        long long val;
        if (!readLong(val)) return false;
        valueRef.setLong(val);
      }
      return true;
    case Value::DOUBLE:
      {
        double val;
        if (!readDouble(val)) return false;
        valueRef.setDouble(val);
      }
      return true;
    case Value::JAVA_OBJECT:
      {
        int objId;
        if (!readInt(objId)) return false;
        valueRef.setJavaObject(objId);
      }
      return true;
    case Value::JS_OBJECT:
      {
        int val;
        if (!readInt(val)) return false;
        valueRef.setJsObjectId(val);
      }
      return true;
    default:
      Debug::log(Debug::Error) << "Unhandled value type sent from server: " << type << Debug::flush;
      break;
  }
  return false;
}

bool HostChannel::sendValue(const Value& value) {
  Value::ValueType type = value.getType();
  if (!sendByte(type)) return false;
  switch (type) {
    case Value::NULL_TYPE:
    case Value::UNDEFINED:
      return true;
    case Value::BOOLEAN:
      return sendByte(value.getBoolean() ? 1 : 0);
    case Value::BYTE:
      return sendByte(value.getByte());
    case Value::CHAR:
      return sendShort(short(value.getChar()));
    case Value::SHORT:
      return sendShort(value.getShort());
    case Value::INT:
      return sendInt(value.getInt());
    case Value::LONG:
      return sendLong(value.getLong());
    case Value::STRING:
      return sendString(value.getString());
    case Value::DOUBLE:
      return sendDouble(value.getDouble());
    case Value::FLOAT:
      return sendFloat(value.getFloat());
    case Value::JS_OBJECT:
      return sendInt(value.getJsObjectId());
    case Value::JAVA_OBJECT:
      return sendInt(value.getJavaObjectId());
    default:
      Debug::log(Debug::Error) << "Unhandled value type sent to server: " << type << Debug::flush;
      break;
  }
  return false;
}
