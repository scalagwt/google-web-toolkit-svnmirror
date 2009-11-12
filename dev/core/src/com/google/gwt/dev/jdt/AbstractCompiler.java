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
package com.google.gwt.dev.jdt;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.HelpInfo;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.javac.CompilationUnit;
import com.google.gwt.dev.javac.GWTProblem;
import com.google.gwt.dev.javac.JdtCompiler;
import com.google.gwt.dev.javac.Shared;
import com.google.gwt.dev.util.CharArrayComparator;
import com.google.gwt.dev.util.Empty;
import com.google.gwt.dev.util.PerfLogger;
import com.google.gwt.dev.util.Util;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A facade around the JDT compiler to manage on-demand compilation, caching
 * smartly where possible.
 */
public abstract class AbstractCompiler {

  /**
   * Adapts a {@link CompilationUnit} for a JDT compile.
   */
  public static class CompilationUnitAdapter implements ICompilationUnit {

    private final CompilationUnit unit;

    public CompilationUnitAdapter(CompilationUnit unit) {
      this.unit = unit;
    }

    @SuppressWarnings("deprecation")
    public char[] getContents() {
      return unit.getSource().toCharArray();
    }

    public char[] getFileName() {
      return unit.getDisplayLocation().toCharArray();
    }

    public char[] getMainTypeName() {
      return Shared.getShortName(unit.getTypeName()).toCharArray();
    }

    public char[][] getPackageName() {
      String packageName = Shared.getPackageName(unit.getTypeName());
      return CharOperation.splitOn('.', packageName.toCharArray());
    }

    @Override
    public String toString() {
      return unit.toString();
    }
  }

  /**
   * Shields {@link AbstractCompiler} so it can be garbage collected at the end
   * of a compile.
   */
  private static class Sandbox {

    /**
     * Adapted to hook the processing of compilation unit declarations so as to
     * be able to add additional compilation units based on the results of
     * previously-compiled ones. Examples of cases where this is useful include
     * classes referenced only from JSNI and <code>GWT.create</code>.
     */
    private class CompilerImpl extends Compiler {

      private Set<CompilationUnitDeclaration> cuds;
      private long jdtProcessNanos;

      public CompilerImpl(INameEnvironment environment,
          IErrorHandlingPolicy policy, CompilerOptions compilerOptions,
          ICompilerRequestor requestor, IProblemFactory problemFactory) {
        super(environment, policy, compilerOptions, requestor, problemFactory);
      }

      @Override
      public void compile(ICompilationUnit[] sourceUnits) {
        jdtProcessNanos = 0;
        super.compile(sourceUnits);
        PerfLogger.log("AbstractCompiler.compile, time spent in JDT process callback: "
            + (jdtProcessNanos / 1000000) + "ms");
        cuds = null;
      }

      @Override
      public void process(CompilationUnitDeclaration unit, int index) {

        long processBeginNanos = System.nanoTime();

        // The following block of code is a copy of super.process(cud, index),
        // with the modification that cud.generateCode is conditionally called
        // based on doGenerateBytes
        {
          this.lookupEnvironment.unitBeingCompleted = unit;
          long parseStart = System.currentTimeMillis();

          this.parser.getMethodBodies(unit);

          long resolveStart = System.currentTimeMillis();
          this.stats.parseTime += resolveStart - parseStart;

          // fault in fields & methods
          if (unit.scope != null) {
            unit.scope.faultInTypes();
          }

          // verify inherited methods
          if (unit.scope != null) {
            unit.scope.verifyMethods(lookupEnvironment.methodVerifier());
          }

          // type checking
          unit.resolve();

          long analyzeStart = System.currentTimeMillis();
          this.stats.resolveTime += analyzeStart - resolveStart;

          // flow analysis
          unit.analyseCode();

          long generateStart = System.currentTimeMillis();
          this.stats.analyzeTime += generateStart - analyzeStart;

          // code generation
          // code generation
          if (doGenerateBytes) {
            unit.generateCode();
          }

          // reference info
          if (options.produceReferenceInfo && unit.scope != null) {
            unit.scope.storeDependencyInfo();
          }

          // finalize problems (suppressWarnings)
          unit.finalizeProblems();

          this.stats.generateTime += System.currentTimeMillis() - generateStart;

          // refresh the total number of units known at this stage
          unit.compilationResult.totalUnitsKnown = totalUnits;

          this.lookupEnvironment.unitBeingCompleted = null;
        }

        ICompilationUnit cu = unit.compilationResult.compilationUnit;
        String loc = String.valueOf(cu.getFileName());
        TreeLogger branch = logger.branch(TreeLogger.SPAM,
            "Scanning for additional dependencies: " + loc, null);

        // Examine the cud for magic types.
        //
        String[] typeNames = outer.doFindAdditionalTypesUsingJsni(branch, unit);
        addAdditionalTypes(branch, typeNames);

        typeNames = outer.doFindAdditionalTypesUsingRebinds(branch, unit);
        addAdditionalTypes(branch, typeNames);

        typeNames = outer.doFindAdditionalTypesUsingArtificialRescues(branch,
            unit);
        addAdditionalTypes(branch, typeNames);

        // Optionally remember this cud.
        //
        if (cuds != null) {
          cuds.add(unit);
        }

        jdtProcessNanos += System.nanoTime() - processBeginNanos;
      }

      /**
       * Helper method for process() that receives the types found by magic.
       * This causes the compiler to find the additional type, possibly winding
       * its back to ask for the compilation unit from the source oracle.
       */
      private void addAdditionalTypes(TreeLogger logger, String[] typeNames) {
        for (int i = 0; i < typeNames.length; i++) {
          String typeName = typeNames[i];
          final String msg = "Need additional type '" + typeName + "'";
          logger.log(TreeLogger.SPAM, msg, null);

          resolvePossiblyNestedType(typeName);
        }
      }

      private void compile(ICompilationUnit[] units,
          Set<CompilationUnitDeclaration> cuds) {
        this.cuds = cuds;
        compile(units);
      }

      private ReferenceBinding resolvePossiblyNestedType(String typeName) {
        return JdtCompiler.resolveType(lookupEnvironment, typeName);
      }
    }

    private class ICompilerRequestorImpl implements ICompilerRequestor {

      public ICompilerRequestorImpl() {
      }

      public void acceptResult(CompilationResult result) {
        // Handle compilation errors.
        //
        IProblem[] errors = result.getErrors();

        if (errors != null && errors.length > 0) {
          // Dump it to disk.
          //
          String fn = String.valueOf(result.compilationUnit.getFileName());
          String msg = "Errors in '" + fn + "'";
          TreeLogger branch = logger.branch(TreeLogger.ERROR, msg, null);

          for (int i = 0; i < errors.length; i++) {
            IProblem error = errors[i];

            // Strip the initial code from each error.
            //
            msg = error.toString();
            msg = msg.substring(msg.indexOf(' '));

            // Append 'Line #: msg' to the error message.
            //
            StringBuffer msgBuf = new StringBuffer();
            int line = error.getSourceLineNumber();
            if (line > 0) {
              msgBuf.append("Line ");
              msgBuf.append(line);
              msgBuf.append(": ");
            }
            msgBuf.append(msg);

            HelpInfo helpInfo = null;
            if (error instanceof GWTProblem) {
              GWTProblem gwtProblem = (GWTProblem) error;
              helpInfo = gwtProblem.getHelpInfo();
            }
            branch.log(TreeLogger.ERROR, msgBuf.toString(), null, helpInfo);
          }
        }
      }
    }

    private class INameEnvironmentImpl implements INameEnvironment {

      public INameEnvironmentImpl() {
      }

      public void cleanup() {
        // intentionally blank
      }

      public NameEnvironmentAnswer findType(char[] type, char[][] pkg) {
        return findType(CharOperation.arrayConcat(pkg, type));
      }

      public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
        String qname = CharOperation.toString(compoundTypeName);
        TreeLogger branch = logger.branch(TreeLogger.SPAM,
            "Compiler is asking about '" + qname + "'", null);

        if (isPackage(qname)) {
          branch.log(TreeLogger.SPAM, "Found to be a package", null);
          return null;
        }

        // Didn't find it in the cache, so let's compile from source.
        // Strip off the inner types, if any
        //
        String className = qname;
        int pos = qname.indexOf('$');
        if (pos >= 0) {
          qname = qname.substring(0, pos);
        }
        CompilationUnit unit = findCompilationUnit(qname);
        if (unit != null) {
          branch.log(TreeLogger.SPAM, "Found type in compilation unit: "
              + unit.getDisplayLocation());
          ICompilationUnit icu = new CompilationUnitAdapter(unit);
          return new NameEnvironmentAnswer(icu, null);
        } else {
          ClassLoader classLoader = getClassLoader();
          URL resourceURL = classLoader.getResource(className.replace('.', '/')
              + ".class");
          if (resourceURL != null) {
            /*
             * We know that there is a .class file that matches the name that we
             * are looking for. However, at least on OSX, this lookup is case
             * insensitive so we need to use Class.forName to effectively verify
             * the case.
             */
            if (isBinaryType(classLoader, className)) {
              byte[] classBytes = Util.readURLAsBytes(resourceURL);
              ClassFileReader cfr;
              try {
                cfr = new ClassFileReader(classBytes, null);
                return new NameEnvironmentAnswer(cfr, null);
              } catch (ClassFormatException e) {
                // Ignored.
              }
            }
          }

          branch.log(TreeLogger.SPAM, "Not a known type", null);
          return null;
        }
      }

      public boolean isPackage(char[][] parentPkg, char[] pkg) {
        // In special cases where class bytes are asserted from the outside,
        // a package can exist that the host doesn't know about. We have to
        // do a special check for these cases.
        //
        final char[] pathChars = CharOperation.concatWith(parentPkg, pkg, '.');
        String packageName = String.valueOf(pathChars);
        if (isPackage(packageName)) {
          return true;
        } else if (isPackage(getClassLoader(), packageName)) {
          // Grow our own list to spare calls into the host.
          //
          rememberPackage(packageName);
          return true;
        } else {
          return false;
        }
      }

      private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
      }

      private boolean isBinaryType(ClassLoader classLoader, String typeName) {
        try {
          Class.forName(typeName, false, classLoader);
          return true;
        } catch (ClassNotFoundException e) {
          // Ignored.
        } catch (LinkageError e) {
          // Ignored.
        }

        // Assume that it is not a binary type.
        return false;
      }

      private boolean isPackage(ClassLoader classLoader, String packageName) {
        String packageAsPath = packageName.replace('.', '/');
        return classLoader.getResource(packageAsPath) != null;
      }

      private boolean isPackage(String packageName) {
        return knownPackages.contains(packageName);
      }
    }

    final CompilerImpl compiler;
    final boolean doGenerateBytes;
    final Set<String> knownPackages = new HashSet<String>();
    TreeLogger logger = null;
    AbstractCompiler outer;

    Sandbox(AbstractCompiler outer, boolean doGenerateBytes) {
      this.outer = outer;
      this.doGenerateBytes = doGenerateBytes;
      rememberPackage("");

      INameEnvironment env = new INameEnvironmentImpl();
      IErrorHandlingPolicy pol = DefaultErrorHandlingPolicies.proceedWithAllProblems();
      IProblemFactory probFact = new DefaultProblemFactory(Locale.getDefault());
      ICompilerRequestor req = new ICompilerRequestorImpl();
      CompilerOptions options = getCompilerOptions();

      // This is only needed by TypeOracleBuilder to parse metadata.
      options.docCommentSupport = false;

      compiler = new CompilerImpl(env, pol, options, req, probFact);

      // Initialize the packages list.
      for (CompilationUnit unit : outer.compilationState.getCompilationUnits()) {
        String packageName = Shared.getPackageName(unit.getTypeName());
        rememberPackage(packageName);
      }
    }

    public void clear() {
      outer = null;
      logger = null;
    }

    private CompilationUnit findCompilationUnit(String qname) {
      // Build the initial set of compilation units.
      Map<String, CompilationUnit> unitMap = outer.compilationState.getCompilationUnitMap();
      CompilationUnit unit = unitMap.get(qname);
      while (unit == null) {
        int pos = qname.lastIndexOf('.');
        if (pos < 0) {
          return null;
        }
        qname = qname.substring(0, pos);
        unit = unitMap.get(qname);
      }
      return unit;
    }

    /**
     * Causes the compilation service itself to recognize the specified package
     * name (and all its parent packages), avoiding a call back into the host.
     * This is useful as an optimization, but more importantly, it is useful to
     * compile against bytecode that was pre-compiled to which we don't have the
     * source. This ability is crucial bridging the gap between user-level and
     * "dev" code in hosted mode for classes such as JavaScriptHost and
     * ShellJavaScriptHost.
     */
    private void rememberPackage(String packageName) {
      int i = packageName.lastIndexOf('.');
      if (i != -1) {
        // Ensure the parent package is also created.
        //
        rememberPackage(packageName.substring(0, i));
      }
      knownPackages.add(packageName);
    }
  }

  private static final Comparator<CompilationUnitDeclaration> CUD_COMPARATOR = new Comparator<CompilationUnitDeclaration>() {

    public int compare(CompilationUnitDeclaration cud1,
        CompilationUnitDeclaration cud2) {
      ICompilationUnit cu1 = cud1.compilationResult().getCompilationUnit();
      ICompilationUnit cu2 = cud2.compilationResult().getCompilationUnit();
      char[][] package1 = cu1.getPackageName();
      char[][] package2 = cu2.getPackageName();
      for (int i = 0, c = Math.min(package1.length, package2.length); i < c; ++i) {
        int result = CharArrayComparator.INSTANCE.compare(package1[i],
            package2[i]);
        if (result != 0) {
          return result;
        }
      }
      int result = package2.length - package1.length;
      if (result != 0) {
        return result;
      }
      return CharArrayComparator.INSTANCE.compare(cu1.getMainTypeName(),
          cu2.getMainTypeName());
    }
  };

  public static CompilerOptions getCompilerOptions() {
    CompilerOptions options = JdtCompiler.getCompilerOptions();

    // Turn off all debugging for web mode.
    options.produceDebugAttributes = 0;
    options.preserveAllLocalVariables = false;
    return options;
  }

  private Sandbox sandbox;
  protected CompilationState compilationState;

  protected AbstractCompiler(CompilationState compilationState,
      boolean doGenerateBytes) {
    this.compilationState = compilationState;
    this.sandbox = new Sandbox(this, doGenerateBytes);
  }

  protected final CompilationUnitDeclaration[] compile(TreeLogger logger,
      ICompilationUnit[] units) {

    // Any additional compilation units that are found to be needed will be
    // pulled in while procssing compilation units. See CompilerImpl.process().
    //
    sandbox.logger = logger;
    try {
      Set<CompilationUnitDeclaration> cuds = new TreeSet<CompilationUnitDeclaration>(
          CUD_COMPARATOR);
      sandbox.compiler.compile(units, cuds);
      int size = cuds.size();
      CompilationUnitDeclaration[] cudArray = new CompilationUnitDeclaration[size];
      return cuds.toArray(cudArray);
    } finally {
      sandbox.clear();
      sandbox = null;
    }
  }

  @SuppressWarnings("unused")
  // overrider may use unused parameter
  protected String[] doFindAdditionalTypesUsingArtificialRescues(
      TreeLogger logger, CompilationUnitDeclaration cud) {
    return Empty.STRINGS;
  }

  @SuppressWarnings("unused")
  // overrider may use unused parameter
  protected String[] doFindAdditionalTypesUsingJsni(TreeLogger logger,
      CompilationUnitDeclaration cud) {
    return Empty.STRINGS;
  }

  @SuppressWarnings("unused")
  // overrider may use unused parameter
  protected String[] doFindAdditionalTypesUsingRebinds(TreeLogger logger,
      CompilationUnitDeclaration cud) {
    return Empty.STRINGS;
  }

  protected final ReferenceBinding resolvePossiblyNestedType(String typeName) {
    return sandbox.compiler.resolvePossiblyNestedType(typeName);
  }
}
