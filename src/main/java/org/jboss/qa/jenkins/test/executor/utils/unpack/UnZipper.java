/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.jenkins.test.executor.utils.unpack;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UnZipper extends UnPacker {

	private static final String TYPE = "zip";

	public String type() {
		return TYPE;
	}

	@Override
	public boolean handles(File archive) {
		return archive.getName().endsWith(TYPE);
	}

	@Override
	public void unpack(File archive, File destination) throws IOException {
		try (ZipFile zip = new ZipFile(archive)) {
			final Set<ZipArchiveEntry> entries = new HashSet<>(Collections.list(zip.getEntries()));

			if (ignoreRootFolders) {
				pathSegmentsToTrim = countRootFolders(entries);
			}

			for (ZipArchiveEntry entry : entries) {
				if (entry.isDirectory()) {
					continue;
				}
				final String zipPath = trimPathSegments(entry.getName(), pathSegmentsToTrim);
				final File file = new File(destination, zipPath);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs(); // Create parent folders if not exist
				}

				try (InputStream is = zip.getInputStream(entry); OutputStream fos = new FileOutputStream(file)) {
					IOUtils.copy(is, fos);
					// check for user-executable bit on entry and apply to file
					if ((entry.getUnixMode() & 0100) != 0) {
						file.setExecutable(true);
					}
				}
				file.setLastModified(entry.getTime());
			}
		}
	}
}
