# Set to classpath value to get GWT classes from
#GWTDEV=../build/staging/gwt-linux-0.0.0/gwt-dev-linux.jar
GWTDEV=../eclipse/dev/linux/bin

INC=-I..
CFLAGS=-Wall -g -O2 -fPIC $(INC) -rdynamic
CXXFLAGS=$(CFLAGS)

HDRS= HostChannel.h InvokeMessage.h LoadModuleMessage.h Message.h \
      ReturnMessage.h Value.h BrowserChannel.h Debug.h DebugLevel.h \
      SessionHandler.h ServerMethods.h Socket.h AllowedConnections.h \
      LoadJsniMessage.h InvokeSpecialMessage.h FreeValueMessage.h \
      ByteOrder.h

OBJS=	HostChannel.o LoadModuleMessage.o InvokeMessage.o \
	ReturnMessage.o ServerMethods.o Debug.o Socket.o \
	AllowedConnections.o LoadJsniMessage.o InvokeSpecialMessage.o \
	FreeValueMessage.o

SRCS=	HostChannel.cpp LoadModuleMessage.cpp InvokeMessage.cpp \
	ReturnMessage.cpp ServerMethods.cpp Debug.cpp Socket.cpp \
	AllowedConnections.cpp LoadJsniMessage.cpp InvokeSpecialMessage.cpp \
	FreeValueMessage.cpp

OBJ32= $(patsubst %.cpp,obj32/%.o,$(SRCS))
OBJ64= $(patsubst %.cpp,obj64/%.o,$(SRCS))

#all:: libcommon32.a libcommon64.a
all:: libcommon32.a

libcommon32.a: $(OBJ32) obj32.dir
	ar -rv $@ $(OBJ32)

libcommon64.a: $(OBJ64) obj64.dir
	ar -rv $@ $(OBJ64)

obj32.dir:
	-mkdir obj32
	touch obj32.dir

obj64.dir:
	-mkdir obj64
	touch obj64.dir

obj32/%.o: %.cpp obj32.dir
	$(CXX) $(CXXFLAGS) -m32 -c $< -o $@

obj64/%.o: %.cpp obj64.dir
	$(CXX) $(CXXFLAGS) -m64 -c $< -o $@

.PHONY: clean depend testdebug

testdebug:
	(cd testing; CXX=$(CXX) CXXFLAGS="$(CFLAGS) -m32" ./testdebug)
	(cd testing; CXX=$(CXX) CXXFLAGS="$(CFLAGS) -m64" ./testdebug)
clean:
	rm $(OBJ32) $(OBJ64) libcommon32.a libcommon64.a

depend:
	g++ -MM $(CFLAGS) $(SRCS) >>Makefile
#	makedepend -- $(CFLAGS) -- $(SRCS)

# DO NOT DELETE
obj32/HostChannel.o obj64/HostChannel.o: HostChannel.cpp Debug.h Platform.h \
  DebugLevel.h FreeValueMessage.h Message.h BrowserChannel.h HostChannel.h \
  Socket.h AllowedConnections.h ReturnMessage.h Value.h SessionHandler.h \
  LoadJsniMessage.h InvokeMessage.h InvokeSpecialMessage.h QuitMessage.h \
  scoped_ptr/scoped_ptr.h
obj32/LoadModuleMessage.o obj64/LoadModuleMessage.o: LoadModuleMessage.cpp Debug.h Platform.h \
  DebugLevel.h LoadModuleMessage.h Message.h BrowserChannel.h \
  HostChannel.h Socket.h AllowedConnections.h ReturnMessage.h Value.h \
  SessionHandler.h scoped_ptr/scoped_ptr.h
obj32/InvokeMessage.o obj64/InvokeMessage.o: InvokeMessage.cpp InvokeMessage.h Message.h \
  BrowserChannel.h Value.h Debug.h Platform.h DebugLevel.h HostChannel.h \
  Socket.h AllowedConnections.h ReturnMessage.h SessionHandler.h \
  scoped_ptr/scoped_ptr.h
obj32/ReturnMessage.o obj64/ReturnMessage.o: ReturnMessage.cpp ReturnMessage.h Message.h \
  BrowserChannel.h Value.h Debug.h Platform.h DebugLevel.h HostChannel.h \
  Socket.h AllowedConnections.h SessionHandler.h
obj32/ServerMethods.o obj64/ServerMethods.o: ServerMethods.cpp Debug.h Platform.h DebugLevel.h \
  FreeValueMessage.h Message.h BrowserChannel.h HostChannel.h Socket.h \
  AllowedConnections.h ReturnMessage.h Value.h SessionHandler.h \
  InvokeMessage.h InvokeSpecialMessage.h ServerMethods.h \
  scoped_ptr/scoped_ptr.h
obj32/Debug.o obj64/Debug.o: Debug.cpp Debug.h Platform.h DebugLevel.h
obj32/Socket.o obj64/Socket.o: Socket.cpp Platform.h Socket.h Debug.h DebugLevel.h
obj32/AllowedConnections.o obj64/AllowedConnections.o: AllowedConnections.cpp Debug.h Platform.h \
  DebugLevel.h AllowedConnections.h
obj32/LoadJsniMessage.o obj64/LoadJsniMessage.o: LoadJsniMessage.cpp LoadJsniMessage.h Message.h \
  BrowserChannel.h HostChannel.h Debug.h Platform.h DebugLevel.h Socket.h \
  AllowedConnections.h ReturnMessage.h Value.h SessionHandler.h
obj32/InvokeSpecialMessage.o obj64/InvokeSpecialMessage.o: InvokeSpecialMessage.cpp InvokeSpecialMessage.h \
  Message.h BrowserChannel.h SessionHandler.h Value.h Debug.h Platform.h \
  DebugLevel.h HostChannel.h Socket.h AllowedConnections.h \
  ReturnMessage.h scoped_ptr/scoped_ptr.h
obj32/FreeValueMessage.o obj64/FreeValueMessage.o: FreeValueMessage.cpp FreeValueMessage.h Message.h \
  BrowserChannel.h HostChannel.h Debug.h Platform.h DebugLevel.h Socket.h \
  AllowedConnections.h ReturnMessage.h Value.h SessionHandler.h \
  scoped_ptr/scoped_ptr.h
