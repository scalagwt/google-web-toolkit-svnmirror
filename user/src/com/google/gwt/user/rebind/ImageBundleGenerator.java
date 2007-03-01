/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.user.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Generates an implementation of a user-defined interface <code>T</code> that
 * extends {@link com.thegwtbook.samples.ch11.client.ImageBundle}.
 * 
 * Each method in <code>T</code> must be declared to return
 * <code>com.google.gwt.user.client.ui.Image</code>, take no parameters, and
 * specify the metadata tag <code>gwt.resource</code> as the name of an image
 * file that can be found on the public path.
 */
public class ImageBundleGenerator extends Generator {

  private static final String CLIPPEDIMAGE_QNAME = "com.google.gwt.user.client.ui.ClippedImage";

  private static final String IMAGEBUNDLE_QNAME = "com.google.gwt.user.client.ImageBundle";

  private static final String METADATA_TAG = "gwt.resource";

  public ImageBundleGenerator() {
  }

  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

    TypeOracle typeOracle = context.getTypeOracle();

    // Get metadata describing the user's class.
    JClassType userType = getValidUserType(logger, typeName, typeOracle);

    // Get the methods that correspond to constituent images.
    JMethod[] imgMethods = getValidImageMethods(logger, userType);

    // Write the new class.
    String resultName = generateImpl(logger, context, userType, imgMethods);

    // Return the complete name of the generated class.
    return resultName;
  }

  private String computeSubclassName(JClassType userType) {
    String baseName = userType.getName().replace('.', '_');
    return baseName + "_generatedBundle";
  }

  private void generateImageMethod(TreeLogger logger, GeneratorContext context,
      ImageBundleBuilder compositeImage, SourceWriter sw, JMethod method)
      throws UnableToCompleteException {

    String imageName = getImageUrlFromMetaData(method);
    String decl = method.getReadableDeclaration(false, true, true, true, true);

    {
      sw.indent();
      sw.print(decl);
      sw.println(" {");

      {
        sw.indent();
        sw.print("return new ClippedImage(IMAGE_BUNDLE_URL, ");
        ImageBundleBuilder.ImageRect imageRect = compositeImage.getMapping(imageName);
        sw.print(Integer.toString(imageRect.left));
        sw.print(", 0, ");
        sw.print(Integer.toString(imageRect.width));
        sw.print(", ");
        sw.print(Integer.toString(imageRect.height));
        sw.println(");");
        sw.outdent();
      }

      sw.println("}");
      sw.outdent();
    }
  }

  private String generateImpl(TreeLogger logger, GeneratorContext context,
      JClassType userType, JMethod[] imageMethods)
      throws UnableToCompleteException {
    // Compute the package and class names of the generated class.
    String pkgName = userType.getPackage().getName();
    String subName = computeSubclassName(userType);

    // Begin writing the generated source.
    ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
        pkgName, subName);
    f.addImport(CLIPPEDIMAGE_QNAME);
    f.addImplementedInterface(userType.getQualifiedSourceName());

    PrintWriter pw = context.tryCreate(logger, pkgName, subName);
    if (pw != null) {
      SourceWriter sw = f.createSourceWriter(context, pw);

      // Build a compound image from each individual image.
      ImageBundleBuilder bulder = new ImageBundleBuilder();

      for (int i = 0; i < imageMethods.length; i++) {
        JMethod method = imageMethods[i];
        String imageUrl = getImageUrlFromMetaData(method);
        assert (imageUrl != null);
        bulder.assimilate(logger, imageUrl);
      }

      // Write the compound image into the output directory.
      String bundledImageUrl = bulder.writeBundledImage(logger, context);

      // Emit a constant for the composite url.
      sw.print("private static final String IMAGE_BUNDLE_URL = \"");
      sw.print(escape(bundledImageUrl));
      sw.println("\";");

      // Generate an implementation of each method.
      for (int i = 0; i < imageMethods.length; i++) {
        JMethod method = imageMethods[i];
        generateImageMethod(logger, context, bulder, sw, method);
      }

      // Finish.
      sw.commit(logger);
    }

    return f.getCreatedClassName();
  }

  // Assume this is only called for valid methods.
  private String getImageUrlFromMetaData(JMethod method) {
    String[][] md = method.getMetaData(METADATA_TAG);
    int lastTagIndex = md.length - 1;
    int lastValueIndex = md[lastTagIndex].length - 1;
    String imageName = md[lastTagIndex][lastValueIndex];

    // Make sure the name is either absolute or package-relative.
    if (imageName.indexOf("/") == -1) {
      String pkgName = method.getEnclosingType().getPackage().getName();
      // This construction handles the default package correctly, too.
      imageName = pkgName.replace('.', '/') + "/" + imageName;
    }
    return imageName;
  }

  private JMethod[] getValidImageMethods(TreeLogger logger, JClassType userType)
      throws UnableToCompleteException {

    logger = logger.branch(TreeLogger.TRACE, "Analyzing methods on "
        + userType.getQualifiedSourceName(), null);

    final JClassType imageClass;
    try {
      imageClass = userType.getOracle().getType(CLIPPEDIMAGE_QNAME);
    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, "GWT Image class is not available", e);
      throw new UnableToCompleteException();
    }

    Map rejectedMethodsAndWhy = new HashMap();
    JMethod[] leafMethods = userType.getAllMethods();
    for (int i = 0; i < leafMethods.length; i++) {
      JMethod method = leafMethods[i];

      if (method.getReturnType() != imageClass) {
        rejectedMethodsAndWhy.put(method, "Return type must be "
            + CLIPPEDIMAGE_QNAME);
        continue;
      }

      if (method.getParameters().length > 0) {
        rejectedMethodsAndWhy.put(method, "Method cannot take parameters");
        continue;
      }

      String[][] md = method.getMetaData(METADATA_TAG);
      if (md.length != 1 || md[0].length != 1) {
        rejectedMethodsAndWhy.put(method,
            "Expecting exactly one metadata tag of the form '@gwt.resource <filename>'");
        continue;
      }
    }

    // Make sure there aren't any invalid methods.
    if (!rejectedMethodsAndWhy.isEmpty()) {
      logger = logger.branch(TreeLogger.ERROR,
          "The following methods are invalid on an image bundle:", null);
      for (Iterator iter = rejectedMethodsAndWhy.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry) iter.next();
        JMethod badMethod = (JMethod) entry.getKey();
        String reason = (String) entry.getValue();
        TreeLogger branch = logger.branch(TreeLogger.ERROR,
            badMethod.getReadableDeclaration(), null);
        branch.log(TreeLogger.ERROR, reason, null);
      }
      throw new UnableToCompleteException();
    }

    return leafMethods;
  }

  private JClassType getValidUserType(TreeLogger logger, String typeName,
      TypeOracle typeOracle) throws UnableToCompleteException {
    try {
      // Get the type that the user is introducing.
      JClassType userType = typeOracle.getType(typeName);

      // Get the type this generator is designed to support.
      JClassType magicType = typeOracle.findType(IMAGEBUNDLE_QNAME);

      // Ensure it's an interface.
      if (userType.isInterface() == null) {
        logger.log(TreeLogger.ERROR, userType.getQualifiedSourceName()
            + " must be an interface", null);
        throw new UnableToCompleteException();
      }

      // Ensure proper derivation.
      if (!userType.isAssignableTo(magicType)) {
        logger.log(TreeLogger.ERROR, userType.getQualifiedSourceName()
            + " must be assignable to " + magicType.getQualifiedSourceName(),
            null);
        throw new UnableToCompleteException();
      }

      return userType;

    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, "Unable to find required type(s)", e);
      throw new UnableToCompleteException();
    }
  }

}
