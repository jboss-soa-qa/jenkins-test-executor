package org.jboss.qa.jenkins.test.executor.utils.unpack;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class UnPacker {

	protected static int countRootFolders(Set<? extends ArchiveEntry> entries) {
		List<String> fileNames = new ArrayList<>();
		for (ArchiveEntry entry : entries) {
			fileNames.add(entry.getName());
		}
		return countRootFolders(fileNames);
	}

	protected static int countRootFolders(List<String> fileNames) {
		String prefix = StringUtils.getCommonPrefix(fileNames.toArray(new String[fileNames.size()]));
		if (!prefix.endsWith(File.separator)) {
			prefix = prefix.substring(0, prefix.lastIndexOf(File.separator) + 1);
		}

		// The first found prefix can match only directory:
		// root/ (will be removed)
		// root/a (will be removed)
		// root/a/a/file.txt (root/a/ is the prefix)
		// root/abreak;/b/file.txt
		if (fileNames.remove(prefix)) {
			return countRootFolders(fileNames);
		}
		return StringUtils.countMatches(prefix, File.separator);
	}

	protected static String trimPathSegments(String zipPath, final int pathSegmentsToTrim) {
		int position = 0;
		for (int i = 0; i < pathSegmentsToTrim; i++) {
			int index = zipPath.indexOf(File.separator, position);
			if (position == -1) {
				break;
			}
			position = index + 1;
		}

		return zipPath.substring(position);
	}

	protected boolean ignoreRootFolders;
	protected int pathSegmentsToTrim;

	public void setIgnoreRootFolders(boolean ignoreRootFolders) {
		this.ignoreRootFolders = ignoreRootFolders;
	}

	public void setPathSegmentsToTrim(int pathSegmentsToTrim) {
		this.pathSegmentsToTrim = pathSegmentsToTrim;
	}

	public abstract String type();

	public abstract void unpack(File archive, File destination) throws IOException;
}
