package org.jboss.qa.jenkins.test.executor.phase.stop;

import org.jboss.qa.phaser.PhaseDefinitionProcessorBuilder;

import java.lang.reflect.Method;

public class StopPhaseProcessorBuilder extends PhaseDefinitionProcessorBuilder<Stop> {

	public StopPhaseProcessor buildProcessor(Stop annotation, Method method) {
		return new StopPhaseProcessor(annotation);
	}
}
