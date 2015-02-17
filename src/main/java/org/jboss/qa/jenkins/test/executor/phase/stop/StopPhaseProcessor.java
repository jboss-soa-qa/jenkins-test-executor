package org.jboss.qa.jenkins.test.executor.phase.stop;

import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class StopPhaseProcessor extends PhaseDefinitionProcessor {

	private Stop stop;

	public void execute() {
		log.debug("@{} - {}", Stop.class.getName(), stop.id());
	}
}
