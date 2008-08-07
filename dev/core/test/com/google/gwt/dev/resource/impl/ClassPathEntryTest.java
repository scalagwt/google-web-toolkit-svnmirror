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
package com.google.gwt.dev.resource.impl;

import com.google.gwt.core.ext.TreeLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class ClassPathEntryTest extends AbstractResourceOrientedTestBase {

  public void testAllCpe1FilesFound() throws URISyntaxException, IOException {
    testAllCpe1FilesFound(getClassPathEntry1AsJar());
    testAllCpe1FilesFound(getClassPathEntry1AsDirectory());
  }

  public void testAllCpe2FilesFound() throws URISyntaxException, IOException {
    testAllCpe2FilesFound(getClassPathEntry2AsJar());
    testAllCpe2FilesFound(getClassPathEntry2AsDirectory());
  }

  public void testPathPrefixSetChanges() throws IOException, URISyntaxException {
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe1dir = getClassPathEntry1AsDirectory();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();
    ClassPathEntry cpe2dir = getClassPathEntry2AsDirectory();

    testPathPrefixSetChanges(cpe1jar, cpe2jar);
    testPathPrefixSetChanges(cpe1dir, cpe2dir);
    testPathPrefixSetChanges(cpe1jar, cpe2dir);
    testPathPrefixSetChanges(cpe1dir, cpe2jar);
  }

  public void testUseOfPrefixesWithFiltering() throws IOException,
      URISyntaxException {
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe1dir = getClassPathEntry1AsDirectory();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();
    ClassPathEntry cpe2dir = getClassPathEntry2AsDirectory();

    testUseOfPrefixesWithFiltering(cpe1jar, cpe2jar);
    testUseOfPrefixesWithFiltering(cpe1dir, cpe2dir);
    testUseOfPrefixesWithFiltering(cpe1jar, cpe2dir);
    testUseOfPrefixesWithFiltering(cpe1dir, cpe2jar);
  }

  public void testUseOfPrefixesWithoutFiltering() throws URISyntaxException,
      IOException {
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe1dir = getClassPathEntry1AsDirectory();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();
    ClassPathEntry cpe2dir = getClassPathEntry2AsDirectory();

    testUseOfPrefixesWithoutFiltering(cpe1jar, cpe2jar);
    testUseOfPrefixesWithoutFiltering(cpe1dir, cpe2dir);
    testUseOfPrefixesWithoutFiltering(cpe1jar, cpe2dir);
    testUseOfPrefixesWithoutFiltering(cpe1dir, cpe2jar);
  }

  public void testUseOfPrefixesWithoutFiltering(ClassPathEntry cpe1,
      ClassPathEntry cpe2) {
    TreeLogger logger = createTestTreeLogger();

    PathPrefixSet pps = new PathPrefixSet();
    pps.add(new PathPrefix("com/google/gwt/user/client/", null));
    pps.add(new PathPrefix("com/google/gwt/i18n/client/", null));

    {
      // Examine cpe1.
      Set<AbstractResource> r = cpe1.findApplicableResources(logger, pps);

      assertEquals(3, r.size());
      assertPathIncluded(r, "com/google/gwt/user/client/_command.gava");
      assertPathIncluded(r, "com/google/gwt/user/client/_timer.gava");
      assertPathIncluded(r, "com/google/gwt/user/client/ui/_widget.gava");
    }

    {
      // Examine cpe2.
      Set<AbstractResource> r = cpe2.findApplicableResources(logger, pps);

      assertEquals(1, r.size());
      assertPathIncluded(r, "com/google/gwt/i18n/client/_messages.gava");
    }
  }

  // NOTE: if this test fails, ensure that the source root containing this very
  // source file is *FIRST* on the classpath
  private void testAllCpe1FilesFound(ClassPathEntry cpe1) {
    TreeLogger logger = createTestTreeLogger();

    PathPrefixSet pps = new PathPrefixSet();
    pps.add(new PathPrefix("", null));

    Set<AbstractResource> r = cpe1.findApplicableResources(logger, pps);

    assertEquals(9, r.size());
    assertPathIncluded(r, "com/google/gwt/user/_user.twit.xml");
    assertPathIncluded(r, "com/google/gwt/user/client/_command.gava");
    assertPathIncluded(r, "com/google/gwt/user/client/_timer.gava");
    assertPathIncluded(r, "com/google/gwt/user/client/ui/_widget.gava");
    assertPathIncluded(r, "org/example/bar/client/BarClient1.txt");
    assertPathIncluded(r, "org/example/bar/client/BarClient2.txt");
    assertPathIncluded(r, "org/example/bar/client/etc/BarEtc.txt");
    assertPathIncluded(r, "org/example/foo/client/_foo_client.gava");
    assertPathIncluded(r, "org/example/foo/server/_foo_server.gava");
  }

  // NOTE: if this test fails, ensure that the source root containing this very
  // source file is on the classpath
  private void testAllCpe2FilesFound(ClassPathEntry cpe2) {
    TreeLogger logger = createTestTreeLogger();

    PathPrefixSet pps = new PathPrefixSet();
    pps.add(new PathPrefix("", null));
    Set<AbstractResource> r = cpe2.findApplicableResources(logger, pps);

    assertEquals(5, r.size());
    assertPathIncluded(r, "com/google/gwt/i18n/_i18n.twit.xml");
    assertPathIncluded(r, "com/google/gwt/i18n/client/_messages.gava");
    assertPathIncluded(r,
        "com/google/gwt/i18n/rebind/_localizable_generator.gava");
    assertPathIncluded(r, "org/example/bar/client/BarClient2.txt");
    assertPathIncluded(r, "org/example/bar/client/BarClient3.txt");
  }

  private void testPathPrefixSetChanges(ClassPathEntry cpe1, ClassPathEntry cpe2) {
    TreeLogger logger = createTestTreeLogger();

    {
      // Filter is not set yet.
      PathPrefixSet pps = new PathPrefixSet();
      pps.add(new PathPrefix("com/google/gwt/user/", null));
      pps.add(new PathPrefix("com/google/gwt/i18n/", null));

      // Examine cpe1 in the absence of the filter.
      Set<AbstractResource> r1 = cpe1.findApplicableResources(logger, pps);

      assertEquals(4, r1.size());
      assertPathIncluded(r1, "com/google/gwt/user/_user.twit.xml");
      assertPathIncluded(r1, "com/google/gwt/user/client/_command.gava");
      assertPathIncluded(r1, "com/google/gwt/user/client/_timer.gava");
      assertPathIncluded(r1, "com/google/gwt/user/client/ui/_widget.gava");

      // Examine cpe2 in the absence of the filter.
      Set<AbstractResource> r2 = cpe2.findApplicableResources(logger, pps);

      assertEquals(3, r2.size());
      assertPathIncluded(r2, "com/google/gwt/i18n/_i18n.twit.xml");
      assertPathIncluded(r2, "com/google/gwt/i18n/client/_messages.gava");
      assertPathIncluded(r2,
          "com/google/gwt/i18n/rebind/_localizable_generator.gava");
    }

    {
      // Create a pps with a filter.
      ResourceFilter excludeXmlFiles = new ResourceFilter() {
        public boolean allows(String path) {
          return !path.endsWith(".xml");
        }
      };

      PathPrefixSet pps = new PathPrefixSet();
      pps.add(new PathPrefix("com/google/gwt/user/", excludeXmlFiles));
      pps.add(new PathPrefix("com/google/gwt/i18n/", excludeXmlFiles));

      // Examine cpe1 in the presence of the filter.
      Set<AbstractResource> r1 = cpe1.findApplicableResources(logger, pps);

      assertEquals(3, r1.size());
      assertPathNotIncluded(r1, "com/google/gwt/user/_user.twit.xml");
      assertPathIncluded(r1, "com/google/gwt/user/client/_command.gava");
      assertPathIncluded(r1, "com/google/gwt/user/client/_timer.gava");
      assertPathIncluded(r1, "com/google/gwt/user/client/ui/_widget.gava");

      // Examine cpe2 in the presence of the filter.
      Set<AbstractResource> r2 = cpe2.findApplicableResources(logger, pps);

      assertEquals(2, r2.size());
      assertPathNotIncluded(r2, "com/google/gwt/i18n/_i18n.twit.xml");
      assertPathIncluded(r2, "com/google/gwt/i18n/client/_messages.gava");
      assertPathIncluded(r2,
          "com/google/gwt/i18n/rebind/_localizable_generator.gava");
    }

    {
      /*
       * Change the prefix path set to the zero-lenth prefix (which allows
       * everything), but specify a filter that disallows everything.
       */
      PathPrefixSet pps = new PathPrefixSet();
      pps.add(new PathPrefix("", new ResourceFilter() {
        public boolean allows(String path) {
          // Exclude everything.
          return false;
        }
      }));

      // Examine cpe1 in the presence of the filter.
      Set<AbstractResource> r1 = cpe1.findApplicableResources(logger, pps);

      assertEquals(0, r1.size());

      // Examine cpe2 in the presence of the filter.
      Set<AbstractResource> r2 = cpe2.findApplicableResources(logger, pps);

      assertEquals(0, r2.size());
    }
  }

  private void testUseOfPrefixesWithFiltering(ClassPathEntry cpe1,
      ClassPathEntry cpe2) {
    TreeLogger logger = createTestTreeLogger();

    PathPrefixSet pps = new PathPrefixSet();
    ResourceFilter excludeXmlFiles = new ResourceFilter() {
      public boolean allows(String path) {
        return !path.endsWith(".xml");
      }
    };
    // The prefix is intentionally starting at the module-level, not 'client'.
    pps.add(new PathPrefix("com/google/gwt/user/", excludeXmlFiles));
    pps.add(new PathPrefix("com/google/gwt/i18n/", excludeXmlFiles));

    {
      // Examine cpe1.
      Set<AbstractResource> r = cpe1.findApplicableResources(logger, pps);

      assertEquals(3, r.size());
      // User.gwt.xml would be included but for the filter.
      assertPathNotIncluded(r, "com/google/gwt/user/_user.twit.xml");
      assertPathIncluded(r, "com/google/gwt/user/client/_command.gava");
      assertPathIncluded(r, "com/google/gwt/user/client/_timer.gava");
      assertPathIncluded(r, "com/google/gwt/user/client/ui/_widget.gava");
    }

    {
      // Examine cpe2.
      Set<AbstractResource> r = cpe2.findApplicableResources(logger, pps);

      assertEquals(2, r.size());
      // I18N.gwt.xml would be included but for the filter.
      assertPathNotIncluded(r, "com/google/gwt/i18n/_i18n.twit.xml");
      assertPathIncluded(r, "com/google/gwt/i18n/client/_messages.gava");
      assertPathIncluded(r,
          "com/google/gwt/i18n/rebind/_localizable_generator.gava");
    }
  }

}
