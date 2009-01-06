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
import com.google.gwt.dev.util.msg.Message1String;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A classpath entry that is a jar or zip file.
 */
public class ZipFileClassPathEntry extends ClassPathEntry {

  /**
   * Logger messages related to this class.
   */
  private static class Messages {
    static final Message1String BUILDING_INDEX = new Message1String(
        TreeLogger.TRACE, "Indexing zip file: $0");

    static final Message1String EXCLUDING_RESOURCE = new Message1String(
        TreeLogger.DEBUG, "Excluding $0");

    static final Message1String FINDING_INCLUDED_RESOURCES = new Message1String(
        TreeLogger.DEBUG, "Searching for included resources in $0");

    static final Message1String INCLUDING_RESOURCE = new Message1String(
        TreeLogger.DEBUG, "Including $0");

    static final Message1String READ_ZIP_ENTRY = new Message1String(
        TreeLogger.DEBUG, "$0");
  }

  private Set<ZipFileResource> allZipFileResources;
  private Set<AbstractResource> cachedAnswers;
  private String cachedLocation;
  private PathPrefixSet lastPrefixSet;
  private final ZipFile zipFile;

  public ZipFileClassPathEntry(ZipFile zipFile) {
    this.zipFile = zipFile;
  }

  /**
   * Indexes the zip file on-demand, and only once over the life of the process.
   */
  @Override
  public Set<AbstractResource> findApplicableResources(TreeLogger logger,
      PathPrefixSet pathPrefixSet) {
    // Never re-index.
    if (allZipFileResources == null) {
      allZipFileResources = buildIndex(logger);
    }

    if (cachedAnswers == null || lastPrefixSet != pathPrefixSet
        || lastPrefixSet.getModCount() != pathPrefixSet.getModCount()) {
      cachedAnswers = computeApplicableResources(logger, pathPrefixSet);
    }

    return cachedAnswers;
  }

  @Override
  public String getLocation() {
    if (cachedLocation == null) {
      cachedLocation = new File(zipFile.getName()).toURI().toString();
    }
    return cachedLocation;
  }

  public ZipFile getZipFile() {
    return zipFile;
  }

  private Set<ZipFileResource> buildIndex(TreeLogger logger) {
    logger = Messages.BUILDING_INDEX.branch(logger, zipFile.getName(), null);

    HashSet<ZipFileResource> results = new HashSet<ZipFileResource>();
    Enumeration<? extends ZipEntry> e = zipFile.entries();
    while (e.hasMoreElements()) {
      ZipEntry zipEntry = e.nextElement();
      if (zipEntry.isDirectory()) {
        // Skip directories.
        continue;
      }
      if (zipEntry.getName().startsWith("META-INF/")) {
        // Skip META-INF since classloaders normally make this invisible.
        continue;
      }
      ZipFileResource zipResource = new ZipFileResource(this, zipEntry);
      results.add(zipResource);
      Messages.READ_ZIP_ENTRY.log(logger, zipEntry.getName(), null);
    }
    return results;
  }

  private Set<AbstractResource> computeApplicableResources(TreeLogger logger,
      PathPrefixSet pathPrefixSet) {
    logger = Messages.FINDING_INCLUDED_RESOURCES.branch(logger,
        zipFile.getName(), null);

    Set<AbstractResource> results = new HashSet<AbstractResource>();
    for (ZipFileResource r : allZipFileResources) {
      String path = r.getPath();
      if (pathPrefixSet.includesResource(path)) {
        Messages.INCLUDING_RESOURCE.log(logger, path, null);
        results.add(r);
      } else {
        Messages.EXCLUDING_RESOURCE.log(logger, path, null);
      }
    }
    return results;
  }
}
