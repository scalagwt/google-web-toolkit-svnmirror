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

#include <cstring>

#include "ScriptableInstance.h"
#include "InvokeMessage.h"
#include "ReturnMessage.h"
#include "ServerMethods.h"
#include "mozincludes.h"
#include "scoped_ptr/scoped_ptr.h"
#include "NPVariantWrapper.h"

using std::string;
using std::endl;

void ScriptableInstance::dumpObjectBytes(NPObject* obj) {
  char buf[20];
  Debug::log(Debug::Debugging) << "   object bytes:\n";
  const unsigned char* ptr = reinterpret_cast<const unsigned char*>(obj);
  for (int i = 0; i < 24; ++i) {
    snprintf(buf, sizeof(buf), " %02x", ptr[i]);
    Debug::log(Debug::Debugging) << buf;
  }
  NPVariant objVar;
  OBJECT_TO_NPVARIANT(obj, objVar);
  Debug::log(Debug::Debugging) << " obj.toString()="
      << NPVariantProxy::toString(objVar) << Debug::flush;
}

ScriptableInstance::ScriptableInstance(NPP npp) : NPObjectWrapper<ScriptableInstance>(npp),
    plugin(*reinterpret_cast<Plugin*>(npp->pdata)),
    _channel(new HostChannel()),
    localObjects(),
    _connectId(NPN_GetStringIdentifier("connect")),
    toStringID(NPN_GetStringIdentifier("toString")),
    connectedID(NPN_GetStringIdentifier("connected")),
    statsID(NPN_GetStringIdentifier("stats")),
    savedID(NPN_GetStringIdentifier("saved")),
    jsWrapperID(NPN_GetStringIdentifier("__gwt_jsWrapper")),
    jsTearOffID(NPN_GetStringIdentifier("__gwt_makeTearOff")),
    jsValueOfID(NPN_GetStringIdentifier("valueOf")),
    idx0(NPN_GetIntIdentifier(0)),
    idx1(NPN_GetIntIdentifier(1)) {
  savedValueIdx = -1;
  if (NPN_GetValue(npp, NPNVWindowNPObject, &window) != NPERR_NO_ERROR) {
    Debug::log(Debug::Error) << "Error getting window object" << Debug::flush;
  }
}

ScriptableInstance::~ScriptableInstance() {
  // TODO(jat): free any remaining Java objects held by JS, then make
  // the JS wrapper handle that situation gracefully
  if (window) {
    NPN_ReleaseObject(window);
  }
  for (hash_map<int, JavaObject*>::iterator it = javaObjects.begin(); it != javaObjects.end();
      ++it) {
    Debug::log(Debug::Spam) << "  disconnecting Java wrapper " << it->first << Debug::flush;
    it->second->disconnectPlugin();
  }
  if (_channel) {
    _channel->disconnectFromHost();
    delete _channel;
  }
}

void ScriptableInstance::dumpJSresult(const char* js) {
  NPString npScript;
  dupString(js, npScript);
  NPVariantWrapper wrappedRetVal(*this);
  if (!NPN_Evaluate(getNPP(), window, &npScript, wrappedRetVal.addressForReturn())) {
    Debug::log(Debug::Error) << "   *** dumpJSresult failed" << Debug::flush;
    return;
  }
  Debug::log(Debug::Info) << "dumpWindow=" << wrappedRetVal.toString() << Debug::flush;
}

bool ScriptableInstance::tryGetStringPrimitive(NPObject* obj, NPVariant& result) {
  if (NPN_HasMethod(getNPP(), obj, jsValueOfID)) {
    if (NPN_Invoke(getNPP(), obj, jsValueOfID, 0, 0, &result)
        && NPVariantProxy::isString(result)) {
      return true;
    }
    NPVariantProxy::release(result);
  }
  return false;
}

//=====================================================================================
// NPObject methods
//=====================================================================================

bool ScriptableInstance::hasProperty(NPIdentifier name) {
  if (!NPN_IdentifierIsString(name)) {
    // all numeric properties are ok, as we assume only JSNI code is making
    // the field access via dispatchID
    return true;
  }
  // TODO: special-case toString tear-offs
  return name == statsID || name == connectedID || name == savedID;
}

bool ScriptableInstance::getProperty(NPIdentifier name, NPVariant* variant) {
  Debug::log(Debug::Debugging) << "ScriptableInstance::getProperty(name="
      << NPN_UTF8FromIdentifier(name) << ")" << Debug::flush;
  bool retVal = false;
  VOID_TO_NPVARIANT(*variant);
  if (name == connectedID) {
    BOOLEAN_TO_NPVARIANT(_channel->isConnected(), *variant);
    retVal = true;
  } else if (name == statsID) {
    NPVariantProxy::assignFrom(*variant, "<stats data>");
    retVal = true;
  } else if (name == savedID) {
    if (savedValueIdx >= 0) {
      NPObject* npObj = localObjects.get(savedValueIdx);
      OBJECT_TO_NPVARIANT(npObj, *variant);
      NPN_RetainObject(npObj);
    }
    retVal = true;
  }
  if (retVal) {
    // TODO: testing
    Debug::log(Debug::Debugging) << " return value " << *variant
        << Debug::flush;
  }
  return retVal;
}

bool ScriptableInstance::setProperty(NPIdentifier name, const NPVariant* variant) {
  Debug::log(Debug::Debugging) << "ScriptableInstance::setProperty(name="
      << NPN_UTF8FromIdentifier(name) << ", value=" << *variant << ")"
      << Debug::flush; 
  if (NPN_IdentifierIsString(name)) {
    if (name == savedID && NPVariantProxy::isObject(*variant)) {
      Debug::log(Debug::Debugging) << " set saved value" << Debug::flush;
      savedValueIdx = localObjects.add(NPVariantProxy::getAsObject(*variant));
      return true;
    }
    return false;
  }
  return false;
}

bool ScriptableInstance::hasMethod(NPIdentifier name) {
  Debug::log(Debug::Debugging) << "ScriptableInstance::hasMethod(name=" << NPN_UTF8FromIdentifier(name) << ")"
      << Debug::flush; 
  if (name == _connectId || name == toStringID) {
    return true;
  }
  return false;
}

bool ScriptableInstance::invoke(NPIdentifier name, const NPVariant* args, unsigned argCount,
    NPVariant* result) {
  NPUTF8* uname = NPN_UTF8FromIdentifier(name);
  Debug::log(Debug::Debugging) << "ScriptableInstance::invoke(name=" << uname << ",#args=" << argCount << ")"
      << Debug::flush;
  VOID_TO_NPVARIANT(*result);
  if (name == _connectId) {
    connect(args, argCount, result);
  } else if (name == toStringID) {
    // TODO(jat): figure out why this doesn't show in Firebug
    string val("[GWT OOPHM Plugin: connected=");
    val += _channel->isConnected() ? 'Y' : 'N';
    val += ']';
    NPVariantProxy::assignFrom(*result, val);
  }
  return true;
}

bool ScriptableInstance::invokeDefault(const NPVariant* args, unsigned argCount,
      NPVariant* result) {
  Debug::log(Debug::Debugging) << "ScriptableInstance::invokeDefault(#args=" << argCount << ")"
      << Debug::flush;
  VOID_TO_NPVARIANT(*result);
  return true;
}

bool ScriptableInstance::enumeration(NPIdentifier** propReturn, uint32_t* count) {
  Debug::log(Debug::Debugging) << "ScriptableInstance::enumeration()" << Debug::flush;
  int n = 2;
  NPIdentifier* props = static_cast<NPIdentifier*>(NPN_MemAlloc(sizeof(NPIdentifier) * n));
  props[0] = connectedID;
  props[1] = statsID;
  *propReturn = props;
  *count = n;
  return true;
}

//=====================================================================================
// internal methods
//=====================================================================================

void ScriptableInstance::connect(const NPVariant* args, unsigned argCount, NPVariant* result) {
  if (argCount < 2 || argCount > 4 || !NPVariantProxy::isString(args[0])
      || !NPVariantProxy::isString(args[1])
      || (argCount == 3 && !NPVariantProxy::isObject(args[2]))) {
    // TODO: better failure?
    Debug::log(Debug::Error) << "ScriptableInstance::connect called with incorrect arguments:\n";
    for (unsigned i = 0; i < argCount; ++i) {
      Debug::log(Debug::Error) << " " << i << " " << NPVariantProxy::toString(args[i]) << "\n";
    }
    Debug::log(Debug::Error) << Debug::flush;
    result->type = NPVariantType_Void;
    return;
  }
  const NPString hostAddr = args[0].value.stringValue;
  const NPString moduleName = args[1].value.stringValue;
  if (argCount >= 3) {
    if (window) {
      NPN_ReleaseObject(window);
    }
    // replace our window object with that passed by the caller
    window = NPVariantProxy::getAsObject(args[2]);
    NPN_RetainObject(window);
  }
  Debug::log(Debug::Info) << "ScriptableInstance::connect(host=" << NPVariantProxy::toString(args[0])
      << ",module=" << NPVariantProxy::toString(args[1]) << ")" << Debug::flush;
  bool connected = false;
  unsigned port = 9997;  // TODO(jat): should there be a default?
  int n = GetNPStringUTF8Length(hostAddr);
  scoped_ptr<char> host(new char[n + 1]);
  const char* s = GetNPStringUTF8Characters(hostAddr);
  char* d = host.get();
  while (n > 0 && *s != ':') {
    n--;
    *d++ = *s++;
  }
  *d = 0;
  if (n > 0) {
    port = atoi(s + 1);
  }
  Debug::log(Debug::Info) << "  host=" << host.get() << ",port=" << port << Debug::flush;
  if (_channel->connectToHost(host.get(), port)) {
    Debug::log(Debug::Debugging) << "  connected, sending loadModule" << Debug::flush;
    connected = LoadModuleMessage::send(*_channel, VERSION, GetNPStringUTF8Characters(moduleName),
        GetNPStringUTF8Length(moduleName), NPN_UserAgent(getNPP()), this);
  }
  BOOLEAN_TO_NPVARIANT(connected, *result);
  result->type = NPVariantType_Bool;
}

void ScriptableInstance::dupString(const char* str, NPString& npString) {
  npString.UTF8Length = static_cast<uint32_t>(strlen(str));
  NPUTF8* chars = static_cast<NPUTF8*>(NPN_MemAlloc(npString.UTF8Length));
  memcpy(chars, str, npString.UTF8Length);
  npString.UTF8Characters = chars;
}

// SessionHandler methods
void ScriptableInstance::freeValue(HostChannel& channel, int idCount, const int* const ids) {
  Debug::log(Debug::Debugging) << "freeValue(#ids=" << idCount << ")" << Debug::flush;
  for (int i = 0; i < idCount; ++i) {
	  Debug::log(Debug::Spam) << " id=" << ids[i] << Debug::flush;
    localObjects.free(ids[i]);
  }
}

void ScriptableInstance::loadJsni(HostChannel& channel, const std::string& js) {
  NPString npScript;
  dupString(js.c_str(), npScript);
  NPVariantWrapper npResult(*this);
  Debug::log(Debug::Spam) << "loadJsni - \n" << js << Debug::flush;
  if (!NPN_Evaluate(getNPP(), window, &npScript, npResult.addressForReturn())) {
    Debug::log(Debug::Error) << "loadJsni failed\n" << js << Debug::flush;
  }
}

Value ScriptableInstance::clientMethod_getProperty(HostChannel& channel, int numArgs, const Value* const args) {
  if (numArgs != 2 || !args[0].isInt() || (!args[1].isString() && !args[1].isInt())) {
    Debug::log(Debug::Error) << "Incorrect invocation of getProperty: #args=" << numArgs << ":";
    for (int i = 0; i < numArgs; ++i) {
      Debug::log(Debug::Error) << " " << i << "=" << args[i].toString();
    }
    Debug::log(Debug::Error) << Debug::flush;
    return Value();
  }
  int id = args[0].getInt();
  NPObject* obj = localObjects.get(id);
  NPIdentifier propID;
  if (args[1].isString()) {
    std::string propName = args[1].getString();
    propID = NPN_GetStringIdentifier(propName.c_str());
  } else {
    int propNum = args[1].getInt();
    propID = NPN_GetIntIdentifier(propNum);
  }
  NPVariantWrapper npResult(*this);
  if (!NPN_GetProperty(getNPP(), obj, propID, npResult.addressForReturn())) {
    Debug::log(Debug::Warning) << "getProperty(id=" << id << ", prop="
        << NPN_UTF8FromIdentifier(propID) << ") failed" << Debug::flush;
    return Value();
  }
  return npResult.getAsValue(*this);
}

Value ScriptableInstance::clientMethod_setProperty(HostChannel& channel, int numArgs, const Value* const args) {
  if (numArgs != 2 || !args[0].isInt() || (!args[1].isString() && !args[1].isInt())) {
    Debug::log(Debug::Error) << "Incorrect invocation of setProperty: #args="
        << numArgs << ":";
    for (int i = 0; i < numArgs; ++i) {
      Debug::log(Debug::Error) << " " << i << "=" << args[i].toString();
    }
    Debug::log(Debug::Error) << Debug::flush;
    return Value();
  }
  int id = args[0].getInt();
  NPObject* obj = localObjects.get(id);
  NPIdentifier propID;
  if (args[1].isString()) {
    std::string propName = args[1].getString();
    propID = NPN_GetStringIdentifier(propName.c_str());
  } else {
    int propNum = args[1].getInt();
    propID = NPN_GetIntIdentifier(propNum);
  }
  NPVariantWrapper npValue(*this);
  npValue.operator=(args[2]);
  if (!NPN_SetProperty(getNPP(), obj, propID, npValue.address())) {
    Debug::log(Debug::Warning) << "setProperty(id=" << id << ", prop="
        << NPN_UTF8FromIdentifier(propID) << ", val=" << args[2].toString()
        << ") failed" << Debug::flush;
    return Value();
  }
  return Value();
}

/**
 * SessionHandler.invoke - used by LoadModule and reactToMessages* to process server-side
 * requests to invoke methods in Javascript or the plugin.
 */
bool ScriptableInstance::invokeSpecial(HostChannel& channel, SpecialMethodId dispatchId,
    int numArgs, const Value* const args, Value* returnValue) {
  switch (dispatchId) {
  case SessionHandler::HasMethod:
  case SessionHandler::HasProperty:
    break;
  case SessionHandler::SetProperty:
    *returnValue = clientMethod_setProperty(channel, numArgs, args);
    return false;
  case SessionHandler::GetProperty:
    *returnValue = clientMethod_getProperty(channel, numArgs, args);
    return false;
  default:
    break;
  }
  Debug::log(Debug::Error) << "Unexpected special method " << dispatchId
      << " called on plugin; #args=" << numArgs << ":";
  for (int i = 0; i < numArgs; ++i) {
    Debug::log(Debug::Error) << " " << i << "=" << args[i].toString();
  }
  Debug::log(Debug::Error) << Debug::flush;
  // TODO(jat): should we create a real exception object?
  std::string buf("unexpected invokeSpecial(");
  buf += static_cast<int>(dispatchId);
  buf += ")";
  returnValue->setString(buf);
  return true;
}

bool ScriptableInstance::invoke(HostChannel& channel, const Value& thisRef,
    const std::string& methodName, int numArgs, const Value* const args,
    Value* returnValue) {
  Debug::log(Debug::Debugging) << "invokeJS(" << methodName << ", this=" 
      << thisRef.toString() << ", numArgs=" << numArgs << ")" << Debug::flush;
  numArgs += 3; // account for args to wrapper function
  NPVariantArray varArgs(*this, numArgs);
  varArgs[0] = methodName;
  varArgs[1] = window;
  if (thisRef.isNull()) {
    varArgs[2] = window;
  } else {
    varArgs[2] = thisRef;
  }
  for (int i = 3; i < numArgs; ++i) {
    varArgs[i] = args[i - 3];
  }
  const NPVariant* argArray = varArgs.getArray();
  if (Debug::level(Debug::Spam)) {
    for (int i = 0; i < numArgs; ++i) {
      Debug::log(Debug::Spam) << "  arg " << i << " is "
          << NPVariantProxy::toString(argArray[i]) << Debug::flush;
    }
  }
  NPVariantWrapper wrappedRetVal(*this);
  if (!NPN_Invoke(getNPP(), window, jsWrapperID, argArray, numArgs,
      wrappedRetVal.addressForReturn())) {
    Debug::log(Debug::Error) << "*** invokeJS(" << methodName << ", this="
        << thisRef.toString() << ", numArgs=" << numArgs << ") failed"
        << Debug::flush;
    // TODO(jat): should we create a real exception object?
    returnValue->setString("invoke of " + methodName + " failed");
    return true;
  }
  Debug::log(Debug::Spam) << "  wrapped return is " << wrappedRetVal.toString() << Debug::flush;
  if (methodName.find("toString") != string::npos) {
    Debug::log(Debug::Error) << "returned from toString" << Debug::flush;
  }
  NPVariantWrapper exceptFlag(*this);
  NPVariantWrapper retval(*this);
  NPObject* wrappedArray = wrappedRetVal.getAsObject();
  if (!NPN_GetProperty(getNPP(), wrappedArray, idx0, exceptFlag.addressForReturn())) {
    Debug::log(Debug::Error) << " Error getting element 0 of wrapped return value ("
        << wrappedRetVal << ") on call to " << methodName << Debug::flush;
  }
  if (!NPN_GetProperty(getNPP(), wrappedArray, idx1, retval.addressForReturn())) {
    Debug::log(Debug::Error) << " Error getting element 1 of wrapped return value ("
        << wrappedRetVal << ") on call to " << methodName << Debug::flush;
  }
  Debug::log(Debug::Debugging) << "  return value " << retval.toString() << Debug::flush;
  *returnValue = retval.getAsValue(*this);
  if (exceptFlag.isInt() && exceptFlag.getAsInt() != 0) {
    Debug::log(Debug::Debugging) << "  exception: " << retval << Debug::flush;
    return true;
  }
  return false;
}

bool ScriptableInstance::JavaObject_invoke(int objectId, NPObject* thisObj, int dispId,
    const NPVariant* args, uint32_t numArgs, NPVariant* result) {
  Debug::log(Debug::Debugging) << "JavaObject_invoke(dispId= " << dispId << ", numArgs=" << numArgs << ")" << Debug::flush;
  if (Debug::level(Debug::Spam)) {
    for (uint32_t i = 0; i < numArgs; ++i) {
      Debug::log(Debug::Spam) << "  " << i << " = " << args[i] << Debug::flush;
    }
  }
  Value javaThis;
  if (thisObj) {
    javaThis.setJsObjectId(localObjects.add(thisObj));
  } else {
    javaThis.setJavaObject(objectId);
  }
  scoped_array<Value> vargs(new Value[numArgs]);
  for (unsigned i = 0; i < numArgs; ++i) {
    vargs[i] = NPVariantProxy::getAsValue(args[i], *this);
  }
  if (!InvokeMessage::send(*_channel, javaThis, dispId, numArgs, vargs.get())) {
    Debug::log(Debug::Error) << "JavaObject_invoke: failed to send invoke message" << Debug::flush;
    // TODO(jat): returning false here spams the browser console
    return true;
  }
  Debug::log(Debug::Debugging) << " return from invoke" << Debug::flush;
  scoped_ptr<ReturnMessage> retMsg(_channel->reactToMessagesWhileWaitingForReturn(this));
  if (!retMsg.get()) {
    Debug::log(Debug::Error) << "JavaObject_invoke: failed to get return value" << Debug::flush;
    return false;
  }
  NPVariantProxy::assignFrom(*this, *result, retMsg->getReturnValue());
  // TODO: no way to set an actual exception object!
  return !retMsg->isException();
}

bool ScriptableInstance::JavaObject_getProperty(int objectId, int dispId,
    NPVariant* result) {
  Debug::log(Debug::Debugging) << "JavaObject_getProperty(objectid="
      << objectId << ", dispId=" << dispId << ")" << Debug::flush;
  VOID_TO_NPVARIANT(*result);
  Value propertyValue = ServerMethods::getProperty(*_channel, this, objectId, dispId);
  if (propertyValue.isJsObject()) {
    // TODO(jat): special-case for testing
    NPObject* npObj = localObjects.get(propertyValue.getJsObjectId());
    OBJECT_TO_NPVARIANT(npObj, *result);
    NPN_RetainObject(npObj);
  } else {
    NPVariantProxy::assignFrom(*this, *result, propertyValue);
  }
  Debug::log(Debug::Debugging) << " return val=" << propertyValue
      << ", NPVariant=" << *result << Debug::flush;
  if (NPVariantProxy::isObject(*result)) {
    dumpObjectBytes(NPVariantProxy::getAsObject(*result));
  }
  if (NPVariantProxy::isObject(*result)) {
    Debug::log(Debug::Debugging) << "  final return refcount = "
        << NPVariantProxy::getAsObject(*result)->referenceCount << Debug::flush; 
  }
  return true;
}

bool ScriptableInstance::JavaObject_setProperty(int objectId, int dispId,
    const NPVariant* npValue) {
  Debug::log(Debug::Debugging) << "JavaObject_setProperty(objectid="
      << objectId << ", dispId=" << dispId << ", value=" << *npValue << ")" << Debug::flush;
  if (NPVariantProxy::isObject(*npValue)) {
    Debug::log(Debug::Debugging) << "  before localObj: refcount = "
        << NPVariantProxy::getAsObject(*npValue)->referenceCount << Debug::flush; 
  }
  Value value = NPVariantProxy::getAsValue(*npValue, *this, true);
  if (NPVariantProxy::isObject(*npValue)) {
    Debug::log(Debug::Debugging) << "  after localObj: refcount = "
        << NPVariantProxy::getAsObject(*npValue)->referenceCount << Debug::flush; 
  }
  if (NPVariantProxy::isObject(*npValue)) {
    dumpObjectBytes(NPVariantProxy::getAsObject(*npValue));
  }
  Debug::log(Debug::Debugging) << "  as value: " << value << Debug::flush;
  // TODO: no way to set an actual exception object! (Could ClassCastException on server.)
  return ServerMethods::setProperty(*_channel, this, objectId, dispId, value);
}

bool ScriptableInstance::JavaObject_getToStringTearOff(NPVariant* result) {
  Debug::log(Debug::Debugging) << "JavaObject_getToStringTearOff()" << Debug::flush;
  VOID_TO_NPVARIANT(*result);

  Value temp;
  NPVariantArray varArgs(*this, 3);
  temp.setNull();  varArgs[0] = temp; // proxy: no proxy needed
  temp.setInt(0);  varArgs[1] = temp; // dispId: always 0 for toString()
  temp.setInt(0);  varArgs[2] = temp; // argCount: always 0 for toString()

  if (!NPN_Invoke(getNPP(), window, jsTearOffID, varArgs.getArray(), 3, result)) {
    Debug::log(Debug::Error) << "*** JavaObject_getToStringTearOff() failed"
        << Debug::flush;
    return true;
  }
  return true;
}

JavaObject* ScriptableInstance::createJavaWrapper(int objectId) {
  Debug::log(Debug::Debugging) << "createJavaWrapper(objectId=" << objectId <<  ")" << Debug::flush;
  JavaObject* jObj;
  hash_map<int, JavaObject*>::iterator it = javaObjects.find(objectId);
  if (it != javaObjects.end()) {
    jObj = it->second;
    NPN_RetainObject(jObj);
    return jObj;
  }
  jObj = JavaObject::create(this, objectId);
  javaObjects[objectId] = jObj;
  return jObj;
}

void ScriptableInstance::destroyJavaWrapper(JavaObject* jObj) {
  int objectId = jObj->getObjectId();
  javaObjects.erase(objectId);
  Debug::log(Debug::Debugging) << "destroyJavaWrapper(id=" << objectId << ")" << Debug::flush;
  if (!ServerMethods::freeJava(*_channel, this, 1, &objectId)) {
    Debug::log(Debug::Error) << "Error freeing Java-side object " << objectId
        << Debug::flush;
  }
}
