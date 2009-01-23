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
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.util.tools.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests {@link ResourceOracleImpl}.
 * 
 * <pre>
 * Important states to test:
 * - No class path entries
 * - A single class path entries
 *   - init/add/update/remove
 * - Multiple class path entries
 *   - init/add/update/remove
 *   - same as previous with shadowing
 *   - same as previous with superceding
 * </pre>
 */
public class ResourceOracleImplTest extends AbstractResourceOrientedTestBase {

  // Starts empty but can change during tests.
  private static class MOCK_CPE0 extends MockClassPathEntry {
    public MOCK_CPE0() {
      super("/cpe0/");
    }
  }

  private static class MOCK_CPE3 extends MockClassPathEntry {
    public MOCK_CPE3() {
      super("/cpe3/");
    }
  }

  private static class ResourceOracleSnapshot {
    private final Set<String> pathNames;
    private final Map<String, Resource> resourceMap;
    private final Set<Resource> resources;

    public ResourceOracleSnapshot(ResourceOracleImpl oracle) {
      resources = oracle.getResources();
      resourceMap = oracle.getResourceMap();
      pathNames = oracle.getPathNames();
    }

    public void assertCollectionsConsistent(int expectedSize) {
      assertEquals(expectedSize, resources.size());
      assertEquals(resources.size(), resourceMap.size());
      assertEquals(resources.size(), pathNames.size());

      // Ensure every resource is in the map correctly.
      for (Resource r : resources) {
        assertSame(r, resourceMap.get(r.getPath()));
      }

      // Ensure that every resource path is in the set.
      for (Resource r : resources) {
        assertTrue(pathNames.contains(r.getPath()));
      }
    }

    public void assertNotSameCollections(ResourceOracleSnapshot other) {
      assertNotSame(resourceMap, other.resourceMap);
      assertNotSame(resources, other.resources);
      assertNotSame(pathNames, other.pathNames);
    }

    public void assertPathIncluded(String path) {
      assertNotNull(findResourceWithPath(path));
    }

    /**
     * Asserts that a resource having the specified path is present and that it
     * was contributed by the specified classpath entry.
     */
    public void assertPathIncluded(String expectedPath,
        ClassPathEntry expectedCpe) {
      AbstractResource r = findResourceWithPath(expectedPath);
      assertNotNull(r);
      ClassPathEntry actualCpe = r.getClassPathEntry();
      assertEquals(expectedCpe.getLocation(), actualCpe.getLocation());
    }

    public void assertPathNotIncluded(String path) {
      assertNull(findResourceWithPath(path));
    }

    public void assertSameCollections(ResourceOracleSnapshot other) {
      assertSame(resourceMap, other.resourceMap);
      assertSame(resources, other.resources);
      assertSame(pathNames, other.pathNames);
    }

    public AbstractResource findResourceWithPath(String path) {
      for (Resource r : resources) {
        if (r.getPath().equals(path)) {
          return (AbstractResource) r;
        }
      }
      return null;
    }
  }

  /**
   * Test that ResourceOracleImpl preserves the order in which the same logical
   * resource is occurs in multiple ClassPathEntries.
   * 
   * @throws URISyntaxException
   * @throws IOException
   */
  public void testClassPathOrderIsHonored() throws IOException,
      URISyntaxException {
    TreeLogger logger = createTestTreeLogger();
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();

    ClassPathEntry[] cp12 = new ClassPathEntry[] {cpe1jar, cpe2jar};
    ClassPathEntry[] cp21 = new ClassPathEntry[] {cpe2jar, cpe1jar};
    String resKeyNormal = "org/example/bar/client/BarClient2.txt";
    String resKeyReroot = "/BarClient2.txt";
    PathPrefix pathPrefixNormal = new PathPrefix("org/example/bar/client",
        null, false);
    PathPrefix pathPrefixReroot = new PathPrefix("org/example/bar/client",
        null, true);

    testResourceInCPE(logger, resKeyNormal, cpe1jar, cp12, pathPrefixNormal);
    testResourceInCPE(logger, resKeyReroot, cpe1jar, cp12, pathPrefixReroot);
    testResourceInCPE(logger, resKeyNormal, cpe2jar, cp21, pathPrefixNormal);
    testResourceInCPE(logger, resKeyReroot, cpe2jar, cp21, pathPrefixReroot);
  }

  public void testNoClassPathEntries() {
    TreeLogger logger = createTestTreeLogger();
    ResourceOracleImpl oracle = createResourceOracle(new MOCK_CPE0());
    ResourceOracleSnapshot s = refreshAndSnapshot(logger, oracle);
    s.assertCollectionsConsistent(0);
  }

  /**
   * Test that ResourceOracleImpl prefers the order of path prefixes over
   * ClassPathEntries.
   * <p>
   * cpe1 contains org/example/bar/client/BarClient1.txt and cpe2 contains
   * org/example/foo/client/BarClient1.txt
   * 
   * @throws URISyntaxException
   * @throws IOException
   */
  public void testPathPrefixOrderPreferredOverClasspath() throws IOException,
      URISyntaxException {
    TreeLogger logger = createTestTreeLogger();
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();

    ClassPathEntry[] cp12 = new ClassPathEntry[] {cpe1jar, cpe2jar};
    ClassPathEntry[] cp21 = new ClassPathEntry[] {cpe2jar, cpe1jar};

    String keyReroot = "/BarClient1.txt";

    PathPrefix pathPrefix1Reroot = new PathPrefix("org/example/bar/client",
        null, true);
    PathPrefix pathPrefix2Reroot = new PathPrefix("org/example/foo/client",
        null, true);

    PathPrefixSet pps12Reroot = new PathPrefixSet();
    pps12Reroot.add(pathPrefix1Reroot);
    pps12Reroot.add(pathPrefix2Reroot);

    PathPrefixSet pps21Reroot = new PathPrefixSet();
    pps21Reroot.add(pathPrefix2Reroot);
    pps21Reroot.add(pathPrefix1Reroot);

    /*
     * the keyReroot resource in cpe2 wins because pathPrefix2Reroot comes
     * later.
     */
    testResourceInCPE(logger, keyReroot, cpe2jar, cp12, pathPrefix1Reroot,
        pathPrefix2Reroot);
    // order of specifying classpath is reversed, it still matches cpe2
    testResourceInCPE(logger, keyReroot, cpe2jar, cp21, pathPrefix1Reroot,
        pathPrefix2Reroot);
    /*
     * the keyReroot resource in cpe2 wins because pathPrefix2Reroot comes
     * later.
     */
    testResourceInCPE(logger, keyReroot, cpe1jar, cp12, pathPrefix2Reroot,
        pathPrefix1Reroot);
    // order of specifying classpath is reversed, it still matches cpe1
    testResourceInCPE(logger, keyReroot, cpe1jar, cp21, pathPrefix2Reroot,
        pathPrefix1Reroot);
  }

  /**
   * Tests the actual reading of resources.
   * 
   * @throws URISyntaxException
   * @throws IOException
   */
  public void testReadingResource() throws IOException, URISyntaxException {
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe1dir = getClassPathEntry1AsDirectory();

    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();
    ClassPathEntry cpe2dir = getClassPathEntry2AsDirectory();

    testReadingResource(cpe1jar, cpe2jar);
    testReadingResource(cpe1dir, cpe2jar);

    testReadingResource(cpe1jar, cpe2dir);
    testReadingResource(cpe1dir, cpe2dir);
  }

  public void testResourceAddition() throws IOException, URISyntaxException {
    ClassPathEntry cpe1mock = getClassPathEntry1AsMock();
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe1dir = getClassPathEntry1AsDirectory();

    ClassPathEntry cpe2mock = getClassPathEntry2AsMock();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();
    ClassPathEntry cpe2dir = getClassPathEntry2AsDirectory();

    testResourceAddition(cpe1jar, cpe2jar);
    testResourceAddition(cpe1dir, cpe2jar);
    testResourceAddition(cpe1mock, cpe2jar);

    testResourceAddition(cpe1jar, cpe2dir);
    testResourceAddition(cpe1dir, cpe2dir);
    testResourceAddition(cpe1mock, cpe2dir);

    testResourceAddition(cpe1jar, cpe2mock);
    testResourceAddition(cpe1dir, cpe2mock);
    testResourceAddition(cpe1mock, cpe2mock);
  }

  public void testResourceDeletion() throws IOException, URISyntaxException {
    ClassPathEntry cpe1mock = getClassPathEntry1AsMock();
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe1dir = getClassPathEntry1AsDirectory();

    ClassPathEntry cpe2mock = getClassPathEntry2AsMock();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();
    ClassPathEntry cpe2dir = getClassPathEntry2AsDirectory();

    testResourceDeletion(cpe1jar, cpe2jar);
    testResourceDeletion(cpe1dir, cpe2jar);
    testResourceDeletion(cpe1mock, cpe2jar);

    testResourceDeletion(cpe1jar, cpe2dir);
    testResourceDeletion(cpe1dir, cpe2dir);
    testResourceDeletion(cpe1mock, cpe2dir);

    testResourceDeletion(cpe1jar, cpe2mock);
    testResourceDeletion(cpe1dir, cpe2mock);
    testResourceDeletion(cpe1mock, cpe2mock);
  }

  public void testResourceModification() throws IOException, URISyntaxException {
    ClassPathEntry cpe1mock = getClassPathEntry1AsMock();
    ClassPathEntry cpe1jar = getClassPathEntry1AsJar();
    ClassPathEntry cpe1dir = getClassPathEntry1AsDirectory();

    ClassPathEntry cpe2mock = getClassPathEntry2AsMock();
    ClassPathEntry cpe2jar = getClassPathEntry2AsJar();
    ClassPathEntry cpe2dir = getClassPathEntry2AsDirectory();

    testResourceModification(cpe1jar, cpe2jar);
    testResourceModification(cpe1dir, cpe2jar);
    testResourceModification(cpe1mock, cpe2jar);

    testResourceModification(cpe1jar, cpe2dir);
    testResourceModification(cpe1dir, cpe2dir);
    testResourceModification(cpe1mock, cpe2dir);

    testResourceModification(cpe1jar, cpe2mock);
    testResourceModification(cpe1dir, cpe2mock);
    testResourceModification(cpe1mock, cpe2mock);

    /*
     * TODO(bruce): figure out a good way to test real resource modifications of
     * jar files and directories
     */
  }

  /**
   * Creates an array of class path entries, setting up each one with a
   * well-known set of client prefixes.
   */
  private ResourceOracleImpl createResourceOracle(ClassPathEntry... entries) {
    PathPrefixSet pps = new PathPrefixSet();
    pps.add(new PathPrefix("com/google/gwt/user/client/", null));
    pps.add(new PathPrefix("org/example/bar/client/", null));
    pps.add(new PathPrefix("org/example/foo/client/", null));
    pps.add(new PathPrefix("com/google/gwt/i18n/client/", null));

    List<ClassPathEntry> classPath = new ArrayList<ClassPathEntry>();
    for (ClassPathEntry entry : entries) {
      classPath.add(entry);
    }
    ResourceOracleImpl oracle = new ResourceOracleImpl(classPath);
    oracle.setPathPrefixes(pps);
    return oracle;
  }

  private ResourceOracleSnapshot refreshAndSnapshot(TreeLogger logger,
      ResourceOracleImpl oracle) {
    oracle.refresh(logger);
    ResourceOracleSnapshot snapshot1 = new ResourceOracleSnapshot(oracle);
    oracle.refresh(logger);
    ResourceOracleSnapshot snapshot2 = new ResourceOracleSnapshot(oracle);
    snapshot1.assertSameCollections(snapshot2);
    return snapshot1;
  }

  private void testResourceInCPE(TreeLogger logger, String resourceKey,
      ClassPathEntry expectedCPE, ClassPathEntry[] classPath,
      PathPrefix... pathPrefixes) {
    ResourceOracleImpl oracle = new ResourceOracleImpl(Arrays.asList(classPath));
    PathPrefixSet pps = new PathPrefixSet();
    for (PathPrefix pathPrefix : pathPrefixes) {
      pps.add(pathPrefix);
    }
    oracle.setPathPrefixes(pps);
    ResourceOracleSnapshot s = refreshAndSnapshot(logger, oracle);
    s.assertPathIncluded(resourceKey, expectedCPE);
  }

  private void testReadingResource(ClassPathEntry cpe1, ClassPathEntry cpe2)
      throws IOException {
    TreeLogger logger = createTestTreeLogger();

    ResourceOracleImpl oracle = createResourceOracle(cpe1, cpe2);
    ResourceOracleSnapshot s = refreshAndSnapshot(logger, oracle);
    s.assertCollectionsConsistent(10);
    s.assertPathIncluded("com/google/gwt/user/client/Command.java", cpe1);
    s.assertPathIncluded("com/google/gwt/i18n/client/Messages.java", cpe2);

    {
      /*
       * Read a resource in cpe1.
       */
      AbstractResource res = s.findResourceWithPath("com/google/gwt/user/client/Command.java");
      BufferedReader rdr = null;
      try {
        InputStream is = res.openContents();
        assertNotNull(is);
        rdr = new BufferedReader(new InputStreamReader(is));
        assertTrue(rdr.readLine().indexOf(
            "package com.google.gwt.dev.resource.impl.testdata.cpe1.com.google.gwt.user.client;") >= 0);
      } finally {
        Utility.close(rdr);
      }
    }

    {
      /*
       * Read a resource in cpe2.
       */
      AbstractResource res = s.findResourceWithPath("com/google/gwt/i18n/client/Messages.java");
      BufferedReader rdr = null;
      try {
        InputStream is = res.openContents();
        assertNotNull(is);
        rdr = new BufferedReader(new InputStreamReader(is));
        assertTrue(rdr.readLine().indexOf(
            "package com.google.gwt.dev.resource.impl.testdata.cpe2.com.google.gwt.i18n.client;") >= 0);
      } finally {
        Utility.close(rdr);
      }
    }

    {
      /*
       * TODO: Try to read an invalid resource and watch it fail as intended.
       */
    }
  }

  private void testResourceAddition(ClassPathEntry cpe1, ClassPathEntry cpe2) {
    TreeLogger logger = createTestTreeLogger();

    MOCK_CPE0 cpe0 = new MOCK_CPE0();
    MOCK_CPE3 cpe3 = new MOCK_CPE3();
    ResourceOracleImpl oracle = createResourceOracle(cpe0, cpe1, cpe2, cpe3);

    {
      /*
       * Ensure it's correct as a baseline. These tests have hard-coded
       * assumptions about the contents of each classpath entry.
       */
      ResourceOracleSnapshot s = refreshAndSnapshot(logger, oracle);
      s.assertCollectionsConsistent(10);
      s.assertPathIncluded("com/google/gwt/user/client/Command.java");
      s.assertPathIncluded("com/google/gwt/user/client/Timer.java");
      s.assertPathIncluded("com/google/gwt/user/client/ui/Widget.java");
      s.assertPathIncluded("org/example/bar/client/BarClient1.txt");
      s.assertPathIncluded("org/example/bar/client/BarClient2.txt");
      s.assertPathIncluded("org/example/bar/client/BarClient3.txt");
      s.assertPathIncluded("org/example/bar/client/etc/BarEtc.txt");
      s.assertPathIncluded("org/example/foo/client/FooClient.java");
      s.assertPathIncluded("com/google/gwt/i18n/client/Messages.java");
    }

    {
      /*
       * Add duplicate resources later in the classpath, which won't be found.
       * Consequently, the collections' identities should not change.
       */
      cpe3.addResource("com/google/gwt/user/client/Command.java");
      cpe3.addResource("com/google/gwt/user/client/Timer.java");
      cpe3.addResource("com/google/gwt/bar/client/etc/BarEtc.txt");

      ResourceOracleSnapshot before = new ResourceOracleSnapshot(oracle);
      ResourceOracleSnapshot after = refreshAndSnapshot(logger, oracle);
      after.assertCollectionsConsistent(10);
      after.assertSameCollections(before);
    }

    {
      /*
       * Add a unique resource later in the classpath, which will be found.
       * Consequently, the collections' identities should change.
       */
      cpe3.addResource("com/google/gwt/i18n/client/Constants.java");

      ResourceOracleSnapshot before = new ResourceOracleSnapshot(oracle);
      ResourceOracleSnapshot after = refreshAndSnapshot(logger, oracle);
      after.assertCollectionsConsistent(11);
      after.assertNotSameCollections(before);
      after.assertPathIncluded("com/google/gwt/i18n/client/Constants.java");
    }
  }

  private void testResourceDeletion(ClassPathEntry cpe1, ClassPathEntry cpe2) {
    TreeLogger logger = createTestTreeLogger();

    MOCK_CPE0 cpe0 = new MOCK_CPE0();
    MOCK_CPE3 cpe3 = new MOCK_CPE3();
    ResourceOracleImpl oracle = createResourceOracle(cpe0, cpe1, cpe2, cpe3);

    /*
     * Intentionally add some duplicate resources.
     */
    cpe0.addResource("com/google/gwt/user/client/Command.java");
    cpe0.addResource("com/google/gwt/i18n/client/Constants.java");
    cpe3.addResource("com/google/gwt/user/client/Command.java");
    cpe3.addResource("com/google/gwt/i18n/client/Constants.java");

    {
      /*
       * Ensure it's correct as a baseline. These tests have hard-coded
       * assumptions about the contents of each classpath entry.
       */
      ResourceOracleSnapshot s = refreshAndSnapshot(logger, oracle);
      s.assertCollectionsConsistent(11);
      s.assertPathIncluded("com/google/gwt/user/client/Command.java");
      s.assertPathIncluded("com/google/gwt/user/client/Timer.java");
      s.assertPathIncluded("com/google/gwt/user/client/ui/Widget.java");
      s.assertPathIncluded("org/example/bar/client/BarClient1.txt");
      s.assertPathIncluded("org/example/bar/client/BarClient2.txt");
      s.assertPathIncluded("org/example/bar/client/BarClient3.txt");
      s.assertPathIncluded("org/example/bar/client/etc/BarEtc.txt");
      s.assertPathIncluded("org/example/foo/client/FooClient.java");
      s.assertPathIncluded("com/google/gwt/i18n/client/Messages.java");
      s.assertPathIncluded("com/google/gwt/i18n/client/Constants.java");
    }

    {
      /*
       * Remove a shadowed resource, which shouldn't have been found anyway.
       * Consequently, the collections' identities should not change.
       */
      cpe3.removeResource("com/google/gwt/user/client/Command.java");
      cpe3.removeResource("com/google/gwt/i18n/client/Constants.java");

      ResourceOracleSnapshot before = new ResourceOracleSnapshot(oracle);
      ResourceOracleSnapshot after = refreshAndSnapshot(logger, oracle);
      after.assertCollectionsConsistent(11);
      after.assertSameCollections(before);
    }

    {
      /*
       * Remove a unique resource, which will no longer be found. We also add a
       * new one to ensure that lack of size change doesn't confuse anything.
       * Consequently, the collections' identities should change.
       */
      cpe0.removeResource("com/google/gwt/i18n/client/Constants.java");
      cpe3.addResource("com/google/gwt/user/client/Window.java");

      ResourceOracleSnapshot before = new ResourceOracleSnapshot(oracle);
      ResourceOracleSnapshot after = refreshAndSnapshot(logger, oracle);
      after.assertCollectionsConsistent(11);
      after.assertNotSameCollections(before);
      after.assertPathIncluded("com/google/gwt/user/client/Window.java");
      after.assertPathNotIncluded("com/google/gwt/i18n/client/Constants.java");
    }
  }

  private void testResourceModification(ClassPathEntry cpe1, ClassPathEntry cpe2) {
    TreeLogger logger = createTestTreeLogger();

    MOCK_CPE0 cpe0 = new MOCK_CPE0();
    MOCK_CPE3 cpe3 = new MOCK_CPE3();
    ResourceOracleImpl oracle = createResourceOracle(cpe0, cpe1, cpe2, cpe3);

    {
      /*
       * Baseline assumptions about the set of resources present by default.
       */
      ResourceOracleSnapshot s = refreshAndSnapshot(logger, oracle);
      s.assertPathIncluded("com/google/gwt/user/client/Command.java", cpe1);
      s.assertPathIncluded("com/google/gwt/user/client/Timer.java", cpe1);
    }

    // Add intentionally duplicate resources.
    cpe0.addResource("com/google/gwt/user/client/Command.java");
    cpe3.addResource("com/google/gwt/user/client/Timer.java");

    {
      /*
       * Ensure that the dups have the effect we expect.
       */
      ResourceOracleSnapshot s = refreshAndSnapshot(logger, oracle);
      s.assertPathIncluded("com/google/gwt/user/client/Command.java", cpe0);
      s.assertPathIncluded("com/google/gwt/user/client/Timer.java", cpe1);
    }

    {
      /*
       * Change a mock resource that was shadowed to ensure that the change
       * isn't observed.
       */
      ResourceOracleSnapshot before = new ResourceOracleSnapshot(oracle);
      before.assertCollectionsConsistent(10);

      cpe3.updateResource("com/google/gwt/user/client/Timer.java");

      ResourceOracleSnapshot after = refreshAndSnapshot(logger, oracle);
      after.assertSameCollections(before);
    }

    {
      /*
       * Change a mock resource that was not shadowed to ensure that the change
       * is observed.
       */
      ResourceOracleSnapshot before = new ResourceOracleSnapshot(oracle);
      before.assertCollectionsConsistent(10);

      cpe0.updateResource("com/google/gwt/user/client/Command.java");

      ResourceOracleSnapshot after = refreshAndSnapshot(logger, oracle);
      after.assertNotSameCollections(before);
    }
  }
}
