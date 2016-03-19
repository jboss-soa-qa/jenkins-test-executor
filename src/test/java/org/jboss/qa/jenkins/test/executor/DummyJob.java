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
package org.jboss.qa.jenkins.test.executor;

import static org.jboss.qa.jenkins.test.executor.DummyJob.DUMMY_FILE;

import org.jboss.qa.jcontainer.Container;
import org.jboss.qa.jcontainer.karaf.KarafClient;
import org.jboss.qa.jcontainer.karaf.KarafConfiguration;
import org.jboss.qa.jcontainer.karaf.KarafContainer;
import org.jboss.qa.jcontainer.karaf.KarafUser;
import org.jboss.qa.jenkins.test.executor.beans.Destination;
import org.jboss.qa.jenkins.test.executor.beans.Workspace;
import org.jboss.qa.jenkins.test.executor.phase.cleanup.CleanUp;
import org.jboss.qa.jenkins.test.executor.phase.download.Download;
import org.jboss.qa.jenkins.test.executor.phase.download.Downloads;
import org.jboss.qa.jenkins.test.executor.phase.download.Dst;
import org.jboss.qa.jenkins.test.executor.phase.download.UnPack;
import org.jboss.qa.jenkins.test.executor.phase.execution.Execution;
import org.jboss.qa.jenkins.test.executor.phase.runtimesetup.RuntimeSetup;
import org.jboss.qa.jenkins.test.executor.phase.runtimeteardown.RuntimeTeardown;
import org.jboss.qa.jenkins.test.executor.phase.start.Start;
import org.jboss.qa.jenkins.test.executor.phase.staticconfiguration.StaticConfiguration;
import org.jboss.qa.jenkins.test.executor.phase.stop.Stop;
import org.jboss.qa.phaser.AfterJob;
import org.jboss.qa.phaser.BeforeJob;
import org.jboss.qa.phaser.Inject;
import org.jboss.qa.phaser.context.Property;

import org.testng.Assert;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Downloads({
		@Download(
				id = "karaf", url = "${karaf.url}",
				destination = @Dst(id = "download", destination = "download"), verbose = true,
				unpack = @UnPack(unpack = true, destination = @Dst(id = "karaf.home", destination = "karaf-server"))
		),
		@Download(
				id = "dummy", url = "file://${java.io.tmpdir}/" + DUMMY_FILE,
				destination = @Dst(id = "download", destination = "download"), verbose = true
		)})
@CleanUp(cleanWorkspace = true)
@Slf4j
public class DummyJob {

	public static final String DUMMY_FILE = "dummy";

	@Inject
	private Workspace workspace;

	@Inject(id = "karaf.home")
	private Destination karafHome;

	@Inject(id = "download")
	private Destination downloadDest;

	@Property("karaf.url")
	private String karafUrl;

	private Container container;

	private File dummyResource;

	@BeforeJob
	public void beforeJob() throws IOException {
		dummyResource = new File(System.getProperty("java.io.tmpdir"), DUMMY_FILE);
		dummyResource.createNewFile();
	}

	@StaticConfiguration(order = 1)
	public void testPropertyInjection() {
		Assert.assertNotNull(workspace);
		Assert.assertNotNull(karafHome);
		Assert.assertNotNull(downloadDest);

		Assert.assertNotNull(karafUrl);
		log.info("Karaf URL: {}", karafUrl);

		final File downloadedDummy = new File(downloadDest.getDestination(), DUMMY_FILE);
		Assert.assertTrue(downloadedDummy.exists());
		log.info("Dummy file location: {}", downloadedDummy.getAbsoluteFile());
	}

	@StaticConfiguration(order = 2)
	public void prepareContainer() throws Exception {
		final KarafConfiguration conf = KarafConfiguration.builder()
				.directory(karafHome.getDestination().getAbsolutePath())
				.build();

		container = new KarafContainer<>(conf);
		final KarafUser user = new KarafUser();
		user.setUsername("admin");
		user.setPassword("admin");
		user.addRoles("admin");
		container.addUser(user);
	}

	@Start
	public void startContainer() throws Exception {
		container.start();
	}

	@RuntimeSetup
	public void runtimeSetup() throws Exception {
		log.info("Runtime setup");
	}

	@Execution
	public void execution() throws Exception {
		try (KarafClient cli = (KarafClient) container.getClient()) {
			cli.execute("version");
			log.info(cli.getCommandResult());
		}
	}

	@RuntimeTeardown
	public void runtimeTeardown() throws Exception {
		log.info("Runtime teardown");
	}

	@Stop
	public void stopContainer() throws Exception {
		container.stop();
	}

	@AfterJob
	public void afterJob() throws IOException {
		dummyResource.delete();
	}
}
