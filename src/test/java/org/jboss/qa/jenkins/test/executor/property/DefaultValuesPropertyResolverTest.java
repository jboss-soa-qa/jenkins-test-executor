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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DefaultValuesPropertyResolverTest {

	private static final String VAL = ".VAL";

	private PropertyReplacer replacer = new DefaultValuesPropertyReplacer(new PropertyResolver() {
		@Override
		public String resolve(String name) {
			if (name.endsWith("null")) {
				return null;
			}
			return name + VAL;
		}
	});

	@DataProvider(name = "validExpressions")
	public static Object[][] validExpressions() {
		return new Object[][] {
				{
						"Expression ${property.a} with ${property.b} properties.",
						"Expression property.a" + VAL + " with property.b" + VAL + " properties."
				},
				{
						"Expression ${property.a}${property.b} with properties.",
						"Expression property.a" + VAL + "property.b" + VAL + " with properties."
				},
				{
						"Expression ${property.a}",
						"Expression property.a" + VAL
				},
				{
						"${property.a} expression.",
						"property.a" + VAL + " expression."
				},
				{
						"Expression ${property.a.null} with ${property.b.null} properties.",
						"Expression  with  properties."
				},
				{
						"Expression ${property.a with ${property.b.null} properties.",
						"Expression  properties."
				},
		};
	}

	@Test(dataProvider = "validExpressions")
	public void shouldEvaluate(String expression, String evaluated) {
		Assert.assertEquals(replacer.replace(expression), evaluated);
	}

	@DataProvider(name = "validExpressionsWithDefaultValues")
	public static Object[][] validExpressionsWithDefaultValues() {
		return new Object[][] {
				{
						"Expression ${property.a.null:Val1} with ${property.b.null:Val2} properties.",
						"Expression Val1 with Val2 properties."
				},
				{
						"Expression ${property.a.null:Val3}${property.b:Val4} with properties.",
						"Expression Val3property.b" + VAL + " with properties."
				},
				{
						"Expression ${property.a.null:Val5}",
						"Expression Val5"
				},
				{
						"${property.a.null:Val6} expression.",
						"Val6 expression."
				},
				{
						"Expression ${property.a with ${property.b.null:Val7} properties.",
						"Expression Val7 properties."
				},
		};
	}

	@Test(dataProvider = "validExpressionsWithDefaultValues")
	public void shouldEvaluateWithDefaultValues(String expression, String evaluated) {
		Assert.assertEquals(replacer.replace(expression), evaluated);
	}

	@DataProvider(name = "invalidExpressions")
	public static Object[][] invalidExpressions() {
		return new Object[][] {
				{"Expression ${property.a"},
				{"Expression ${property.a.null:Val5"}
		};
	}

	@Test(dataProvider = "invalidExpressions", expectedExceptions = IllegalStateException.class)
	public void shouldThrowInvalidStateException(String expression) {
		replacer.replace(expression);
	}

	@DataProvider(name = "escapedBoundary")
	public static Object[][] escapedBoundary() {
		return new Object[][] {
				{
						"Expression $${property.a} without properties.",
						"Expression ${property.a} without properties."
				},
				{
						"Expression $$ without properties.",
						"Expression $ without properties."
				},
				{
						"Expression $$ with properties ${property.a}.",
						"Expression $ with properties property.a" + VAL + "."
				}
		};
	}

	@Test(dataProvider = "escapedBoundary")
	public void shouldRecognizeBoundaryCharacterEscaping(String expression, String evaluated) {
		Assert.assertEquals(replacer.replace(expression), evaluated);
	}

	@DataProvider(name = "invalidBoundary")
	public static Object[][] invalidBoundary() {
		return new Object[][] {
				{
						"Expression $ without properties.",
						"Expression  without properties."
				},
				{
						"Expression$",
						"Expression"
				},
				{
						"Expression $ with properties ${property.a}.",
						"Expression  with properties property.a" + VAL + "."
				}
		};
	}

	@Test(dataProvider = "invalidBoundary")
	public void shouldIgnoreInvalidBoundary(String expression, String evaluated) {
		Assert.assertEquals(replacer.replace(expression), evaluated);
	}
}
