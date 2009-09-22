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

#import <JavaScriptCore/JavaScriptCore.h>
#import "HostChannel.h"
#import "WebScriptSessionHandler.h"

@interface OophmWebScriptObject : NSObject {
@private
  JSGlobalContextRef _contextRef;
  CrashHandlerRef _crashHandler;
  HostChannel* _hostChannel;
  WebScriptSessionHandlerRef _sessionHandler;
  WebView* _webView;
}
+ (void)initialize;
+ (BOOL)isSelectorExcludedFromWebScript: (SEL)selector;
+ (OophmWebScriptObject*)scriptObjectWithContext: (JSGlobalContextRef)context
                                     withWebView: (WebView*) webView;
+ (NSString*)webScriptNameForSelector: (SEL)selector;
- (BOOL)initForWebScriptWithJsniContext: (WebScriptObject*) jsniContext;
- (BOOL)connectWithUrl: (NSString*) url
        withSessionKey: (NSString*) sessionKey
              withHost: (NSString*) host
        withModuleName: (NSString*) moduleName
 withHostedHtmlVersion: (NSString*) hostedHtmlVersion;
- (void)crashWithMessage: (NSString*)message;
- (void)dealloc;
- (void)finalizeForWebScript;
@end

