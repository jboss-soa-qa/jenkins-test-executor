package org.jboss.qa.jenkins.test.executor.phase.maven;

import org.jboss.qa.phaser.PhaseDefinitionProcessorBuilder;

import java.lang.reflect.Method;

public class MavenPhaseProcessorBuilder extends PhaseDefinitionProcessorBuilder<Maven> {

	public MavenPhaseProcessor buildProcessor(Maven annotation, Method method) {
		return new MavenPhaseProcessor(annotation);
	}
}
