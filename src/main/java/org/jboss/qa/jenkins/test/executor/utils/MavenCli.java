/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.jenkins.test.executor.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class MavenCli {

	@Builder.Default
	private final String binary = "mvn";
	private final File mavenHome;
	private final File javaHome;
	private final File userDir;
	@Builder.Default
	private final String xms = "64m";
	@Builder.Default
	private final String xmx = "256m";
	private final String minPermSize;
	private final String maxPermSize;
	private final File pom;
	private final boolean alsoMake;
	private final boolean alsoMakeDependents;
	private final boolean debug;
	private final boolean nonRecursive;
	private final boolean failAtEnd;
	@Singular
	@NonNull
	private final List<String> goals;
	@Singular
	private final Set<String> profiles;
	@Singular
	private final Set<String> projects;
	@Singular
	private final Set<String> mavenOpts;
	@Singular
	private final Map<String, String> sysProps;
	@Singular
	private final List<String> params;
	private final File settings;

	private List<String> generateCommand() {
		final List<String> cmd = new ArrayList<>();

		// Maven
		if (SystemUtils.IS_OS_WINDOWS) {
			cmd.add("cmd");
			cmd.add("/c");
			String mvnCommand = null;
			if (mavenHome != null) {
				final String bin = mavenHome + File.separator + "bin" + File.separator;
				mvnCommand = new File(bin + binary + ".bat").exists() ? bin + binary + ".bat" : bin + binary + ".cmd";
			}
			cmd.add(mvnCommand != null ? mvnCommand : "mvn");
		} else {
			cmd.add("bash");
			cmd.add(mavenHome != null ? mavenHome + File.separator + "bin" + File.separator + binary : binary);
		}

		// Path to POM file
		if (pom != null) {
			cmd.add("-f");
			cmd.add(pom.getAbsolutePath());
		} else {
			log.warn("Pom file has not been configured");
		}

		// Settings.xml
		if (settings != null) {
			cmd.add("-s");
			cmd.add(settings.getAbsolutePath());
			log.warn("Using custom settings file");
		}

		cmd.addAll(goals);

		// Profiles
		if (!profiles.isEmpty()) {
			cmd.add("-P" + StringUtils.join(profiles, ","));
		}

		// Projects
		if (!projects.isEmpty()) {
			cmd.add("-pl");
			cmd.add(StringUtils.join(projects, ","));
		}

		// If project list is specified, also build projects required by the list
		if (alsoMake) {
			cmd.add("-am");
		}

		// If project list is specified, also build projects that depend on projects on the list
		if (alsoMakeDependents) {
			cmd.add("-amd");
		}

		// Produce execution debug output
		if (debug) {
			cmd.add("-X");
		}

		if (nonRecursive) {
			cmd.add("-N");
		}

		// Only fail the build afterwards; allow all non-impacted builds to continue
		if (failAtEnd) {
			cmd.add("-fae");
		}

		// System properties
		for (Map.Entry<String, String> entry : sysProps.entrySet()) {
			cmd.add(String.format("-D%s=%s", entry.getKey(), entry.getValue()));
		}

		if (params != null) {
			cmd.addAll(params);
		}
		log.info("Process arguments: " + cmd.toString());
		return cmd;
	}

	private String buildMavenOpts() {
		final Set<String> newMvnOpts = new HashSet<>(mavenOpts);
		// Maven opts
		if (xms != null) {
			newMvnOpts.add("-Xms" + xms);
		}
		if (xmx != null) {
			newMvnOpts.add("-Xmx" + xmx);
		}
		if (minPermSize != null) {
			newMvnOpts.add("-XX:PermSize=" + minPermSize);
		}
		if (maxPermSize != null) {
			newMvnOpts.add("-XX:MaxPermSize=" + maxPermSize);
		}
		return StringUtils.join(newMvnOpts, " ");
	}

	public int run() throws Exception {

		final ProcessBuilder processBuilder = new ProcessBuilder(generateCommand());
		if (userDir != null && userDir.exists()) {
			processBuilder.directory(userDir);
		}
		processBuilder.environment().putAll(System.getenv());
		if (javaHome != null) {
			processBuilder.environment().put("JAVA_HOME", javaHome.getAbsolutePath());
		}
		if (mavenHome != null) {
			processBuilder.environment().put("M2_HOME", mavenHome.getAbsolutePath());
		}
		processBuilder.environment().put("MAVEN_OPTS", buildMavenOpts());

		log.info("JAVA_HOME={}", processBuilder.environment().get("JAVA_HOME"));
		log.info("M2_HOME={}", processBuilder.environment().get("M2_HOME"));
		log.info("MAVEN_OPTS={}", processBuilder.environment().get("MAVEN_OPTS"));

		return new SyncProcessRunner().run(processBuilder);
	}
}
