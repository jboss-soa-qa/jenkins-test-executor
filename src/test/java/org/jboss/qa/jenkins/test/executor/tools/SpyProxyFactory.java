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
package org.jboss.qa.jenkins.test.executor.tools;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.attribute.MethodAttributeAppender;

public final class SpyProxyFactory {

	private SpyProxyFactory() {
	}

	public static <T> T createProxy(Class<T> tClass, T mockInstance) throws IllegalAccessException, InstantiationException {
		return new ByteBuddy()
				.subclass(tClass)
				.method(isDeclaredBy(tClass))
				.intercept(MethodDelegation.to(mockInstance).andThen(SuperMethodCall.INSTANCE))
				.attribute(MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER)
				.make()
				.load(tClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
				.getLoaded()
				.newInstance();
	}
}
