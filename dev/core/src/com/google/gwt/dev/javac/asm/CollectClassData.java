/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.dev.javac.asm;

import com.google.gwt.dev.asm.AnnotationVisitor;
import com.google.gwt.dev.asm.FieldVisitor;
import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.commons.EmptyVisitor;
import com.google.gwt.dev.util.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the bytecode for a class and collects data needed for building
 * TypeOracle structures.
 */
public class CollectClassData extends EmptyVisitor {

  /**
   * Type of this class.
   */
  public enum ClassType {
    /**
     * A top level class named the same as its source file.
     */
    TopLevel,

    /**
     * A non-static named class nested inside another class.
     */
    Inner {
      @Override
      public boolean hasHiddenConstructorArg() {
        return true;
      }
    },

    /**
     * A static nested class inside another class.
     */
    Nested,

    /**
     * An anonymous inner class.
     */
    Anonymous {
      @Override
      public boolean hasOuter() {
        return true;
      }

      @Override
      public boolean isLocal() {
        return true;
      }
    },

    /**
     * A named class defined inside a method.
     */
    Local {
      @Override
      public boolean hasHiddenConstructorArg() {
        return true;
      }

      @Override
      public boolean hasOuter() {
        return true;
      }

      @Override
      public boolean isLocal() {
        return true;
      }
    };

    public boolean hasHiddenConstructorArg() {
      return false;
    }

    public boolean hasOuter() {
      return false;
    }

    public boolean isLocal() {
      return false;
    }
  }

  /**
   * Holds the descriptor and value for an Enum-valued annotation.
   */
  public static class AnnotationEnum {
    private final String desc;
    private final String value;

    public AnnotationEnum(String desc, String value) {
      this.desc = desc;
      this.value = value;
    }

    /**
     * @return the descriptor
     */
    public String getDesc() {
      return desc;
    }

    /**
     * @return the value
     */
    public String getValue() {
      return value;
    }
  }

  private List<CollectAnnotationData> annotations = new ArrayList<CollectAnnotationData>();

  private String source = null;
  
  // internal name
  private String name;

  private String signature;

  // internal name of superclass
  private String superName;

  // internal names of interfaces
  private String[] interfaces;
  private byte[] bytes;
  private List<CollectMethodData> methods = new ArrayList<CollectMethodData>();
  private List<CollectFieldData> fields = new ArrayList<CollectFieldData>();
  private int access;
  private String outerClass;
  private String outerMethodName;
  private String outerMethodDesc;
  private CollectClassData.ClassType classType = ClassType.TopLevel;

  public CollectClassData(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * @return the access
   */
  public int getAccess() {
    return access;
  }

  /**
   * @return annotations on this class
   */
  public List<CollectAnnotationData> getAnnotations() {
    return annotations;
  }

  /**
   * @return the bytes
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * @return the class type.
   */
  public CollectClassData.ClassType getClassType() {
    return classType;
  }

  /**
   * @return the fields
   */
  public List<CollectFieldData> getFields() {
    return fields;
  }

//  /**
//   * @return the innerClasses
//   */
//  public List<Resource> getInnerClasses() {
//    return innerClasses;
//  }

  /**
   * @return the interfaces
   */
  public String[] getInterfaces() {
    return interfaces;
  }

  /**
   * @return the methods
   */
  public List<CollectMethodData> getMethods() {
    return methods;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the outerClass
   */
  public String getOuterClass() {
    return outerClass;
  }

  /**
   * @return the outerMethodDesc
   */
  public String getOuterMethodDesc() {
    return outerMethodDesc;
  }

  /**
   * @return the outerMethodName
   */
  public String getOuterMethodName() {
    return outerMethodName;
  }

  /**
   * @return the signature
   */
  public String getSignature() {
    return signature;
  }

  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * @return the superName
   */
  public String getSuperName() {
    return superName;
  }

  public boolean hasNoExternalName() {
    return classType == ClassType.Anonymous || classType == ClassType.Local;
  }

  public boolean isAnonymous() {
    return classType == ClassType.Anonymous;
  }

  public boolean isLocal() {
    if (name.matches("\\$\\d") && !isAnonymous()) {
      throw new IllegalStateException("Not anonymous with name of " + name);
    }
    return classType.isLocal();
  }

  @Override
  public String toString() {
    return "class " + name;
  }

  /**
   * Called at the beginning of visiting the class.
   * 
   * @param version classfile version (ie, Opcodes.V1_5 etc)
   * @param access access flags (ie, bitwise or of Opcodes.ACC_*)
   * @param name internal name of this class (ie, com/google/Foo)
   * @param signature generic signature or null
   * @param superName binary name of superclass (ie, java/lang/Object)
   * @param interfaces array of binary names of implemented interfaces
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    this.access = access;
    assert Name.isInternalName(name);
    this.name = name;
    this.signature = signature;
    this.superName = superName;
    this.interfaces = interfaces;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    CollectAnnotationData av = new CollectAnnotationData(desc,
        visible);
    annotations.add(av);
    return av;
  }

  /**
   * Called for each field.
   * 
   * @param access access flags for field
   * @param name field name
   * @param desc type descriptor (ie, Ljava/lang/String;)
   * @param signature generic signature (null if not generic)
   * @param value initialized value if constant
   */
  @Override
  public FieldVisitor visitField(int access, String name, String desc,
      String signature, Object value) {
    if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      // if ("this$1".equals(name) && classType == ClassType.Anonymous) {
      // // TODO(jat): !!! really nasty hack
      // classType = ClassType.Inner;
      // }
      // skip synthetic fields
      return null;
    }
    CollectFieldData fv = new CollectFieldData(access, name, desc,
        signature, value);
    fields.add(fv);
    return fv;
  }

  /**
   * Called once for every inner class of this class.
   * 
   * @param name internal name of inner class (ie, com/google/Foo$1)
   * @param outerName internal name of enclosing class (null if not a member
   *          class or anonymous)
   * @param innerName simple name of the inner class (null if anonymous)
   * @param access access flags (bitwise or of Opcodes.ACC_*) as declared in the
   *          enclosing class
   */
  @Override
  public void visitInnerClass(String name, String outerName, String innerName,
      int access) {
    // If this inner class is ourselves, merge the access flags, since
    // static, for example, only appears in the InnerClass attribute.
    if (this.name.equals(name)) {
      if (outerName != null) {
        outerClass = outerName;
      }
      // TODO(jat): should we only pull in a subset of these flags? Use only
      // these flags, or what? For now, just grab ACC_STATIC and ACC_PRIVATE
      int copyFlags = access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE);
      this.access |= copyFlags;
      boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
      switch (classType) {
        case TopLevel:
          classType = isStatic ? ClassType.Nested : ClassType.Inner;
          break;
        case Anonymous:
          if (innerName != null) {
            classType = ClassType.Local;
          }
          break;
        case Inner:
          // Already marked as inner class by the synthetic this$1 field
          break;
        default:
          throw new IllegalStateException("Unexpected INNERCLASS with type of "
              + classType);
      }
    }
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      // skip synthetic methods
      return null;
    }
    CollectMethodData mv = new CollectMethodData(classType,
        access, name, desc, signature, exceptions);
    methods.add(mv);
    return mv;
  }

  @Override
  public void visitOuterClass(String owner, String name, String desc) {
    this.outerClass = owner;
    this.outerMethodName = name;
    this.outerMethodDesc = desc;
    classType = ClassType.Anonymous; // Could be Local, catch that later
  }

  /**
   * If compiled with debug, visit the source information.
   * 
   * @param source unqualified filename containing source (ie, Foo.java)
   * @param debug additional debug information (may be null)
   */
  @Override
  public void visitSource(String source, String debug) {
    this.source = source;
  }
}