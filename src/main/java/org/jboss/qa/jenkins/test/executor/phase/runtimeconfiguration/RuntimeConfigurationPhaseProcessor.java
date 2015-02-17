package org.jboss.qa.jenkins.test.executor.phase.runtimeconfiguration;

import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RuntimeConfigurationPhaseProcessor extends PhaseDefinitionProcessor {

	private RuntimeConfiguration configuration;

	public void execute() {
		log.debug("@{} - {}", RuntimeConfiguration.class.getName(), configuration.id());
	}
}
