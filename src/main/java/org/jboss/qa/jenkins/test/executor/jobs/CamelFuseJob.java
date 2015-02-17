package org.jboss.qa.jenkins.test.executor.jobs;

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
import org.jboss.qa.jenkins.test.executor.phase.download.Unpack;
import org.jboss.qa.jenkins.test.executor.phase.maven.Maven;
import org.jboss.qa.jenkins.test.executor.phase.runtimeconfiguration.RuntimeConfiguration;
import org.jboss.qa.jenkins.test.executor.phase.start.Start;
import org.jboss.qa.jenkins.test.executor.phase.staticconfiguration.StaticConfiguration;
import org.jboss.qa.jenkins.test.executor.phase.stop.Stop;
import org.jboss.qa.phaser.Inject;

import lombok.extern.slf4j.Slf4j;

@Downloads({
		@Download(
				url = "file:///home/vchalupa/Downloads/jboss-fuse-full-6.2.0.redhat-058.zip",
				destination = @Dst(id = "fuse-download-dst", destination = "FUSE-6.2"),
				unpack = @Unpack(unpack = true, destination = @Dst(id = "fuse-home", destination = "HM"))
		)
})
@Slf4j
public class CamelFuseJob {

	@Inject
	private Workspace workspace;

	@Inject(id = "fuse-download-dst")
	private Destination fuseDownloadDestination;

	@Inject(id = "fuse-home")
	private Destination fuseHome;

	private Container container;

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
		container.getClient().execute("osgi:info");
	}

	@Maven
	public void executeTests() {
		System.out.println("TESTING");
	}

	@Stop
	public void stop() throws Exception {
		container.stop();
		System.out.println(container.isRunning());
	}

	@CleanUp
	public void clean() {
	}
}
