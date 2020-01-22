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
package org.jboss.qa.jenkins.test.executor.maven;

import org.jboss.qa.jenkins.test.executor.utils.MavenCli;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;

public class MavenCliTest {

	@Test
	public void receiveVersionOfMaven() throws Exception {
		Assert.assertEquals(MavenCli.builder().binary("mvnw").goal("-version").build().run(), 0);
	}

	@Test
	public void useMavenOptsProperty() throws Exception {
		final String key = "testProp";
		final String value = "value";

		// Create a stream to hold the output
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		System.setOut(ps);
		Assert.assertEquals(MavenCli.builder().binary("mvnw").mavenOpts(new HashSet<String>(Arrays.asList(new String [] {String.format("-D%s=%s", key, value)}))).goal("help:evaluate").sysProp("expression", key).build().run(), 0);
		Assert.assertTrue(baos.toString().contains(value));
	}
}
