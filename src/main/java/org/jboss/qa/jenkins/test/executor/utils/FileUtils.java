package org.jboss.qa.jenkins.test.executor.utils;

import java.io.File;
import java.io.FileFilter;

public final class FileUtils {

	public static File[] listDirectories(File path) {
		if (!path.isDirectory()) {
			return new File[] {};
		}
		return path.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
	}

	public static File get(File parent, String child) {
		File destination = new File(parent, child);
		destination.mkdirs();
		return destination;
	}

	private FileUtils() {
	}
}
