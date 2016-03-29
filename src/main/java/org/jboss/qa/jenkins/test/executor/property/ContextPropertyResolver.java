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

import org.jboss.qa.phaser.context.Context;
import org.jboss.qa.phaser.registry.InstanceRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextPropertyResolver implements PropertyResolver {

	private InstanceRegistry registry;

	public ContextPropertyResolver(InstanceRegistry registry) {
		this.registry = registry;
	}

	@Override
	public String resolve(String name) {
		String value = null;
		for (Context context : registry.get(Context.class)) {
			value = context.get(name, String.class);
			if (value != null) {
				break;
			}
		}
		return value;
	}
}
