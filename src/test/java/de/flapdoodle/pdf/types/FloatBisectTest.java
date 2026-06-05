/*
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.pdf.types;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloatBisectTest {

	@Test
	void validBisect() {
		FloatBisect testee = new FloatBisect(1.0f, 2.0f, 3.0f, 0.51f);

		assertThat(testee.isCloseEnough()).isFalse();

		FloatBisect upperLowerhalf = testee.lowerHalf().upperHalf();
		
		assertThat(upperLowerhalf.lower()).isCloseTo(1.5f, Percentage.withPercentage(1.0));
		assertThat(upperLowerhalf.upper()).isCloseTo(2f, Percentage.withPercentage(1.0));
		assertThat(upperLowerhalf.isCloseEnough()).isTrue();
	}

	@Test
	void invalidBisect() {
		assertThatThrownBy(() -> new  FloatBisect(1.0f, 2.0f, 3.0f, 0.0f))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new  FloatBisect(1.0f, 1.0f, 1.1f, 1.0f))
			.isInstanceOf(IllegalArgumentException.class);
	}
}