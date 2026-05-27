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

import de.flapdoodle.pdf.types.Range;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PutColumnIntoSlotIfAllOfItWillFitTest {
	private final PutColumnIntoSlotIfAllOfItWillFit testee = new PutColumnIntoSlotIfAllOfItWillFit();

	private static float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	@Test
	void firstColumnWillFitInFirstSlot() {
		var columnWidth = nextFloat() * 999.9f + 0.1f;

		var columnWidths = Map.of(0, columnWidth);
		var slotWidths = List.of(columnWidth * 1.1f, columnWidth);

		var result = testee.map(columnWidths, slotWidths);
		assertThat(result).containsExactly(new Range(0, 0));
	}

	@Test
	void secondCellPutIntoSecondSlot() {
		var columnWidth = nextFloat() * 999.9f + 0.1f;

		var columnWidths = Map.of(0, columnWidth, 1, columnWidth);
		var slotWidths = List.of(columnWidth * 1.99f, columnWidth);

		var result = testee.map(columnWidths, slotWidths);
		assertThat(result).containsExactly(new Range(0, 0), new Range(1, 1));
	}

	@Test
	void tenColumnsInto3SlotsSample() {
		var columnWidth = nextFloat() * 999.9f + 0.1f;

		var columnWidths = IntStream.range(0, 10)
			.boxed()
			.collect(Collectors.toMap(it->it, it->columnWidth));

		var slotWidths = List.of(columnWidth * 3.1f, columnWidth * 3.1f, columnWidth * 3.11f, columnWidth * 3.11f);

		var result = testee.map(columnWidths, slotWidths);
		assertThat(result).containsExactly(new Range(0, 2), new Range(3, 5), new Range(6, 8), new Range(9, 9));
	}

	@Test
	void failIfColumnsLeft() {
		var columnWidth = nextFloat() * 999.9f + 0.1f;

		var columnWidths = Map.of(0, columnWidth, 1, columnWidth, 2,  columnWidth*0.1f);
		var slotWidths = List.of(columnWidth * 1.99f, columnWidth);

		assertThatThrownBy(() -> testee.map(columnWidths, slotWidths))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("columns left: 2 (last: 2)");
	}
}