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
import de.flapdoodle.pdf.types.FloatArray;
import de.flapdoodle.pdf.types.Floats;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;

record ColumnWidths(
	int start,
	FloatArray widths
) {

	public int lastColumn() {
		return start + widths.length() - 1;
	}

	public OptionalInt lastFittingColumn(int column, float width) {
		int current = Preconditions.checkElementIndex((column - start), widths.length());
		OptionalInt result = OptionalInt.empty();

		float left = width;
		while (current < widths.length() && (left >= widths.get(current) || Floats.isNearBy(left, widths.get(current)))) {
			left -= widths.get(current);
			result = OptionalInt.of(current);
			current++;
		}

		return result;
	}

	static ColumnWidths ofMap(Map<Integer, Float> columnWidths) {
		List<Map.Entry<Integer, Float>> columnWeightList = columnWidths.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.toList();

		int firstColumn = columnWeightList.get(0).getKey();
		int lastColumn = columnWeightList.get(columnWeightList.size() - 1).getKey();

		Preconditions.checkArgument(((lastColumn - firstColumn) + 1) == columnWeightList.size(), "there are missing columns: %s", columnWidths);

		return new ColumnWidths(
			firstColumn,
			FloatArray.from(columnWeightList.stream().map(Map.Entry::getValue).collect(Collectors.toList()))
		);
	}
}
