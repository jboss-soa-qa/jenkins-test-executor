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

import org.jboss.qa.jenkins.test.executor.beans.Workspace;
import org.jboss.qa.jenkins.test.executor.phase.cleanup.CleanUpPhase;
import org.jboss.qa.jenkins.test.executor.phase.download.DownloadPhase;
import org.jboss.qa.jenkins.test.executor.phase.execution.ExecutionPhase;
import org.jboss.qa.jenkins.test.executor.phase.runtimesetup.RuntimeSetupPhase;
import org.jboss.qa.jenkins.test.executor.phase.runtimeteardown.RuntimeTeardownPhase;
import org.jboss.qa.jenkins.test.executor.phase.start.StartPhase;
import org.jboss.qa.jenkins.test.executor.phase.staticconfiguration.StaticConfigurationPhase;
import org.jboss.qa.jenkins.test.executor.phase.stop.StopPhase;
import org.jboss.qa.phaser.PhaseTreeBuilder;
import org.jboss.qa.phaser.Phaser;
import org.jboss.qa.phaser.registry.InstanceRegistry;
import org.jboss.qa.phaser.registry.SimpleInstanceRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JenkinsTestExecutor {

	public static final Workspace DEFAULT_WORKSPACE = new Workspace(new File("target"));

	private List<Object> jobInstances;

	private Workspace workspace;

	public JenkinsTestExecutor(Workspace workspace, List<Object> jobInstances) {
		this.jobInstances = jobInstances;
		this.workspace = workspace;
	}

	public JenkinsTestExecutor(List<Object> jobInstances) {
		this(DEFAULT_WORKSPACE, jobInstances);
	}

	public JenkinsTestExecutor(Workspace workspace, Object... jobInstances) {
		this(workspace, Arrays.asList(jobInstances));
	}

	public JenkinsTestExecutor(Object... jobInstances) {
		this(Arrays.asList(jobInstances));
	}

	public JenkinsTestExecutor(Workspace workspace, Class... jobClasses) throws IllegalAccessException, InstantiationException {
		jobInstances = new ArrayList<>();
		for (Class o : jobClasses) {
			jobInstances.add(o.newInstance());
		}
		this.workspace = workspace;
	}

	public JenkinsTestExecutor(Class... jobClasses) throws Exception {
		this(DEFAULT_WORKSPACE, jobClasses);
	}

	public void run() throws Exception {
		final InstanceRegistry registry = new SimpleInstanceRegistry();
		run(registry);
	}

	public void run(InstanceRegistry registry) throws Exception {
		// Setup workspace
		registry.insert(workspace);

		// Create phase-tree
		final PhaseTreeBuilder builder = new PhaseTreeBuilder();
		builder
				.addPhase(new DownloadPhase())
				.addPhase(new StaticConfigurationPhase())
				.addPhase(new StartPhase())
				.addPhase(new RuntimeSetupPhase())
				.addPhase(new ExecutionPhase())
				.addPhase(new RuntimeTeardownPhase())
				.addPhase(new StopPhase())
				.addPhase(new CleanUpPhase());

		// Run the Phaser
		new Phaser(builder.build(), jobInstances).run(registry);
	}
}
