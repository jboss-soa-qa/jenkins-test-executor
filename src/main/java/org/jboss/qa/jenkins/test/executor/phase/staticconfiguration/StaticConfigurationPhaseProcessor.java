package org.jboss.qa.jenkins.test.executor.phase.staticconfiguration;

import org.jboss.qa.phaser.PhaseDefinitionProcessor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class StaticConfigurationPhaseProcessor extends PhaseDefinitionProcessor {

	private StaticConfiguration configuration;

	public void execute() {
		log.debug("@{} - {}", StaticConfiguration.class.getName(), configuration.id());
	}
}
