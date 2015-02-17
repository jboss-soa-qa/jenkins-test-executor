package org.jboss.qa.jenkins.test.executor.utils;

import java.util.HashMap;
import java.util.Map;

import groovy.util.AntBuilder;

public class AntEr {

	private static final AntBuilder ANT = new AntBuilder();

	public static AntEr build() {
		return new AntEr();
	}

	private Map<String, String> params = new HashMap<>(5);

	public AntEr param(String name, String value) {
		params.put(name, value);
		return this;
	}

	public Object invoke(String method) {
		return ANT.invokeMethod(method, params);
	}
}
