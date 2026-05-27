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
package de.flapdoodle.pdf.grid.tablesplitter.posterize;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColumnWidthsTest {

	@Test
	void failOnMissingColumns() {
		assertThatThrownBy(() -> {
			ColumnWidths.ofMap(Map.of(0, 0.0f, 2, 2.f));
		}).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("there are missing columns: ");
	}

	@Test
	void singleColumnFit() {
		int start = randomInt();
		ColumnWidths testee = ColumnWidths.ofMap(Map.of(start, 123.0f));

		assertThat(testee.lastFittingColumn(start, 123.0f))
			.isPresent()
			.hasValue(0);
	}

	@Test
	void singleColumnFitIfNearby() {
		int start = randomInt();
		ColumnWidths testee = ColumnWidths.ofMap(Map.of(start, 123.0012f));

		assertThat(testee.lastFittingColumn(start, 123.0f))
			.isPresent()
			.hasValue(0);
	}

	@Test
	void singleColumnNoFitting() {
		int start = randomInt();
		ColumnWidths testee = ColumnWidths.ofMap(Map.of(start, 130.0f));

		assertThat(testee.lastFittingColumn(start, 123.0f))
			.isEmpty();
	}

	@Test
	void multipleColumnsFitting() {
		int start = randomInt();
		ColumnWidths testee = ColumnWidths.ofMap(Map.of(
			start, 10.0f,
			start + 1, 20.f,
			start + 2, 30.0f,
			start + 3, 40.0f,
			start + 4, 0.1f,
			start + 5, 0.1f,
			start + 6, 0.1f
		));

		assertThat(testee.lastFittingColumn(start, 90.0f))
			.isPresent()
			.hasValue(2);

		assertThat(testee.lastFittingColumn(start, 100.0f))
			.isPresent()
			.hasValue(3);

		assertThat(testee.lastFittingColumn(start, 100.01f))
			.isPresent()
			.hasValue(3);

		assertThat(testee.lastFittingColumn(start, 100.1f))
			.isPresent()
			.hasValue(4);
	}

	private static int randomInt() {
		return ThreadLocalRandom.current().nextInt(0, 100);
	}
}