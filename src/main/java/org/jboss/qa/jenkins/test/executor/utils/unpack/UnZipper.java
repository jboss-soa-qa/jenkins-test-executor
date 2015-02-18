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
	public void unpack(File archive, File destination) throws IOException {
		try (ZipFile zip = new ZipFile(archive)) {
			Set<ZipArchiveEntry> entries = new HashSet<>(Collections.list(zip.getEntries()));

			if (ignoreRootFolders) {
				pathSegmentsToTrim = countRootFolders(entries);
			}

			for (ZipArchiveEntry entry : entries) {
				if (entry.isDirectory()) {
					continue;
				}
				String zipPath = trimPathSegments(entry.getName(), pathSegmentsToTrim);
				final File file = new File(destination, zipPath);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs(); // Create parent folders if not exist
				}

				try(InputStream is = zip.getInputStream(entry); OutputStream fos = new FileOutputStream(file)) {
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
