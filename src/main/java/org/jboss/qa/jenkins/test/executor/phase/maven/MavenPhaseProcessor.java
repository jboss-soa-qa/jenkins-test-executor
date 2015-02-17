package org.jboss.qa.jenkins.test.executor.phase.maven;

import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MavenPhaseProcessor extends PhaseDefinitionProcessor {

	private Maven maven;

	public void execute() {
		log.debug("@{} - {}", Maven.class.getName(), maven.id());
	}
}
