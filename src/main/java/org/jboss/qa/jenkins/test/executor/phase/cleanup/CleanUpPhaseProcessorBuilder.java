package org.jboss.qa.jenkins.test.executor.phase.cleanup;

import org.jboss.qa.phaser.PhaseDefinitionProcessorBuilder;

import java.lang.reflect.Method;

public class CleanUpPhaseProcessorBuilder extends PhaseDefinitionProcessorBuilder<CleanUp> {

	public CleanUpPhaseProcessor buildProcessor(CleanUp annotation, Method method) {
		return new CleanUpPhaseProcessor(annotation);
	}
}
