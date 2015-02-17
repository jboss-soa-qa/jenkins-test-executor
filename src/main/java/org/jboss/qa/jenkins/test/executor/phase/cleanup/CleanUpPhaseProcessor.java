package org.jboss.qa.jenkins.test.executor.phase.cleanup;

import org.apache.commons.io.FileUtils;

import org.jboss.qa.jenkins.test.executor.JenkinsTestExecutor;
import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class CleanUpPhaseProcessor extends PhaseDefinitionProcessor {

	private CleanUp cleanUp;

	public void execute() {
		log.debug("@{} - {}", CleanUp.class.getName(), cleanUp.id());
		try {
			// TODO(vchalupa): Make a decision about this, some parameter in annotation may be set...
			// TODO(vchalupa): Without clean the unpacked server home can not be recognized
			FileUtils.deleteDirectory(JenkinsTestExecutor.WORKSPACE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
