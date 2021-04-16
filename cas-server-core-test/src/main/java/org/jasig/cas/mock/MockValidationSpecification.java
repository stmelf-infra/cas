/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.mock;

import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ValidationSpecification;

/**
 * Class to test the Runtime exception thrown when there is no default constructor on a ValidationSpecification.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public class MockValidationSpecification implements ValidationSpecification {

	private boolean test;

	public MockValidationSpecification(final boolean test) {
		this.test = test;
	}

	public boolean isSatisfiedBy(final Assertion assertion) {
		return this.test;
	}
}
