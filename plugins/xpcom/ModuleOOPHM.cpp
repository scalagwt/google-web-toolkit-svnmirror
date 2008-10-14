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
#include "ExternalWrapper.h"

#include "nsCOMPtr.h"
#include "nsIGenericFactory.h"
#include "nsICategoryManager.h"
#include "nsISupports.h"
#include "nsServiceManagerUtils.h"
#include "nsXPCOMCID.h"

#ifndef NS_DECL_CLASSINFO
#include "nsIClassInfoImpl.h" // 1.9 only
#endif

#ifdef _WINDOWS
#include <windows.h>

BOOL APIENTRY DllMain(HMODULE hModule, DWORD ulReasonForCall, LPVOID lpReserved) {
  switch (ulReasonForCall) {
    case DLL_PROCESS_ATTACH:
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
      break;
  }
  return TRUE;
}
#endif

NS_GENERIC_FACTORY_CONSTRUCTOR(ExternalWrapper)
NS_DECL_CLASSINFO(ExternalWrapper)

static NS_IMETHODIMP registerSelf(nsIComponentManager *aCompMgr, nsIFile *aPath,
    const char *aLoaderStr, const char *aType,
    const nsModuleComponentInfo *aInfo) {

  Debug::log(Debug::Info) << "Registered GWT hosted mode plugin"
      << Debug::flush;
  nsresult rv;
  nsCOMPtr<nsICategoryManager> categoryManager =
      do_GetService(NS_CATEGORYMANAGER_CONTRACTID, &rv);

  NS_ENSURE_SUCCESS(rv, rv);

  rv = categoryManager->AddCategoryEntry("JavaScript global property",
      "__gwt_HostedModePlugin", OOPHM_CONTRACTID, true, true, nsnull);

  if (rv != NS_OK) {
    Debug::log(Debug::Error) << "ModuleOOPHM registerSelf returned " << rv
        << Debug::flush;
  }
  return rv;
}

static NS_IMETHODIMP factoryDestructor(void) {
  Debug::log(Debug::Debugging) << "ModuleOOPHM factoryDestructor()"
      << Debug::flush;
  return NS_OK;
}

static NS_IMETHODIMP unregisterSelf(nsIComponentManager *aCompMgr,
    nsIFile *aPath, const char *aLoaderStr,
    const nsModuleComponentInfo *aInfo) {
  Debug::log(Debug::Debugging) << "ModuleOOPHM unRegisterSelf()"
      << Debug::flush;
  return NS_OK;
}

static nsModuleComponentInfo components[] = {
    {
       OOPHM_CLASSNAME,
       OOPHM_CID,
       OOPHM_CONTRACTID,
       ExternalWrapperConstructor,
       registerSelf,
       unregisterSelf, /* unregister self */
       factoryDestructor, /* factory destructor */
       NS_CI_INTERFACE_GETTER_NAME(ExternalWrapper), /* get interfaces */
       nsnull, /* language helper */
       &NS_CLASSINFO_NAME(ExternalWrapper), /* global class-info pointer */
       0 /* class flags */
    }
};

NS_IMPL_NSGETMODULE("ExternalWrapperModule", components)
