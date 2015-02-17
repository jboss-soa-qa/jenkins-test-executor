package org.jboss.qa.jenkins.test.executor.phase.staticconfiguration;

import org.jboss.qa.phaser.PhaseDefinitionProcessorBuilder;

import java.lang.reflect.Method;

public class StaticConfigurationPhaseProcessorBuilder extends PhaseDefinitionProcessorBuilder<StaticConfiguration> {

	public StaticConfigurationPhaseProcessor buildProcessor(StaticConfiguration annotation, Method method) {
		return new StaticConfigurationPhaseProcessor(annotation);
	}
}
