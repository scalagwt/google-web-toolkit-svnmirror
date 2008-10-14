XUL_SDK_PATH=../../../xulrunner-sdk
#GECKO_SDK_PATH=../../../gecko-sdk-1.8
GECKO_SDK_PATH=$(XUL_SDK_PATH)/sdk
GECKO_BINSDK_PATH=$(GECKO_SDK_PATH)
#GECKO_BINSDK_PATH=/usr/lib/xulrunner-devel-1.9.0.1/
INC=-I. -I../common -I$(GECKO_SDK_PATH)/include -I$(XUL_SDK_PATH)/include/caps -I$(XUL_SDK_PATH)/include/dom -I$(XUL_SDK_PATH)/include/js -I$(XUL_SDK_PATH)/include/necko -I$(XUL_SDK_PATH)/include/string -I$(XUL_SDK_PATH)/include/widget -I$(XUL_SDK_PATH)/include/xpcom -I$(XUL_SDK_PATH)/include/xpconnect -I$(XUL_SDK_PATH)/include
RUN_PATH_FLAG=-rpath-link
LD_FLAGS=-L$(GECKO_BINSDK_PATH)/lib -L$(GECKO_BINSDK_PATH)/bin -Wl,$(RUN_PATH_FLAG),$(GECKO_BINSDK_PATH)/bin -lxpcomglue_s -lxpcom -lnspr4 -lmozjs
XPIDL_FLAGS=-I$(GECKO_SDK_PATH)/idl
XPIDL=$(GECKO_SDK_PATH)/bin/xpidl
FLAG32BIT=$(shell ./get32bitflag arch)
CFLAGS=-g -O2 -fPIC -fshort-wchar $(INC)
CXXFLAGS=$(CFLAGS)

INSTDIR := extension/platform/$(shell ./getarch arch)/components
#INSTDIR := extension/components

COMMON= ../common/libcommon$(FLAG32BIT).a

all:: oophm-xpcom.xpi

OBJS=	ExternalWrapper.o ModuleOOPHM.o FFSessionHandler.o JavaObject.o JSRunner.o XpcomDebug.o

SRCS=	ExternalWrapper.cpp ModuleOOPHM.cpp FFSessionHandler.cpp JavaObject.cpp JSRunner.cpp XpcomDebug.cpp

oophm-xpcom.xpi: arch extension $(INSTDIR)/liboophm.so \
  extension/components/IOOPHM.xpt extension/install.rdf
	-rm -f oophm-xpcom.xpi
	cd extension; zip -r -D -9 -o ../$@ * -x '*/.svn/*' -x 'META-INF/*'

extension: prebuilt/extension
	-rm -rf extension
	cp -r $< $@

$(INSTDIR):
	-mkdir -p $@

extension/components:
	-mkdir -p $@

arch: computearch
	./computearch arch
	@echo "Restart make"
	@exit 1

$(INSTDIR)/liboophm.so: liboophm.so
	cp $< $@

extension/components/IOOPHM.xpt: IOOPHM.xpt extension/components
	cp $< $@

extension/install.rdf: install-template.rdf version
	sed s/GWT_OOPHM_VERSION/`cat version`/ install-template.rdf >$@

version: computeversion $(HDRS) $(SRCS) $(COMMON)
	./computeversion $@

IOOPHM.h: IOOPHM.idl
	$(XPIDL) -m header $(XPIDL_FLAGS) $<

IOOPHM.xpt: IOOPHM.idl
	$(XPIDL) -m typelib $(XPIDL_FLAGS) $<

liboophm.so: $(OBJS) $(COMMON) $(INSTDIR)
	g++ -m$(FLAG32BIT) -shared -o $@ $(OBJS) $(COMMON) $(LD_FLAGS)

$(COMMON): common
	cd ../common; make -f Makefile

$(OBJS): arch IOOPHM.h

.PHONY: all clean realclean depend common install install-platform

install:: oophm-xpcom.xpi
	-cp --preserve=mode $< prebuilt

install-platform:: liboophm.so
	-mkdir -p $(subst extension,prebuilt/extension,$(INSTDIR))
	-cp --preserve=mode $< $(subst extension,prebuilt/extension,$(INSTDIR))
clean::
	-rm -f $(OBJS) liboophm.so liboophm.rsrc IOOPHM.h IOOPHM.xpt oophm-xpcom.xpi
	-rm -rf extension

realclean:: clean
	-rm -f arch

depend:
	g++ -MM $(CFLAGS) $(SRCS) >>Makefile
#	makedepend -- $(CFLAGS) -- $(SRCS)

# DO NOT DELETE
ExternalWrapper.o: ExternalWrapper.cpp ExternalWrapper.h mozincludes.h \
  $(XUL_SDK_PATH)/sdk/include/xpcom-config.h \
  $(XUL_SDK_PATH)/include/mozilla-config.h IOOPHM.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupports.h \
  $(XUL_SDK_PATH)/sdk/include/nsrootidl.h \
  $(XUL_SDK_PATH)/sdk/include/nscore.h \
  $(XUL_SDK_PATH)/sdk/include/prtypes.h \
  $(XUL_SDK_PATH)/sdk/include/prcpucfg.h \
  $(XUL_SDK_PATH)/sdk/include/obsolete/protypes.h \
  $(XUL_SDK_PATH)/sdk/include/nsError.h \
  $(XUL_SDK_PATH)/sdk/include/prtime.h \
  $(XUL_SDK_PATH)/sdk/include/prlong.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsBase.h \
  $(XUL_SDK_PATH)/sdk/include/nsID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsUtils.h \
  $(XUL_SDK_PATH)/sdk/include/nsDebug.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOM.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMCID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsImpl.h \
  $(XUL_SDK_PATH)/sdk/include/prthread.h \
  $(XUL_SDK_PATH)/sdk/include/prinrval.h \
  $(XUL_SDK_PATH)/sdk/include/pratom.h \
  $(XUL_SDK_PATH)/sdk/include/prlock.h \
  $(XUL_SDK_PATH)/sdk/include/nsTraceRefcnt.h \
  $(XUL_SDK_PATH)/sdk/include/nsCycleCollector.h \
  FFSessionHandler.h SessionData.h ../common/SessionHandler.h \
  ../common/BrowserChannel.h ../common/Value.h ../common/Debug.h \
  ../common/Platform.h ../common/DebugLevel.h \
  $(XUL_SDK_PATH)/include/js/jsapi.h \
  $(XUL_SDK_PATH)/include/js/jspubtd.h \
  $(XUL_SDK_PATH)/include/js/jstypes.h \
  $(XUL_SDK_PATH)/include/js/jsautocfg.h \
  $(XUL_SDK_PATH)/include/js/jsotypes.h \
  $(XUL_SDK_PATH)/include/js/jscompat.h \
  $(XUL_SDK_PATH)/include/js/jslong.h \
  $(XUL_SDK_PATH)/include/js/jsproto.tbl \
  $(XUL_SDK_PATH)/include/js/jsconfig.h \
  $(XUL_SDK_PATH)/include/js/jsutil.h ../common/Debug.h \
  ../common/scoped_ptr/scoped_ptr.h \
  $(XUL_SDK_PATH)/include/caps/nsISecurityCheckedComponent.h \
  $(XUL_SDK_PATH)/sdk/include/nsStringAPI.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMStrings.h \
  $(XUL_SDK_PATH)/sdk/include/prlog.h \
  $(XUL_SDK_PATH)/include/necko/nsIHttpProtocolHandler.h \
  $(XUL_SDK_PATH)/include/necko/nsIProxiedProtocolHandler.h \
  $(XUL_SDK_PATH)/include/necko/nsIProtocolHandler.h \
  $(XUL_SDK_PATH)/include/necko/nsNetCID.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h \
  $(XUL_SDK_PATH)/sdk/include/nsMemory.h \
  $(XUL_SDK_PATH)/sdk/include/nsIMemory.h \
  $(XUL_SDK_PATH)/sdk/include/nsServiceManagerUtils.h \
  $(XUL_SDK_PATH)/sdk/include/nsIServiceManager.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h \
  $(XUL_SDK_PATH)/sdk/include/nsIClassInfoImpl.h \
  $(XUL_SDK_PATH)/sdk/include/nsIClassInfo.h \
  ../common/LoadModuleMessage.h ../common/Message.h \
  ../common/HostChannel.h ../common/ByteOrder.h ../common/Socket.h \
  ../common/AllowedConnections.h ../common/ReturnMessage.h \
  ../common/SessionHandler.h ../common/ServerMethods.h
ModuleOOPHM.o: ModuleOOPHM.cpp ExternalWrapper.h mozincludes.h \
  $(XUL_SDK_PATH)/sdk/include/xpcom-config.h \
  $(XUL_SDK_PATH)/include/mozilla-config.h IOOPHM.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupports.h \
  $(XUL_SDK_PATH)/sdk/include/nsrootidl.h \
  $(XUL_SDK_PATH)/sdk/include/nscore.h \
  $(XUL_SDK_PATH)/sdk/include/prtypes.h \
  $(XUL_SDK_PATH)/sdk/include/prcpucfg.h \
  $(XUL_SDK_PATH)/sdk/include/obsolete/protypes.h \
  $(XUL_SDK_PATH)/sdk/include/nsError.h \
  $(XUL_SDK_PATH)/sdk/include/prtime.h \
  $(XUL_SDK_PATH)/sdk/include/prlong.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsBase.h \
  $(XUL_SDK_PATH)/sdk/include/nsID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsUtils.h \
  $(XUL_SDK_PATH)/sdk/include/nsDebug.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOM.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMCID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsImpl.h \
  $(XUL_SDK_PATH)/sdk/include/prthread.h \
  $(XUL_SDK_PATH)/sdk/include/prinrval.h \
  $(XUL_SDK_PATH)/sdk/include/pratom.h \
  $(XUL_SDK_PATH)/sdk/include/prlock.h \
  $(XUL_SDK_PATH)/sdk/include/nsTraceRefcnt.h \
  $(XUL_SDK_PATH)/sdk/include/nsCycleCollector.h \
  FFSessionHandler.h SessionData.h ../common/SessionHandler.h \
  ../common/BrowserChannel.h ../common/Value.h ../common/Debug.h \
  ../common/Platform.h ../common/DebugLevel.h \
  $(XUL_SDK_PATH)/include/js/jsapi.h \
  $(XUL_SDK_PATH)/include/js/jspubtd.h \
  $(XUL_SDK_PATH)/include/js/jstypes.h \
  $(XUL_SDK_PATH)/include/js/jsautocfg.h \
  $(XUL_SDK_PATH)/include/js/jsotypes.h \
  $(XUL_SDK_PATH)/include/js/jscompat.h \
  $(XUL_SDK_PATH)/include/js/jslong.h \
  $(XUL_SDK_PATH)/include/js/jsproto.tbl \
  $(XUL_SDK_PATH)/include/js/jsconfig.h \
  $(XUL_SDK_PATH)/include/js/jsutil.h ../common/Debug.h \
  ../common/scoped_ptr/scoped_ptr.h \
  $(XUL_SDK_PATH)/include/caps/nsISecurityCheckedComponent.h \
  $(XUL_SDK_PATH)/sdk/include/nsStringAPI.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMStrings.h \
  $(XUL_SDK_PATH)/sdk/include/prlog.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h \
  $(XUL_SDK_PATH)/sdk/include/nsIGenericFactory.h \
  $(XUL_SDK_PATH)/sdk/include/nsIFactory.h \
  $(XUL_SDK_PATH)/sdk/include/nsIModule.h \
  $(XUL_SDK_PATH)/sdk/include/nsIClassInfo.h \
  $(XUL_SDK_PATH)/sdk/include/nsICategoryManager.h \
  $(XUL_SDK_PATH)/sdk/include/nsISimpleEnumerator.h \
  $(XUL_SDK_PATH)/sdk/include/nsServiceManagerUtils.h \
  $(XUL_SDK_PATH)/sdk/include/nsIServiceManager.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMCID.h \
  $(XUL_SDK_PATH)/sdk/include/nsIClassInfoImpl.h
FFSessionHandler.o: FFSessionHandler.cpp FFSessionHandler.h mozincludes.h \
  $(XUL_SDK_PATH)/sdk/include/xpcom-config.h \
  $(XUL_SDK_PATH)/include/mozilla-config.h SessionData.h \
  ../common/SessionHandler.h ../common/BrowserChannel.h ../common/Value.h \
  ../common/Debug.h ../common/Platform.h ../common/DebugLevel.h \
  $(XUL_SDK_PATH)/include/js/jsapi.h \
  $(XUL_SDK_PATH)/include/js/jspubtd.h \
  $(XUL_SDK_PATH)/include/js/jstypes.h \
  $(XUL_SDK_PATH)/include/js/jsautocfg.h \
  $(XUL_SDK_PATH)/include/js/jsotypes.h \
  $(XUL_SDK_PATH)/include/js/jscompat.h \
  $(XUL_SDK_PATH)/include/js/jslong.h \
  $(XUL_SDK_PATH)/include/js/jsproto.tbl \
  $(XUL_SDK_PATH)/include/js/jsconfig.h \
  $(XUL_SDK_PATH)/include/js/jsutil.h ../common/HostChannel.h \
  ../common/ByteOrder.h ../common/Socket.h ../common/AllowedConnections.h \
  ../common/Message.h ../common/ReturnMessage.h \
  ../common/SessionHandler.h JavaObject.h JSRunner.h ../common/Debug.h \
  ../common/scoped_ptr/scoped_ptr.h RootedObject.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h \
  $(XUL_SDK_PATH)/sdk/include/nsDebug.h \
  $(XUL_SDK_PATH)/sdk/include/nscore.h \
  $(XUL_SDK_PATH)/sdk/include/prtypes.h \
  $(XUL_SDK_PATH)/sdk/include/prcpucfg.h \
  $(XUL_SDK_PATH)/sdk/include/obsolete/protypes.h \
  $(XUL_SDK_PATH)/sdk/include/nsError.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOM.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMCID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsUtils.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsBase.h \
  $(XUL_SDK_PATH)/sdk/include/nsID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsImpl.h \
  $(XUL_SDK_PATH)/sdk/include/prthread.h \
  $(XUL_SDK_PATH)/sdk/include/prinrval.h \
  $(XUL_SDK_PATH)/sdk/include/pratom.h \
  $(XUL_SDK_PATH)/sdk/include/prlock.h \
  $(XUL_SDK_PATH)/sdk/include/nsTraceRefcnt.h \
  $(XUL_SDK_PATH)/sdk/include/nsCycleCollector.h \
  $(XUL_SDK_PATH)/include/xpconnect/nsIJSContextStack.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupports.h \
  $(XUL_SDK_PATH)/sdk/include/nsrootidl.h \
  $(XUL_SDK_PATH)/sdk/include/prtime.h \
  $(XUL_SDK_PATH)/sdk/include/prlong.h \
  $(XUL_SDK_PATH)/include/caps/nsIPrincipal.h \
  $(XUL_SDK_PATH)/include/xpcom/nsISerializable.h \
  $(XUL_SDK_PATH)/sdk/include/nsServiceManagerUtils.h \
  $(XUL_SDK_PATH)/sdk/include/nsIServiceManager.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h
JavaObject.o: JavaObject.cpp JavaObject.h mozincludes.h \
  $(XUL_SDK_PATH)/sdk/include/xpcom-config.h \
  $(XUL_SDK_PATH)/include/mozilla-config.h \
  $(XUL_SDK_PATH)/include/js/jsapi.h \
  $(XUL_SDK_PATH)/include/js/jspubtd.h \
  $(XUL_SDK_PATH)/include/js/jstypes.h \
  $(XUL_SDK_PATH)/include/js/jsautocfg.h \
  $(XUL_SDK_PATH)/include/js/jsotypes.h \
  $(XUL_SDK_PATH)/include/js/jscompat.h \
  $(XUL_SDK_PATH)/include/js/jslong.h \
  $(XUL_SDK_PATH)/include/js/jsproto.tbl \
  $(XUL_SDK_PATH)/include/js/jsconfig.h \
  $(XUL_SDK_PATH)/include/js/jsutil.h FFSessionHandler.h \
  SessionData.h ../common/SessionHandler.h ../common/BrowserChannel.h \
  ../common/Value.h ../common/Debug.h ../common/Platform.h \
  ../common/DebugLevel.h ../common/ServerMethods.h ../common/Debug.h \
  ../common/HostChannel.h ../common/ByteOrder.h ../common/Socket.h \
  ../common/AllowedConnections.h ../common/Message.h \
  ../common/ReturnMessage.h ../common/SessionHandler.h \
  ../common/InvokeMessage.h ../common/ReturnMessage.h \
  ../common/scoped_ptr/scoped_ptr.h
JSRunner.o: JSRunner.cpp JSRunner.h mozincludes.h \
  $(XUL_SDK_PATH)/sdk/include/xpcom-config.h \
  $(XUL_SDK_PATH)/include/mozilla-config.h \
  $(XUL_SDK_PATH)/include/js/jsapi.h \
  $(XUL_SDK_PATH)/include/js/jspubtd.h \
  $(XUL_SDK_PATH)/include/js/jstypes.h \
  $(XUL_SDK_PATH)/include/js/jsautocfg.h \
  $(XUL_SDK_PATH)/include/js/jsotypes.h \
  $(XUL_SDK_PATH)/include/js/jscompat.h \
  $(XUL_SDK_PATH)/include/js/jslong.h \
  $(XUL_SDK_PATH)/include/js/jsproto.tbl \
  $(XUL_SDK_PATH)/include/js/jsconfig.h \
  $(XUL_SDK_PATH)/include/js/jsutil.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h \
  $(XUL_SDK_PATH)/sdk/include/nsDebug.h \
  $(XUL_SDK_PATH)/sdk/include/nscore.h \
  $(XUL_SDK_PATH)/sdk/include/prtypes.h \
  $(XUL_SDK_PATH)/sdk/include/prcpucfg.h \
  $(XUL_SDK_PATH)/sdk/include/obsolete/protypes.h \
  $(XUL_SDK_PATH)/sdk/include/nsError.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOM.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMCID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsUtils.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsBase.h \
  $(XUL_SDK_PATH)/sdk/include/nsID.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupportsImpl.h \
  $(XUL_SDK_PATH)/sdk/include/prthread.h \
  $(XUL_SDK_PATH)/sdk/include/prinrval.h \
  $(XUL_SDK_PATH)/sdk/include/pratom.h \
  $(XUL_SDK_PATH)/sdk/include/prlock.h \
  $(XUL_SDK_PATH)/sdk/include/nsTraceRefcnt.h \
  $(XUL_SDK_PATH)/sdk/include/nsCycleCollector.h \
  $(XUL_SDK_PATH)/include/caps/nsIPrincipal.h \
  $(XUL_SDK_PATH)/include/xpcom/nsISerializable.h \
  $(XUL_SDK_PATH)/include/xpcom/nsISupports.h \
  $(XUL_SDK_PATH)/include/xpcom/nsrootidl.h \
  $(XUL_SDK_PATH)/include/xpcom/nscore.h \
  $(XUL_SDK_PATH)/sdk/include/prtime.h \
  $(XUL_SDK_PATH)/sdk/include/prlong.h \
  $(XUL_SDK_PATH)/include/xpcom/nsISupportsBase.h \
  $(XUL_SDK_PATH)/include/xpcom/nsISupportsUtils.h \
  $(XUL_SDK_PATH)/include/dom/nsIScriptGlobalObject.h \
  $(XUL_SDK_PATH)/sdk/include/nsISupports.h \
  $(XUL_SDK_PATH)/include/widget/nsEvent.h \
  $(XUL_SDK_PATH)/sdk/include/nsIProgrammingLanguage.h \
  $(XUL_SDK_PATH)/include/dom/nsIScriptObjectPrincipal.h \
  $(XUL_SDK_PATH)/sdk/include/nsIURI.h \
  $(XUL_SDK_PATH)/include/xpconnect/nsIXPConnect.h \
  $(XUL_SDK_PATH)/sdk/include/nsIClassInfo.h \
  $(XUL_SDK_PATH)/include/xpconnect/xpccomponents.h \
  $(XUL_SDK_PATH)/include/xpconnect/xpcexception.h \
  $(XUL_SDK_PATH)/include/xpcom/nsIException.h \
  $(XUL_SDK_PATH)/include/xpconnect/xpcjsid.h \
  $(XUL_SDK_PATH)/sdk/include/nsIComponentManager.h \
  $(XUL_SDK_PATH)/include/xpconnect/nsIScriptableInterfaces.h \
  $(XUL_SDK_PATH)/include/xpcom/nsIInterfaceInfoManager.h \
  $(XUL_SDK_PATH)/include/xpcom/nsIInterfaceInfo.h \
  $(XUL_SDK_PATH)/include/xpcom/nsIEnumerator.h \
  $(XUL_SDK_PATH)/include/xpcom/nsISimpleEnumerator.h \
  $(XUL_SDK_PATH)/include/xpcom/nsIExceptionService.h \
  $(XUL_SDK_PATH)/include/xpcom/nsIVariant.h \
  $(XUL_SDK_PATH)/include/js/jspubtd.h \
  $(XUL_SDK_PATH)/include/xpcom/xptinfo.h \
  $(XUL_SDK_PATH)/sdk/include/prtypes.h \
  $(XUL_SDK_PATH)/include/xpcom/xpt_struct.h \
  $(XUL_SDK_PATH)/include/xpcom/xpt_arena.h \
  $(XUL_SDK_PATH)/include/xpconnect/nsAXPCNativeCallContext.h \
  $(XUL_SDK_PATH)/sdk/include/nsCOMPtr.h \
  $(XUL_SDK_PATH)/sdk/include/nsStringAPI.h \
  $(XUL_SDK_PATH)/sdk/include/nsXPCOMStrings.h \
  $(XUL_SDK_PATH)/sdk/include/prlog.h
