package org.jboss.qa.jenkins.test.executor.phase.runtimeconfiguration;

import org.jboss.qa.phaser.PhaseDefinitionProcessorBuilder;

import java.lang.reflect.Method;

public class RuntimeConfigurationPhaseProcessorBuilder extends PhaseDefinitionProcessorBuilder<RuntimeConfiguration> {

	public RuntimeConfigurationPhaseProcessor buildProcessor(RuntimeConfiguration annotation, Method method) {
		return new RuntimeConfigurationPhaseProcessor(annotation);
	}
}
