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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JenkinsUtils {

	private JenkinsUtils() {
	}

	/**
	 * Get property.
	 *
	 * <p>Priority list (according to forms and locations):
	 * <ol>
	 * <li>my.great.property (system property)
	 * <li>my.great.property (ENV variable)
	 * <li>my_great_property (system property)
	 * <li>my_great_property (ENV variable)
	 * <li>MY_GREAT_PROPERTY (system property)
	 * <li>MY_GREAT_PROPERTY (ENV variable)
	 * <li>myGreatPorperty (system property)
	 * <li>myGreatPorperty (ENV variable)
	 * </ol>
	 *
	 * @param name Property name
	 * @param defaultValue Default value, if property was not found
	 */
	public static String getUniversalProperty(String name, String defaultValue) {
		String result = null;
		final String re = "[a-z0-9]+(\\.[a-z0-9]+)*";
		if (!Pattern.compile(re).matcher(name).matches()) {
			throw new IllegalArgumentException(String.format("Property '%s' should match pattern '%s'", name, re));
		}
		final List<String> forms = new ArrayList<>();
		forms.add(name); // form: "abc.def.ghi"
		final String underscoreForm = name.replaceAll("\\.", "_");
		forms.add(underscoreForm); // form: "abc_def_ghi"
		forms.add(underscoreForm.toUpperCase()); // form: "ABC_DEF_GHI"
		final StringBuilder sb = new StringBuilder();
		for (String part : name.split("\\.")) {
			if (sb.length() == 0) {
				sb.append(part);
			} else {
				sb.append(part.substring(0, 1).toUpperCase() + part.substring(1));
			}
		}
		forms.add(sb.toString()); // form: "abcDefGhi"
		for (String form : forms) {
			if (System.getProperty(form) != null) { // Search in system properties
				result = System.getProperty(form);
			} else if (System.getenv(form) != null) { // search in environment properties
				result = System.getenv(form);
			}
			if (result != null) {
				break;
			}
		}
		return result == null ? defaultValue : result;
	}

	public static String getUniversalProperty(String name) {
		return getUniversalProperty(name, null);
	}
}
