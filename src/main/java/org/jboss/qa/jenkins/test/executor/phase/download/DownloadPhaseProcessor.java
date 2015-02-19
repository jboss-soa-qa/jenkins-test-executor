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
package org.jboss.qa.jenkins.test.executor.phase.download;

import org.apache.commons.io.FilenameUtils;

import org.jboss.qa.jenkins.test.executor.JenkinsTestExecutor;
import org.jboss.qa.jenkins.test.executor.beans.Destination;
import org.jboss.qa.jenkins.test.executor.utils.AntEr;
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

		final File downloaded = download();
		registerDestination(download.destination().id(), downloaded);

		// Unpack
		if (download.unpack().unpack()) {
			final File unpacked = unpack(downloaded);
			registerDestination(download.unpack().destination().id(), unpacked);
		}
	}

	private File download() {
		final File destination = new File(JenkinsTestExecutor.WORKSPACE, download.destination().destination());
		destination.mkdirs();

		log.info("Download resource \"{}\"", download.id());

		AntEr.build()
				.param("src", download.url())
				.param("dest", destination.getAbsolutePath())
				.param("verbose", download.verbose() ? Boolean.TRUE.toString() : Boolean.FALSE.toString())
				.invoke("get");

		return destination;
	}

	private File unpack(File downloaded) {
		File unpacked = downloaded;
		// Use own destination or destination for download
		if (!download.unpack().destination().destination().isEmpty()) {
			unpacked = new File(JenkinsTestExecutor.WORKSPACE, download.unpack().destination().destination());
		}
		final File archive = new File(downloaded, download.url().substring(download.url().lastIndexOf("/")));

		log.info("Unpack resource {}", download.id());

		try {
			final UnPacker unPacker = UnPackerRegistry.get(FilenameUtils.getExtension(download.url()));
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
