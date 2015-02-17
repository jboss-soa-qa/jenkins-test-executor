package org.jboss.qa.jenkins.test.executor.phase.download;

import org.jboss.qa.jenkins.test.executor.JenkinsTestExecutor;
import org.jboss.qa.jenkins.test.executor.beans.Destination;
import org.jboss.qa.jenkins.test.executor.utils.AntEr;
import org.jboss.qa.jenkins.test.executor.utils.FileUtils;
import org.jboss.qa.phaser.InstanceRegistry;
import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class DownloadPhaseProcessor extends PhaseDefinitionProcessor {

	private static void registerDestination(String id, File destination) {
		if (!id.isEmpty()) {
			InstanceRegistry.insert(id, new Destination(destination));
		}
	}

	private static Map<String, File> saveDirHistory(File[] files) {
		Map<String, File> dirsHistory = new HashMap<>();
		for (File f : files) {
			dirsHistory.put(f.getAbsolutePath() + ":" + f.lastModified(), f);
		}
		return dirsHistory;
	}

	private static Collection<File> dirDiff(Map<String, File> prev, Map<String, File> current) {
		for (Map.Entry cf : prev.entrySet()) {
			current.remove(cf.getKey());
		}
		return current.values();
	}

	private Download download;

	public void execute() {
		log.debug("@{} - {}", Download.class.getName(), download.id());

		File downloaded = download();
		registerDestination(download.destination().id(), downloaded);

		// Unpack
		if (download.unpack().unpack()) {
			File unpacked = unpack(downloaded);
			registerDestination(download.unpack().destination().id(), unpacked);
		}
	}

	private File download() {
		File destination = FileUtils.get(JenkinsTestExecutor.WORKSPACE, download.destination().destination());

		log.info("Download resource \"{}\"", download.id());

		AntEr.build()
				.param("src", download.url()).param("dest", destination.getAbsolutePath()).param("verbose", "true")
				.invoke("get");

		return destination;
	}

	private File unpack(File downloaded) {
		File unpacked = downloaded;
		// Use own destination or destination for download
		if (!download.unpack().destination().destination().isEmpty()) {
			unpacked = FileUtils.get(JenkinsTestExecutor.WORKSPACE, download.unpack().destination().destination());
		}
		File archive = new File(downloaded, download.url().substring(download.url().lastIndexOf("/")));

		log.info("Unpack resource \"{}\"", download.id());

		// Remember all dirs in dest
		Map<String, File> beforeUnzipDirs = saveDirHistory(FileUtils.listDirectories(unpacked));

		AntEr ant = AntEr.build().param("src", archive.getAbsolutePath()).param("dest", unpacked.getAbsolutePath());
		if (download.url().endsWith(".zip")) {
			ant.param("overwrite", "true").invoke("unzip");
		} else if (download.url().endsWith(".tar.gz")) {
			ant.param("compression", "gzip").invoke("untar");
		}

		// Discover if crated sub-dir by unpacking
		Map<String, File> afterUnzipDirs = saveDirHistory(FileUtils.listDirectories(unpacked));
		Collection<File> newDirs = dirDiff(beforeUnzipDirs, afterUnzipDirs);
		if (newDirs.size() == 1) {
			for (File f : newDirs) {
				unpacked = f;
			}
		}

		// TODO(vchalupa): LastModified does not change for unzip override
		unpacked = new File(unpacked.getAbsolutePath() + File.separator + "jboss-fuse-6.2.0.redhat-058");
		return unpacked;
	}
}
