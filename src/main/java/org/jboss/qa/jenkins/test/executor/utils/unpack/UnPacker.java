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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class UnPacker {

	protected static int countRootFolders(Set<? extends ArchiveEntry> entries) {
		final List<String> fileNames = new ArrayList<>();
		for (ArchiveEntry entry : entries) {
			fileNames.add(entry.getName());
		}
		return countRootFolders(fileNames);
	}

	protected static int countRootFolders(List<String> fileNames) {
		String prefix = StringUtils.getCommonPrefix(fileNames.toArray(new String[fileNames.size()]));
		if (!prefix.endsWith("/")) {
			prefix = prefix.substring(0, prefix.lastIndexOf("/") + 1);
		}

		// The first found prefix can match only directory:
		// root/ (will be removed)
		// root/a (will be removed)
		// root/a/a/file.txt (root/a/ is the prefix)
		// root/abreak;/b/file.txt
		if (fileNames.remove(prefix)) {
			return countRootFolders(fileNames);
		}
		return StringUtils.countMatches(prefix, "/");
	}

	protected static String trimPathSegments(String zipPath, final int pathSegmentsToTrim) {
		int position = 0;
		for (int i = 0; i < pathSegmentsToTrim; i++) {
			final int index = zipPath.indexOf("/", position);
			if (index == -1) {
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

	public abstract boolean handles(File archive);
}
