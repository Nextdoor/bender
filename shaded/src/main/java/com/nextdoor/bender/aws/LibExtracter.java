/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.aws;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Mostly copy paste from https://stackoverflow.com/a/600198/384973
 * 
 * "lib/bender-shaded-s3proxy-1.0.0-SNAPSHOT.jar"
 */
public class LibExtracter {
  public static String extract(String libJar) throws URISyntaxException, ZipException, IOException {
    final URI uri;
    final URI jar;

    uri = getJarURI();
    jar = getFile(uri, libJar);

    return jar.getPath();
  }

  private static URI getJarURI() throws URISyntaxException {
    final ProtectionDomain domain;
    final CodeSource source;
    final URL url;
    final URI uri;

    domain = LibExtracter.class.getProtectionDomain();
    source = domain.getCodeSource();
    url = source.getLocation();
    uri = url.toURI();

    return (uri);
  }

  private static URI getFile(final URI where, final String fileName)
      throws ZipException, IOException {
    final File location;
    final URI fileURI;

    location = new File(where);

    // not in a JAR, just return the path on disk
    if (location.isDirectory()) {
      fileURI = URI.create(where.toString() + fileName);
    } else {
      final ZipFile zipFile;

      zipFile = new ZipFile(location);

      try {
        fileURI = extract(zipFile, fileName);
      } finally {
        zipFile.close();
      }
    }

    return (fileURI);
  }

  private static URI extract(final ZipFile zipFile, final String fileName) throws IOException {
    final File tempFile;
    final ZipEntry entry;
    final InputStream zipStream;
    OutputStream fileStream;

    tempFile = File.createTempFile(fileName, Long.toString(System.currentTimeMillis()));
    tempFile.deleteOnExit();
    entry = zipFile.getEntry(fileName);

    if (entry == null) {
      throw new FileNotFoundException(
          "cannot find file: " + fileName + " in archive: " + zipFile.getName());
    }

    zipStream = zipFile.getInputStream(entry);
    fileStream = null;

    try {
      final byte[] buf;
      int i;

      fileStream = new FileOutputStream(tempFile);
      buf = new byte[1024];
      i = 0;

      while ((i = zipStream.read(buf)) != -1) {
        fileStream.write(buf, 0, i);
      }
    } finally {
      close(zipStream);
      close(fileStream);
    }

    return (tempFile.toURI());
  }

  private static void close(final Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (final IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
