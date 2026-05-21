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
package de.flapdoodle.pdf.extensions;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapExtensionsTest {

	@Test
	void mapEntries() {
		Map<String, Integer> result = MapExtensions.indexedBy(List.of(1, 2, 3), it -> "" + it);

		assertThat(result)
			.containsEntry("1", 1)
			.containsEntry("2", 2)
			.containsEntry("3", 3)
			.hasSize(3);
	}

	@Test
	void failOnKeyCollisions() {
		assertThatThrownBy(() -> MapExtensions.indexedBy(List.of(1, 2, 1), it -> "" + it))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("duplicate key 1(1) <-> 1(1)");
	}
}