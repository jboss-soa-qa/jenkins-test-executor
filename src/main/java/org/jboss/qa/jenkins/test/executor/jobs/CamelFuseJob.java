package org.jboss.qa.jenkins.test.executor.jobs;

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
import org.jboss.qa.jenkins.test.executor.phase.maven.Maven;
import org.jboss.qa.jenkins.test.executor.phase.runtimeconfiguration.RuntimeConfiguration;
import org.jboss.qa.jenkins.test.executor.phase.start.Start;
import org.jboss.qa.jenkins.test.executor.phase.staticconfiguration.StaticConfiguration;
import org.jboss.qa.jenkins.test.executor.phase.stop.Stop;
import org.jboss.qa.jenkins.test.executor.utils.JenkinsUtils;
import org.jboss.qa.jenkins.test.executor.utils.MavenCli;
import org.jboss.qa.phaser.Create;
import org.jboss.qa.phaser.Inject;

import lombok.extern.slf4j.Slf4j;

/**
 * CamelFuseJob
 *
 * <p>Universal properties:
 * <ul>
 * <li>job.mvn.profiles
 * <li>job.mvn.projects
 * <li>job.mvn.testPattern
 * <li>maven.version
 * </ul>
 */
@Downloads({
		@Download(
				url = "https://repository.jboss.org/nexus/content/repositories/ea/org/jboss/fuse/jboss-fuse-full/6.2.0.redhat-064/jboss-fuse-full-6.2.0.redhat-064.zip",
				destination = @Dst(id = "fuse-download-dst", destination = "FUSE-6.2"),
				unpack = @UnPack(unpack = true, destination = @Dst(id = "fuse-home", destination = "HM2"))
		)
})
@CleanUp(cleanWorkspace = true)
@Slf4j
public class CamelFuseJob {

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
		profiles = StringUtils.split(JenkinsUtils.getUniversalProperty("job.mvn.profiles", "jboss-fuse"), ",");
		projects = StringUtils.split(JenkinsUtils.getUniversalProperty("job.mvn.projects", ""), ",");
		test = JenkinsUtils.getUniversalProperty("job.mvn.testPattern");
	}

	@StaticConfiguration
	public void beforeStart() throws Exception {
		log.info("Preparing FUSE");

		FuseConfiguration conf = FuseConfiguration.builder()
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

	@Maven(order = 1)
	public void buildMavenPrerequisites(@Create MavenCli.Builder builder) throws Exception {
		builder.pom(workspace + "/jbossqe-camel-it/pom.xml").goals("clean", "install").alsoMake(true);
		if (projects != null) {
			builder.projects(projects);
		}
		builder.build().run();
	}

	@Maven(order = 2)
	public void executeTests(@Create MavenCli.Builder builder) throws Exception {
		builder.pom(workspace + "/jbossqe-camel-it/pom.xml").goals("verify").failAtEnd(true);
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
		System.out.println(container.isRunning());
	}
}
