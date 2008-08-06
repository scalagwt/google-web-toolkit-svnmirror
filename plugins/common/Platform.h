#ifndef _H_Platform
#define _H_Platform
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

// Platform-specific hacks to enable more shared code elsewhere
#ifdef _WINDOWS
typedef __int32 int32_t;
typedef unsigned __int32 uint32_t; 
typedef __int16 int16_t;
typedef unsigned __int16 uint16_t;
typedef __int64 int64_t;
typedef unsigned __int64 uint64_t;
typedef long ssize_t;

#define snprintf sprintf_s
#define SOCKETTYPE SOCKET
#pragma warning(disable:4267) // Bogus conversion from size_t -> unsigned int warnings.
#else
#define SOCKETTYPE int
#endif

#endif
