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

import org.apache.commons.lang3.StringUtils;

import org.jboss.qa.jcontainer.Container;
import org.jboss.qa.jcontainer.fuse.FuseConfiguration;
import org.jboss.qa.jcontainer.fuse.FuseContainer;
import org.jboss.qa.jcontainer.fuse.FuseUser;
import org.jboss.qa.jenkins.test.executor.beans.Destination;
import org.jboss.qa.jenkins.test.executor.beans.Workspace;
import org.jboss.qa.jenkins.test.executor.phase.cleanup.CleanUp;
import org.jboss.qa.jenkins.test.executor.phase.download.Download;
import org.jboss.qa.jenkins.test.executor.phase.download.Downloads;
import org.jboss.qa.jenkins.test.executor.phase.download.Dst;
import org.jboss.qa.jenkins.test.executor.phase.download.UnPack;
import org.jboss.qa.jenkins.test.executor.phase.execution.Execution;
import org.jboss.qa.jenkins.test.executor.phase.runtimeconfiguration.RuntimeConfiguration;
import org.jboss.qa.jenkins.test.executor.phase.start.Start;
import org.jboss.qa.jenkins.test.executor.phase.staticconfiguration.StaticConfiguration;
import org.jboss.qa.jenkins.test.executor.phase.stop.Stop;
import org.jboss.qa.jenkins.test.executor.utils.JenkinsUtils;
import org.jboss.qa.jenkins.test.executor.utils.MavenCli;
import org.jboss.qa.phaser.Create;
import org.jboss.qa.phaser.Inject;

import lombok.extern.slf4j.Slf4j;

@Downloads({
		@Download(
				url = "file:///tmp/workspace/jboss-fuse.zip",
				destination = @Dst(id = "fuse-download-dst", destination = "FUSE-6.2"), verbose = true,
				unpack = @UnPack(unpack = true, destination = @Dst(id = "fuse-home", destination = "fuse-server"))
		)})
@CleanUp(cleanWorkspace = true)
@Slf4j
public class DummyJob {

	@Inject
	private Workspace workspace;

	@Inject(id = "fuse-download-dst")
	private Destination fuseDownloadDestination;

	@Inject(id = "fuse-home")
	private Destination fuseHome;

	private Container container;

	private String[] profiles;
	private String[] projects;
	private String test;

	@StaticConfiguration
	public void getUnivarsalProperties() {
		log.info("Setting properties");

		profiles = StringUtils.split(JenkinsUtils.getUniversalProperty("job.mvn.profiles", "jboss-fuse"), ",");
		projects = StringUtils.split(JenkinsUtils.getUniversalProperty("job.mvn.projects", ""), ",");
		test = JenkinsUtils.getUniversalProperty("job.mvn.testpattern");
	}

	@StaticConfiguration
	public void beforeStart() throws Exception {
		log.info("Preparing FUSE");

		final FuseConfiguration conf = FuseConfiguration.builder()
				.directory(fuseHome.getDestination().getAbsolutePath())
				.build();

		container = new FuseContainer<>(conf);
		final FuseUser user = new FuseUser();
		user.setUsername("admin");
		user.setPassword("admin");
		user.addRoles("admin", "SuperUser");
		container.addUser(user);
	}

	@Start
	public void start() throws Exception {
		container.start();
		System.out.println(container.isRunning());
	}

	@RuntimeConfiguration
	public void afterStart() throws Exception {
		container.getClient().execute("osgi:version");
	}

	protected MavenCli.Builder initMavenBuilder(MavenCli.Builder builder) {
		return builder.pom(workspace.getDestination().getAbsolutePath() + "/jbossqe-camel-it/pom.xml");
	}

	@Execution(order = 1)
	public void buildMavenPrerequisites(@Create MavenCli.Builder builder) throws Exception {
		initMavenBuilder(builder).goals("clean", "install").alsoMake(true);
		if (projects != null) {
			builder.projects(projects);
		}
		builder.build().run();
	}

	@Execution(order = 2)
	public void executeTests(@Create MavenCli.Builder builder) throws Exception {
		initMavenBuilder(builder).goals("verify").failAtEnd(true).alsoMakeDependents(true);
		if (profiles != null) {
			builder.profiles(profiles);
		}
		if (projects != null) {
			builder.projects(projects);
		}
		if (test != null) {
			builder.sysProp("test", test);
		}
		builder.build().run();
	}

	@Stop
	public void stop() throws Exception {
		container.stop();
		log.debug("Container status: {}", container.isRunning() ? "running" : "stopped");
	}
}
