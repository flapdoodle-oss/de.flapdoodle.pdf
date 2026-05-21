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
package de.flapdoodle.pdf.tables;

import de.flapdoodle.pdf.types.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TablesTest {
	@Nested
	class Widths {

		@Test
		@DisplayName("invalid parameter must fail")
		void invalidParameterMustFail() {
			assertThatThrownBy(() -> {
				Tables.columnWidths(Range.EMPTY, 30f, ColumnWeights.EMPTY);
			})
				.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("every column has same with if no weight is set")
		void everyColumnHasSameWithIfNoWeightIsSet() {
			var columns = ThreadLocalRandom.current().nextInt(1, 1001);
			var width = ThreadLocalRandom.current().nextFloat() * 999.9f + 0.1f;

			var result = Tables.columnWidths(new Range(0, columns - 1), width, ColumnWeights.EMPTY);

			assertThat(result).hasSize(columns);
			result.values().forEach(value -> {
				assertThat(value).isEqualTo(width / columns);
			});
		}

		@Test
		@DisplayName("column get width from weight property")
		void columnGetWidthFromWeightProperty() {
			var width = ThreadLocalRandom.current().nextFloat() * 999.9f + 0.1f;

			var columnWeights = ColumnWeights.fromMap(Map.of(0 , 1f, 1 , 2f, 2 , 4f));
			var result = Tables.columnWidths(new Range(0, 2), width, columnWeights);

			assertThat(result).hasSize(3);
			assertThat(result.get(0)).isEqualTo(width / 7f);
			assertThat(result.get(1)).isEqualTo((width * 2f) / 7f);
			assertThat(result.get(2)).isEqualTo((width * 4f) / 7f);
		}
	}
}