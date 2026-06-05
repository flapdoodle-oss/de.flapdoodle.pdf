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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntRangeTest {

	@Test
	void mapToFloat() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int start = random.nextInt(1, 10);
		int size = random.nextInt(1, 10);

		IntRange testee = random.nextBoolean()
			? IntRange.to(start, start + size - 1)
			: IntRange.until(start, start + size);

		float[] result = testee.mapToFloat(index -> (float) index);
		assertThat(result).hasSize(size);
		for (int i = 0; i < result.length; i++) {
			assertThat(result[i]).isEqualTo((float) i + start);
		}
	}

	@Nested
	class OpenTest {
		@Test
		void rangeCheck() {
			assertThatThrownBy(() -> {
				IntRange.until(0, 0);
			}).isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void hashCodeEquals() {
			IntRange.Open a = IntRange.until(0, 1);
			IntRange.Open b = IntRange.until(0, 2);
			IntRange.Open c = IntRange.until(0, 1);

			assertThat(a).isNotEqualTo(b);
			assertThat(a).isEqualTo(c);
			assertThat(b).isNotEqualTo(a);
			assertThat(b).isNotEqualTo(c);
			assertThat(c).isEqualTo(a);
			assertThat(c).isNotEqualTo(b);

			assertThat(a.hashCode()).isEqualTo(c.hashCode());
		}

		@Test
		void closedOpenAgain() {
			IntRange.Open start = IntRange.until(0, 1);
			assertThat(start.asClosed().asOpen()).isEqualTo(start);
			assertThat(IntRange.Open.EMPTY.asClosed().asOpen()).isEqualTo(IntRange.Open.EMPTY);
		}

		@Test
		void closedIsNotOpen() {
			IntRange.Open start = IntRange.until(0, 1);
			assertThat(start.asClosed()).isNotEqualTo(start);
			assertThat(start.asClosed()).isNotEqualTo(IntRange.to(0, 1));
		}

		@Test
		void iterablesAndStream() {
			IntRange.Open range = IntRange.until(0, 5);
			List<Integer> streamAsList = range.stream().boxed().toList();
			List<Integer> interatorAsList = StreamSupport.stream(range.spliterator(), false).toList();

			assertThat(streamAsList).containsExactly(0, 1, 2, 3, 4);
			assertThat(interatorAsList).containsExactly(0, 1, 2, 3, 4);
			assertThat(range.size()).isEqualTo(5);
		}

		@Test
		void contains() {
			IntRange.Open testee = IntRange.until(0, 2);
			
			assertThat(testee.contains(0)).isTrue();
			assertThat(testee.contains(1)).isTrue();
			assertThat(testee.contains(testee)).isTrue();

			assertThat(testee.contains(IntRange.until(0, 1))).isTrue();
			assertThat(testee.contains(IntRange.until(1, 2))).isTrue();
			assertThat(testee.contains(IntRange.until(1, 3))).isFalse();
		}
	}

	@Nested
	class ClosedTest {
		@Test
		void rangeCheck() {
			assertThatThrownBy(() -> IntRange.to(0, -1)).isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void hashCodeEquals() {
			IntRange.Closed a = IntRange.to(0, 1);
			IntRange.Closed b = IntRange.to(0, 2);
			IntRange.Closed c = IntRange.to(0, 1);

			assertThat(a).isNotEqualTo(b);
			assertThat(a).isEqualTo(c);
			assertThat(b).isNotEqualTo(a);
			assertThat(b).isNotEqualTo(c);
			assertThat(c).isEqualTo(a);
			assertThat(c).isNotEqualTo(b);

			assertThat(a.hashCode()).isEqualTo(c.hashCode());
		}

		@Test
		void openClosedAgain() {
			IntRange.Closed start = IntRange.to(0, 1);
			assertThat(start.asOpen().asClosed()).isEqualTo(start);
			assertThat(IntRange.Closed.EMPTY.asOpen().asClosed()).isEqualTo(IntRange.Closed.EMPTY);
		}

		@Test
		void closedIsNotOpen() {
			IntRange.Closed start = IntRange.to(0, 1);
			assertThat(start.asOpen()).isNotEqualTo(start);
			assertThat(start.asOpen()).isNotEqualTo(IntRange.until(0, 1));
		}

		@Test
		void iterablesAndStream() {
			IntRange.Closed range = IntRange.to(0, 5);
			List<Integer> streamAsList = range.stream().boxed().toList();
			List<Integer> interatorAsList = StreamSupport.stream(range.spliterator(), false).toList();

			assertThat(streamAsList).containsExactly(0, 1, 2, 3, 4, 5);
			assertThat(interatorAsList).containsExactly(0, 1, 2, 3, 4, 5);
			assertThat(range.size()).isEqualTo(6);
		}

		@Test
		void contains() {
			IntRange.Closed testee = IntRange.to(0, 2);

			assertThat(testee.contains(0)).isTrue();
			assertThat(testee.contains(2)).isTrue();
			assertThat(testee.contains(testee)).isTrue();

			assertThat(testee.contains(IntRange.to(0, 1))).isTrue();
			assertThat(testee.contains(IntRange.to(1, 2))).isTrue();
			assertThat(testee.contains(IntRange.to(1, 3))).isFalse();
		}
	}
}