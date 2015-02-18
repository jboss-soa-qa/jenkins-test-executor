package org.jboss.qa.jenkins.test.executor.phase.download;

import org.apache.commons.io.FilenameUtils;

import org.jboss.qa.jenkins.test.executor.JenkinsTestExecutor;
import org.jboss.qa.jenkins.test.executor.beans.Destination;
import org.jboss.qa.jenkins.test.executor.utils.AntEr;
import org.jboss.qa.jenkins.test.executor.utils.FileUtils;
import org.jboss.qa.jenkins.test.executor.utils.unpack.UnPacker;
import org.jboss.qa.jenkins.test.executor.utils.unpack.UnPackerRegistry;
import org.jboss.qa.phaser.InstanceRegistry;
import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import java.io.File;
import java.io.IOException;

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
				.param("src", download.url())
				.param("dest", destination.getAbsolutePath())
				.param("verbose", new Boolean(download.verbose()).toString())
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

		log.info("Unpack resource {}", download.id());

		try {
			UnPacker unPacker = UnPackerRegistry.get(FilenameUtils.getExtension(download.url()));
			if (unPacker == null) {
				throw new RuntimeException("No existing UnPacker for " + download.url());
			}
			unPacker.setPathSegmentsToTrim(download.unpack().pathSegmentsToTrim());
			unPacker.setIgnoreRootFolders(download.unpack().ignoreRootFolders());
			unPacker.unpack(archive, unpacked);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return unpacked;
	}
}
