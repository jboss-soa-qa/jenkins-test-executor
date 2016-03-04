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
package org.jboss.qa.jenkins.test.executor.property;

import org.jboss.qa.jenkins.test.executor.utils.JenkinsUtils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UniversalPropertyPriorityTest {

	@DataProvider(name = "forms")
	public static Object[][] invalidForms() {
		return new Object[][] {
				{"test", true},
				{"test123", true},
				{"test.prop", true},
				{"test_prop", false},
				{"TEST_PROP", false},
				{"testProp", false},
				{"test-prop", false},
		};
	}

	@Test(dataProvider = "forms")
	public void propertyFormTest(String form, Boolean valid) {
		try {
			JenkinsUtils.getUniversalProperty(form);
			if (!valid) {
				Assert.fail(String.format("Should be invalid: %s", form));
			}
		} catch (IllegalArgumentException e) {
			if (valid) {
				Assert.fail(String.format("Should be valid: %s", form));
			}
		}
	}

	@Test
	public void propertyPriotityTest() {
		// System properties and environment variables are defined in maven-surefire-plugin.
		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.a"), "as1");
		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.b"), "bs1");
		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.c"), "cs1");
		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.d"), "ds1");

		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.e"), "ee1");
		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.f"), "fe1");
		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.g"), "ge1");
		Assert.assertEquals(JenkinsUtils.getUniversalProperty("test.prop.h"), "he1");
	}
}
