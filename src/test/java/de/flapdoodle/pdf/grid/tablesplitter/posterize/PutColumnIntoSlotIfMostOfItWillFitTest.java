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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PutColumnIntoSlotIfMostOfItWillFitTest {
	private final PutColumnIntoSlotIfMostOfItWillFit testee = new PutColumnIntoSlotIfMostOfItWillFit();

	private static float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	@Test
	@DisplayName("first column will always put into current slot")
	void firstColumnWillAlwaysPutIntoCurrentSlot() {
		var columnWidth = nextFloat() * 999.9f + 0.1f;

		var columnWidths = Map.of(0, columnWidth);
		var slotWidths = List.of(columnWidth * 0.49f, columnWidth);

		var result = testee.map(columnWidths, slotWidths);
		assertThat(result).containsExactly(new Range(0, 0));
	}

	@Test
	@DisplayName("second cell put into second slot if less than 50 percent will fit into current slot")
	void secondCellPutIntoSecondSlotIfLessThan50Percent() {
		var columnWidth = nextFloat() * 999.9f + 0.1f;

		var columnWidths = Map.of(0, columnWidth, 1, columnWidth);
		var slotWidths = List.of(columnWidth * 1.4f, columnWidth);

		var result = testee.map(columnWidths, slotWidths);
		assertThat(result).containsExactly(new Range(0, 0), new Range(1, 1));
	}

	@Test
	@DisplayName("10 columns into 3 slots sample")
	void tenColumnsInto3SlotsSample() {
		var columnWidth = nextFloat() * 999.9f + 0.1f;

		var columnWidths = IntStream.range(0, 10)
			.boxed()
			.collect(Collectors.toMap(it->it, it->columnWidth));

		var slotWidths = List.of(columnWidth * 2.9f, columnWidth * 2.9f, columnWidth * 2.9f, columnWidth * 2.9f);

		var result = testee.map(columnWidths, slotWidths);
		assertThat(result).containsExactly(new Range(0, 2), new Range(3, 5), new Range(6, 8), new Range(9, 9));
	}
}