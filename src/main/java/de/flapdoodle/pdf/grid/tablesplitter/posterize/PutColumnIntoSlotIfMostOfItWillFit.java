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

import de.flapdoodle.commons.checks.Preconditions;
import de.flapdoodle.pdf.types.Floats;
import de.flapdoodle.pdf.types.IntRange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PutColumnIntoSlotIfMostOfItWillFit implements ColumnWidthsSlotMapper {
	@Override
	public List<IntRange.Closed> map(Map<Integer, Float> columnWidths, List<Float> slots) {
		if (columnWidths.isEmpty()) throw new IllegalArgumentException("no columns");
		if (slots.isEmpty()) throw new IllegalArgumentException("no slots");

		var sumOfColumnWidth = Floats.sum(columnWidths.values());
		var sumOfDestinationWiths = Floats.sum(slots);
		if (!Floats.isNearBy(sumOfColumnWidth, sumOfDestinationWiths) && (sumOfColumnWidth > sumOfDestinationWiths))
			throw new IllegalArgumentException("all columns("+sumOfColumnWidth+") does not fit into "+sumOfDestinationWiths);

		var ranges = new ArrayList<IntRange.Closed>();

		int currentColumn = columnWidths.keySet().stream()
			.min(Comparator.naturalOrder())
			.orElseThrow(() -> new IllegalArgumentException("no minimum"));

		for (Float width : slots) {
			var spaceLeft = width;
			if (currentColumn < columnWidths.size()) {
				int lastColumnInThisBlock = currentColumn;
				for (int c : IntRange.until(currentColumn,  columnWidths.size())) {
					var columnWidth = Preconditions.checkNotNull(columnWidths.get(c), "no width found for column %s", c);

					if (columnWidth < spaceLeft || (columnWidth > spaceLeft && (columnWidth / 2 < spaceLeft))) {
						lastColumnInThisBlock = c;
						spaceLeft = spaceLeft - columnWidth;
						if (spaceLeft < 0f || Floats.isNearBy(spaceLeft, 0f)) {
							break;
						}
					} else {
						break;
					}
				}
				ranges.add(IntRange.to(currentColumn, lastColumnInThisBlock));
				currentColumn = lastColumnInThisBlock + 1;
			}
		}

		return List.copyOf(ranges);
	}
}
