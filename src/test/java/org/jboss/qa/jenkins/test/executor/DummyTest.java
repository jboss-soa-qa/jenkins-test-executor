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
package org.jboss.qa.jenkins.test.executor;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import org.jboss.qa.jenkins.test.executor.tools.SpyProxyFactory;
import org.jboss.qa.phaser.context.Context;
import org.jboss.qa.phaser.context.SimpleContext;
import org.jboss.qa.phaser.registry.InstanceRegistry;
import org.jboss.qa.phaser.registry.SimpleInstanceRegistry;

import org.mockito.InOrder;
import org.testng.annotations.Test;

import java.util.Map;

public class DummyTest {

	@Test
	public void testExecution() throws Exception {
		final InstanceRegistry registry = new SimpleInstanceRegistry();
		final Context context = new SimpleContext();
		for (Map.Entry<Object, Object> p : System.getProperties().entrySet()) {
			context.set((String) p.getKey(), p.getValue());
		}
		registry.insert(context);

		final DummyJob mock = mock(DummyJob.class);
		final DummyJob proxy = SpyProxyFactory.createProxy(DummyJob.class, mock);
		new JenkinsTestExecutor(proxy).run(registry);

		final InOrder order = inOrder(mock);
		order.verify(mock, times(1)).beforeJob();
		order.verify(mock, times(1)).testPropertyInjection();
		order.verify(mock, times(1)).prepareContainer();
		order.verify(mock, times(1)).startContainer();
		order.verify(mock, times(1)).runtimeSetup();
		order.verify(mock, times(1)).runtimeTeardown();
		order.verify(mock, times(1)).stopContainer();
		order.verify(mock, times(1)).afterJob();
		order.verifyNoMoreInteractions();
	}
}
