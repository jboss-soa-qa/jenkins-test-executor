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

import org.jboss.qa.jcontainer.Container;
import org.jboss.qa.jcontainer.fuse.FuseUser;
import org.jboss.qa.jcontainer.karaf.KarafClient;
import org.jboss.qa.jcontainer.karaf.KarafConfiguration;
import org.jboss.qa.jcontainer.karaf.KarafContainer;
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
import org.jboss.qa.jenkins.test.executor.utils.JenkinsUtils;
import org.jboss.qa.phaser.Inject;

import lombok.extern.slf4j.Slf4j;

@Downloads({
		@Download(
				id = "karaf", url = "${karaf.url}",
				destination = @Dst(id = "download", destination = "download"), verbose = true,
				unpack = @UnPack(unpack = true, destination = @Dst(id = "karaf-home", destination = "karaf-server"))
		)})
@CleanUp(cleanWorkspace = true)
@Slf4j
public class DummyJob {

	@Inject
	private Workspace workspace;

	@Inject(id = "karaf-home")
	private Destination karafHome;

	private Container container;

	@StaticConfiguration
	public void getUnivarsalProperties() {
		log.info("Karaf URL: {}", JenkinsUtils.getUniversalProperty("karaf.url"));
	}

	@StaticConfiguration
	public void beforeStart() throws Exception {
		final KarafConfiguration conf = KarafConfiguration.builder()
				.directory(karafHome.getDestination().getAbsolutePath())
				.build();

		container = new KarafContainer<>(conf);
		final FuseUser user = new FuseUser();
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
	public void rumtimeSetup() throws Exception {
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
}
