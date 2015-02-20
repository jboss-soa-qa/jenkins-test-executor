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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MavenCli {

	protected final File mavenHome;
	protected final File javaHome;
	protected final String xms;
	protected final String xmx;
	protected final String maxPermSize;
	protected final File pom;
	protected final boolean alsoMake;
	protected final boolean failAtEnd;
	protected final List<String> goals;
	protected final List<String> profiles;
	protected final List<String> projects;
	protected final List<String> mavenOpts;
	protected final Map<String, String> sysProps;
	protected final List<String> params;

	private MavenCli(Builder builder) {
		// Mandatory properties
		checkMandatoryProperty("mavenHome", mavenHome = builder.mavenHome);
		checkMandatoryProperty("javaHome", javaHome = builder.javaHome);
		checkMandatoryProperty("pom", pom = builder.pom);
		checkMandatoryProperty("goals", goals = builder.goals);
		// Optional properties
		xms = builder.xms;
		xmx = builder.xmx;
		maxPermSize = builder.maxPermSize;
		mavenOpts = builder.mavenOpts;
		sysProps = builder.sysProps;
		profiles = builder.profiles;
		projects = builder.projects;
		params = builder.params;
		alsoMake = builder.alsoMake;
		failAtEnd = builder.failAtEnd;
	}

	protected void checkMandatoryProperty(String name, Object value) {
		if (value == null) {
			throw new IllegalArgumentException(String.format("Property '%s' is mandatory", name));
		}
	}

	public String getXms() {
		return xms;
	}

	public String getXmx() {
		return xmx;
	}

	public String getMaxPermSize() {
		return maxPermSize;
	}

	public List<String> getParams() {
		return params;
	}

	public File getMavenHome() {
		return mavenHome;
	}

	public File getJavaHome() {
		return javaHome;
	}

	public File getPom() {
		return pom;
	}

	public boolean isAlsoMake() {
		return alsoMake;
	}

	public boolean isFailAtEnd() {
		return failAtEnd;
	}

	public List<String> getMavenOpts() {
		return mavenOpts;
	}

	public Map<String, String> getSysProps() {
		return sysProps;
	}

	public List<String> getGoals() {
		return goals;
	}

	public List<String> getProfiles() {
		return profiles;
	}

	public List<String> getProjects() {
		return projects;
	}

	public void run() throws Exception {
		final List<String> cmd = new ArrayList<>();

		// Maven
		cmd.add("/bin/bash");
		cmd.add(mavenHome + "/bin/mvn");

		// Maven opts
		if (xms != null) {
			mavenOpts.add("-Xms" + xms);
		}
		if (xmx != null) {
			mavenOpts.add("-Xmx" + xmx);
		}
		if (maxPermSize != null) {
			mavenOpts.add("-XX:MaxPermSize=" + maxPermSize);
		}

		// Path to POM file
		cmd.add("-f");
		cmd.add(pom.getAbsolutePath());

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

		final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
		processBuilder.environment().put("JAVA_HOME", javaHome.getAbsolutePath());
		processBuilder.environment().put("M2_HOME", mavenHome.getAbsolutePath());
		processBuilder.environment().put("MAVEN_OTPS", StringUtils.join(mavenOpts, " "));

		log.debug("===========");
		log.debug("Process arguments: " + cmd.toString());
		log.debug("JAVA_HOME={}", processBuilder.environment().get("JAVA_HOME"));
		log.debug("M2_HOME={}", processBuilder.environment().get("M2_HOME"));
		log.debug("MAVEN_OTPS={}", processBuilder.environment().get("MAVEN_OTPS"));

		final Process process = processBuilder.start();
		process.waitFor();

		if (process.exitValue() != 0) {
			log.error("Maven execution failed with exit code: " + process.exitValue());
		}
	}

	public static class Builder {
		private File mavenHome;
		private File javaHome;
		private String xms;
		private String xmx;
		private String maxPermSize;
		private File pom;
		private boolean alsoMake;
		private boolean failAtEnd;
		private List<String> goals;
		private List<String> profiles;
		private List<String> projects;
		private List<String> mavenOpts;
		private Map<String, String> sysProps;
		private List<String> params;

		public Builder() {
			alsoMake = false;
			failAtEnd = false;
			xms = "64m";
			xmx = "256m";
			maxPermSize = "512m";
			goals = new ArrayList<>();
			profiles = new ArrayList<>();
			projects = new ArrayList<>();
			mavenOpts = new ArrayList<>();
			sysProps = new HashMap<>();
			params = new ArrayList<>();
		}

		public Builder mavenHome(File mavenHome) {
			this.mavenHome = mavenHome;
			return this;
		}

		public Builder javaHome(File javaHome) {
			this.javaHome = javaHome;
			return this;
		}

		public Builder xms(String xms) {
			this.xms = xms;
			return this;
		}

		public Builder xmx(String xmx) {
			this.xmx = xmx;
			return this;
		}

		public Builder maxPermSize(String maxPermSize) {
			this.maxPermSize = maxPermSize;
			return this;
		}

		public Builder alsoMake(boolean alsoMake) {
			this.alsoMake = alsoMake;
			return this;
		}

		public Builder failAtEnd(boolean failAtEnd) {
			this.failAtEnd = failAtEnd;
			return this;
		}

		public Builder pom(File pom) {
			this.pom = pom;
			return this;
		}

		public Builder pom(String pom) {
			this.pom = new File(pom);
			return this;
		}

		public Builder goals(String... goals) {
			this.goals.addAll(Arrays.asList(goals));
			return this;
		}

		public Builder profiles(String... profiles) {
			this.profiles.addAll(Arrays.asList(profiles));
			return this;
		}

		public Builder projects(String... projects) {
			this.projects.addAll(Arrays.asList(projects));
			return this;
		}

		public Builder mavenOpts(String... mavenOpts) {
			this.mavenOpts.addAll(Arrays.asList(mavenOpts));
			return this;
		}

		public Builder sysProp(String key, String value) {
			this.sysProps.put(key, value);
			return this;
		}

		public Builder params(String... params) {
			this.params.addAll(Arrays.asList(params));
			return this;
		}

		public MavenCli build() {
			return new MavenCli(this);
		}
	}
}
