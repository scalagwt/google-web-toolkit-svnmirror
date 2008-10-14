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

#include "ExternalWrapper.h"

#include "nsIHttpProtocolHandler.h"
#include "nsISupports.h"
#include "nsNetCID.h"
#include "nsCOMPtr.h"
#include "nsMemory.h"
#include "nsServiceManagerUtils.h"

#ifndef NS_IMPL_ISUPPORTS2_CI
#include "nsIClassInfoImpl.h" // 1.9 only
#endif

#include "LoadModuleMessage.h"
#include "ServerMethods.h"

NS_IMPL_ISUPPORTS2_CI(ExternalWrapper, IOOPHM, nsISecurityCheckedComponent)

ExternalWrapper::ExternalWrapper() {
  Debug::log(Debug::Spam) << "ExternalWrapper::ExternalWrapper()" << Debug::flush;
}

ExternalWrapper::~ExternalWrapper() {
  Debug::log(Debug::Spam) << "ExternalWrapper::~ExternalWrapper" << Debug::flush;
}

// define the CID for nsIHttpProtocolHandler
static NS_DEFINE_CID(kHttpHandlerCID, NS_HTTPPROTOCOLHANDLER_CID);

static nsresult getUserAgent(std::string& userAgent) {
  nsresult res;
  nsCOMPtr<nsIHttpProtocolHandler> http = do_GetService(kHttpHandlerCID, &res);
  if (NS_FAILED(res)) {
    return res;
  }
  nsCString userAgentStr;
  res = http->GetUserAgent(userAgentStr);
  if (NS_FAILED(res)) {
    return res;
  }
  userAgent.assign(userAgentStr.get());
  return NS_OK;
}

// TODO: handle context object passed in (currently nsIDOMWindow below)
NS_IMETHODIMP ExternalWrapper::Connect(const nsACString & aAddr, const nsACString & aModuleName, nsIDOMWindow* domWindow, PRBool *_retval) {
  Debug::log(Debug::Spam) << "Address: " << aAddr << " Module: " << aModuleName << Debug::flush;

  // TODO: string utilities?
  nsCString addrAutoStr(aAddr), moduleAutoStr(aModuleName);
  std::string url(addrAutoStr.get());
  
  size_t index = url.find(':');
  if (index == std::string::npos) {
    *_retval = false;
    return NS_OK;
  }
  std::string hostPart = url.substr(0, index);
  std::string portPart = url.substr(index + 1);

  HostChannel* channel = new HostChannel();

  Debug::log(Debug::Debugging) << "Connecting..." << Debug::flush;

  if (!channel->connectToHost(
      const_cast<char*>(hostPart.c_str()),
      atoi(portPart.c_str()))) {
    *_retval = false;
    return NS_OK;
  }

  Debug::log(Debug::Debugging) << "...Connected" << Debug::flush;

  sessionHandler.reset(new FFSessionHandler(channel/*, ctx*/));
  std::string moduleName(moduleAutoStr.get());
  std::string userAgent;

  // get the user agent
  nsresult res = getUserAgent(userAgent);
  if (NS_FAILED(res)) {
    return res;
  }

  LoadModuleMessage::send(*channel, 1,
    moduleName.c_str(), moduleName.length(),
    userAgent.c_str(),
    sessionHandler.get());

  // TODO: return session object?
  *_retval = true;
  return NS_OK;
}

// nsISecurityCheckedComponent
static char* cloneAllAccess() {
  static const char allAccess[] = "allAccess";
  return static_cast<char*>(nsMemory::Clone(allAccess, sizeof(allAccess)));
}

static bool strEquals(const PRUnichar* utf16, const char* ascii) {
  nsCString utf8;
  NS_UTF16ToCString(nsDependentString(utf16), NS_CSTRING_ENCODING_UTF8, utf8);
  return strcmp(ascii, utf8.get()) == 0;
}

NS_IMETHODIMP ExternalWrapper::CanCreateWrapper(const nsIID * iid, char **_retval) {
  Debug::log(Debug::Spam) << "ExternalWrapper::CanCreateWrapper" << Debug::flush;
  *_retval = cloneAllAccess();
  return NS_OK;
}

NS_IMETHODIMP ExternalWrapper::CanCallMethod(const nsIID * iid, const PRUnichar *methodName, char **_retval) {
  Debug::log(Debug::Spam) << "ExternalWrapper::CanCallMethod" << Debug::flush;
  if (strEquals(methodName, "connect")) {
    *_retval = cloneAllAccess();
  } else {
    *_retval = nsnull;
  }
  return NS_OK;
}

NS_IMETHODIMP ExternalWrapper::CanGetProperty(const nsIID * iid, const PRUnichar *propertyName, char **_retval) {
  Debug::log(Debug::Spam) << "ExternalWrapper::CanGetProperty" << Debug::flush;
  *_retval = nsnull;
  return NS_OK;
}
NS_IMETHODIMP ExternalWrapper::CanSetProperty(const nsIID * iid, const PRUnichar *propertyName, char **_retval) {
  Debug::log(Debug::Spam) << "ExternalWrapper::CanSetProperty" << Debug::flush;
  *_retval = nsnull;
  return NS_OK;
}
