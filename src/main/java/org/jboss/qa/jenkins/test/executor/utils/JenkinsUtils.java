package org.jboss.qa.jenkins.test.executor.utils;

import java.io.File;
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
	 * <p/>
	 * <p>Locations:
	 * <ul>
	 * <li>System properties</li>
	 * <li>Environment properties</li>
	 * </ul>
	 * <p>Searched formats:
	 * <ul>
	 * <li>my.great.property
	 * <li>my_great_property
	 * <li>MY_GREAT_PROPERTY
	 * <li>myGreatPorperty
	 * </ul>
	 *
	 * @param name Property name
	 * @param defaultValue Default value, if property was not found
	 */
	public static String getUniversalProperty(String name, String defaultValue) {
		String result = defaultValue;
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
		return result;
	}

	public static String getUniversalProperty(String name) {
		return getUniversalProperty(name, null);
	}

	public static File getMavenHome() {
		File mavenHome = null;
		if (getUniversalProperty("m2.home") != null) {
			mavenHome = new File(getUniversalProperty("m2.home"));
		} else if (getUniversalProperty("maven.home") != null) {
			mavenHome = new File(getUniversalProperty("maven.home"));
		} else {
			final String mavenVersion = getUniversalProperty("maven.version", "3.2.5");
			final String commonTools = getUniversalProperty("common.tools", "/qa/tools/opt");
			mavenHome = new File(commonTools, "apache-maven-" + mavenVersion);
		}
		if (!mavenHome.exists()) {
			log.warn("Maven home does not exist: " + mavenHome.getAbsolutePath());
		}
		return mavenHome;
	}

	public static File getJavaHome() {
		final File javaHome = new File(JenkinsUtils.getUniversalProperty("java.home"));
		if (!javaHome.exists()) {
			log.warn("Java home does not exist: " + javaHome.getAbsolutePath());
		}
		return javaHome;
	}
}
