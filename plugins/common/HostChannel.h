#ifndef __H_HostChannel
#define __H_HostChannel
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

#include "Debug.h"

// Sun's cstdio doesn't define a bunch of stuff
#include <stdio.h>
#include <string>

#include "Socket.h"
#include "AllowedConnections.h"
#include "Platform.h"
#include "Message.h"
#include "ReturnMessage.h"
#include "Value.h"
#include "SessionHandler.h"

class HostChannel {
  Socket sock;
  AllowedConnections whitelist;

public:
  ~HostChannel() {
    if (isConnected()) {
      disconnectFromHost();
    }
    Debug::log(Debug::Debugging) << "HostChannel destroyed" << Debug::flush;
  }

  bool connectToHost(char* host, unsigned port);
  bool disconnectFromHost();

  bool isConnected() const {
    return sock.isConnected();
  }

  bool readBytes(void* data, size_t dataLen) {
    char* ptr = static_cast<char*>(data);
    while(dataLen > 0) {
      if (!readByte(*ptr++)) {
        return false;
      }
      --dataLen;
    }
    return true;
  }

  bool sendBytes(const void* data, size_t dataLen) {
    const char* ptr = static_cast<const char*>(data);
    while(dataLen > 0) {
      if (!sendByte(*ptr++)) {
        return false;
      }
      --dataLen;
    }
    return true;
  }

  // TODO: don't pass out-params by reference as it makes the call site misleading
  bool readInt(int32_t& data);  
  bool sendInt(const int32_t data);
 
  bool readShort(short& data);  
  bool sendShort(const short data);

  bool readLong(long long& data);  
  bool sendLong(const long long data);
  
  bool readFloat(float& data);  
  bool sendFloat(const float data);
  
  bool readDouble(double& doubleRef);
  bool sendDouble(const double data);
  
  bool readByte(char& data) {
    int c = sock.readByte();
    if (c < 0) {
      return false;
    }
    data = static_cast<char>(c);
    return true;
  }

  bool sendByte(const char data) {
    return sock.writeByte(data);
  }
  
  bool readStringLength(uint32_t& data);
  bool readStringBytes(char* data, const uint32_t len);
  bool readString(std::string& strRef);
  bool HostChannel::sendString(const char* data, const uint32_t len) {
    return sendInt(len) && sendBytes(data, len);
  }

  bool HostChannel::sendString(const std::string& str) {
    return sendString(str.c_str(), static_cast<uint32_t>(str.length()));
  }

  bool readValue(Value& valueRef);
  bool sendValue(const Value& value);
  
  ReturnMessage* reactToMessages(SessionHandler* handler, bool expectReturn);
  
  bool reactToMessages(SessionHandler* handler) {
    return !reactToMessages(handler, false);
  }
  
  bool flush() {
    return sock.flush();
  }
  
  ReturnMessage* reactToMessagesWhileWaitingForReturn(SessionHandler* handler) {
    return reactToMessages(handler, true);
  }
};
#endif
