package org.jboss.qa.jenkins.test.executor.phase.download;

import org.jboss.qa.phaser.PhaseDefinitionProcessorBuilder;

import java.lang.reflect.Method;

public class DownloadPhaseProcessorBuilder extends PhaseDefinitionProcessorBuilder<Download> {

	public DownloadPhaseProcessor buildProcessor(Download annotation, Method method) {
		return new DownloadPhaseProcessor(annotation);
	}
}
