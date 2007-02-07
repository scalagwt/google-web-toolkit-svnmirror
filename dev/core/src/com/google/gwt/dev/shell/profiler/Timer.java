/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.dev.shell.profiler;

import java.lang.reflect.Method;

/**
 * A temporary implementation of a high-precision timer. Currently GWT
 * supports 1.4, but we're planning on migrating to 1.5 at some point.
 * At that time , we should remove this class and replace calls to it
 * with 1.5's System.nanoTime();
 *
 * This class is forward-compatible, in that it will automatically use
 * System.nanoTime if it is executing in a 1.5 JVM.
 *
 */
public class Timer {

  // Using reflection for the TimerImpl classes is pretty horrid.
  // It probably adds some microseconds to the invocation.
  // A long-term solution would be to compile them with the appropriate compilers
  // in the build, but... the entire plan is to get rid of these Timers
  // anyway after the migration to Java 1.5.

  static class FallbackTimer implements TimerImpl {
    public long nanoTime() {
      return System.currentTimeMillis();
    }
  }

  static class SunPerfTimer implements TimerImpl {
    private static Class perfClass;
    private static final Object timer = getTimer();
    private static final double frequency = invokeMethod( getMethod( perfClass, "highResFrequency" ), timer ); // cycles per second
    private static final Method highResCounter = getMethod( perfClass, "highResCounter" );
    private static final long initCounter = invokeMethod( highResCounter, timer );
    private static final double nanosPerSecond = 1000000000;

    private static Object getTimer() {
      try {
        perfClass = Class.forName( "sun.misc.Perf" );
        Method getPerf = perfClass.getMethod( "getPerf", new Class[] {} );
        return getPerf.invoke( null, NO_ARGS );
      } catch (Exception e) {
        throw new RuntimeException( "Unable to locate sun.misc.Perf.getPerf()", e );
      }
    }

    public long nanoTime() {
      // TODO This probably doesn't behave well when the counter wraps
      double elapsedSeconds = (invokeMethod(highResCounter, timer) - initCounter) / frequency;
      return ( long ) ( elapsedSeconds * nanosPerSecond );
    }
  }

  static class Timer15 implements TimerImpl {
    private static Method method = getMethod( System.class, "nanoTime" );

    public long nanoTime() {
      return invokeMethod( method, null );
    }
  }

  interface TimerImpl {
    public long nanoTime();
  }

  private static TimerImpl impl = getTimer();

  private static final Object[] NO_ARGS = new Object[] {};

  /**
   * A high-precision timer value. Follows the same contract as System.nanoTime.
   *
   * @return a possibly negative timing value.
   */
  public static long nanoTime() {
    return impl.nanoTime();
  }

  // A utility method to get a method by reflection that should always exist
  private static Method getMethod( Class cls, String name ) {
    try {
      return cls.getMethod( name, new Class[] {} );
    } catch (Exception e) {
      throw new RuntimeException( "Unable to locate " + cls.getName() + "." + name, e );
    }
  }

  private static TimerImpl getTimer() {
    String vmVersion = System.getProperty( "java.specification.version" );
    int subVersion = Integer.parseInt( vmVersion.split( "\\." )[ 1 ] );

    if ( subVersion > 5 ) {
      return new Timer15();
    } else {
      try {
        Class.forName( "sun.misc.Perf" );
        return new SunPerfTimer();
      } catch ( Exception e ) {
        System.out.println( "Warning - GWT has fallen back to a low performance timer, " +
            "because neither Java 1.5 nor Sun's 1.4 high-res timers are available." );
        return new FallbackTimer();
      }
    }
  }

  // A utility method for invoking a method by reflection that takes no args and
  // returns a long
  private static long invokeMethod( Method method, Object obj ) {
    try {
      Long value = (Long) method.invoke( obj, NO_ARGS );
      return value.longValue();
    } catch (Exception e) {
      // Should NEVER happen
      throw new RuntimeException( "Unable to invoke " + method.getName(), e );
    }
  }
}
