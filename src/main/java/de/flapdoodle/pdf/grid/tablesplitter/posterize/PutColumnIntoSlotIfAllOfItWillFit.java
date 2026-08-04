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
import java.util.List;
import java.util.Map;

public class PutColumnIntoSlotIfAllOfItWillFit implements ColumnWidthsSlotMapper {
	@Override
	public List<IntRange.Closed> map(Map<Integer, Float> columnWidths, List<Float> slots) {
		if (columnWidths.isEmpty()) throw new IllegalArgumentException("no columns");
		if (slots.isEmpty()) throw new IllegalArgumentException("no slots");

		var sumOfColumnWidth = Floats.sum(columnWidths.values());
		var sumOfDestinationWiths = Floats.sum(slots);
		if (!Floats.isNearBy(sumOfColumnWidth, sumOfDestinationWiths) && (sumOfColumnWidth > sumOfDestinationWiths))
			throw new IllegalArgumentException("all columns("+sumOfColumnWidth+") does not fit into "+sumOfDestinationWiths);

		ColumnWidths widths = ColumnWidths.ofMap(columnWidths);

		List<IntRange.Closed> result = new ArrayList<>();
		int currentColumn = widths.start();

		for (Float width : slots) {
			if (currentColumn<=widths.lastColumn()) {
				var lastColumn = widths.lastFittingColumn(currentColumn, width);
				Preconditions.checkArgument(lastColumn.isPresent(),"could not fit columns %s into %s",widths,width);
				result.add(IntRange.to(currentColumn, lastColumn.getAsInt()));
				currentColumn = lastColumn.getAsInt() + 1;
			}
		}

		Preconditions.checkArgument(currentColumn>widths.lastColumn(),"columns left: %s (last: %s)", currentColumn, widths.lastColumn());

		return List.copyOf(result);
	}

}
