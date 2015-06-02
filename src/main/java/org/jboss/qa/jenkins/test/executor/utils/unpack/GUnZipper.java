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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GUnZipper extends UnPacker {

	private static final String TYPE = "tar.gz";

	private static Set<TarArchiveEntry> getEntries(TarArchiveInputStream tarIn) throws IOException {
		final Set<TarArchiveEntry> entries = new HashSet<>();
		while (true) {
			final TarArchiveEntry entry = tarIn.getNextTarEntry();
			if (entry == null) {
				break;
			}
			entries.add(entry);
		}
		return entries;
	}

	public String type() {
		return TYPE;
	}

	@Override
	public boolean handles(File archive) {
		return archive.getName().endsWith(TYPE);
	}

	@Override
	public void unpack(File archive, File destination) throws IOException {
		try (FileInputStream fin = new FileInputStream(archive);
			 BufferedInputStream in = new BufferedInputStream(fin);
			 GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
			 TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)) {
			final Set<TarArchiveEntry> entries = getEntries(tarIn);
			if (ignoreRootFolders) {
				pathSegmentsToTrim = countRootFolders(entries);
			}
		}
		// Input stream is already read so we need to open new stream
		try (FileInputStream fin = new FileInputStream(archive);
			 BufferedInputStream in = new BufferedInputStream(fin);
			 GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
			 TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)) {

			TarArchiveEntry entry = null;
			while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}

				final File file = new File(destination, trimPathSegments(entry.getName(), pathSegmentsToTrim));
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}

				final int buffer = 2048;
				try (FileOutputStream fos = new FileOutputStream(file);
					 BufferedOutputStream dest = new BufferedOutputStream(fos, buffer)) {
					IOUtils.copy(tarIn, fos);

					// check for user-executable bit on entry and apply to file
					if ((entry.getMode() & 0100) != 0) {
						file.setExecutable(true);
					}
				}
			}
		}
	}
}
