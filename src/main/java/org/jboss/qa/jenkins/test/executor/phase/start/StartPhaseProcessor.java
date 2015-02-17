package org.jboss.qa.jenkins.test.executor.phase.start;

import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class StartPhaseProcessor extends PhaseDefinitionProcessor {

	private Start start;

	public void execute() {
		log.debug("@{} - {}", Start.class.getName(), start.id());
	}
}
