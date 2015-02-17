package org.jboss.qa.jenkins.test.executor.phase.start;

import org.jboss.qa.phaser.PhaseDefinitionProcessorBuilder;

import java.lang.reflect.Method;

public class StartPhaseProcessorBuilder extends PhaseDefinitionProcessorBuilder<Start> {

	public StartPhaseProcessor buildProcessor(Start annotation, Method method) {
		return new StartPhaseProcessor(annotation);
	}
}
