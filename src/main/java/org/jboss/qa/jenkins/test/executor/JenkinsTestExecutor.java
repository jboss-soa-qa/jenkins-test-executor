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
import org.jboss.qa.jenkins.test.executor.utils.JenkinsUtils;
import org.jboss.qa.phaser.PhaseTreeBuilder;
import org.jboss.qa.phaser.Phaser;
import org.jboss.qa.phaser.registry.InstanceRegistry;
import org.jboss.qa.phaser.registry.SimpleInstanceRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JenkinsTestExecutor {

	public static final File WORKSPACE = new File(JenkinsUtils.getUniversalProperty("workspace", "target"));

	private List<Object> jobInstances;

	public JenkinsTestExecutor(List<Object> jobInstances) {
		this.jobInstances = jobInstances;
	}

	public JenkinsTestExecutor(Object... jobInstances) {
		this.jobInstances = new ArrayList<>();
		for (Object o : jobInstances) {
			this.jobInstances.add(o);
		}
	}

	public JenkinsTestExecutor(Class... jobClasses) throws Exception {
		this.jobInstances = new ArrayList<>();
		for (Class o : jobClasses) {
			this.jobInstances.add(o.newInstance());
		}
	}

	public void run() throws Exception {
		// Set default workspace
		final InstanceRegistry registry = new SimpleInstanceRegistry();
		registry.insert(new Workspace(WORKSPACE));

		// Create phase-tree
		final PhaseTreeBuilder builder = new PhaseTreeBuilder();
		builder
				.addPhase(new DownloadPhase())
				.next()
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
