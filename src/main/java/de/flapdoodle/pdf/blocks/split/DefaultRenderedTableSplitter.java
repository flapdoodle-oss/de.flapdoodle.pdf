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
package de.flapdoodle.pdf.blocks.split;

import de.flapdoodle.commons.checks.Preconditions;
import de.flapdoodle.pdf.checks.VisibleForTesting;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.render.table.ImmutableTableAttributes;
import de.flapdoodle.pdf.render.table.RenderedTableDimension;
import de.flapdoodle.pdf.render.table.TableAttributes;
import de.flapdoodle.pdf.render.table.TableWidth;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.types.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultRenderedTableSplitter implements RenderedTableSplitter {
	@Override
	public Split split(Grid grid, RenderedTableDimension dimensions, ScaleToColumnWidth scaleToColumnWidth, boolean hasHeader, int keyColumns,
		boolean repeatHeader) {
		Preconditions.checkArgument(!(repeatHeader && !hasHeader), "can not repeat a header if not exist");
		FloatArray gridWidths = FloatArray.from(grid.widths());
		FloatArray gridHeights = FloatArray.from(grid.heights());

		List<IntRange.Closed> rowParts = split(dimensions.rowHeights(), hasHeader ? 1 : 0, repeatHeader ? 1 : 0, gridHeights);
		List<IntRange.Closed> columnParts = split(dimensions.columnWidths(), 0, keyColumns, gridWidths);

		List<Split.Part> result = new ArrayList<>();

		ImmutableTableAttributes defaultAttributes = TableAttributes.defaults()
			.withHorizontalAlignment(HorizontalAlignment.LEFT);

		Map<Integer, ImmutableTableAttributes> gridColumnTableAttributes = new LinkedHashMap<>();

		for (int gridColumn = 0; gridColumn < columnParts.size(); gridColumn++) {
			IntRange.Closed column = columnParts.get(gridColumn);
			float width = dimensions.columnWidths().part(0, keyColumns).sum() +
				dimensions.columnWidths().part(column.start(), column.end() + 1).sum();

			ImmutableTableAttributes columnAttributes = switch (scaleToColumnWidth) {
				case EVER -> defaultAttributes.withTableWidth(TableWidth.relative(100f));
				case NEVER -> defaultAttributes.withTableWidth(TableWidth.absolute(width));
				case NOT_LAST_COLUMNS -> ((gridColumn + 1) < columnParts.size())
					? defaultAttributes.withTableWidth(TableWidth.relative(100.f))
					: defaultAttributes.withTableWidth(TableWidth.absolute(width));
			};
			gridColumnTableAttributes.put(gridColumn, columnAttributes);
		}

		for (int gridRow = 0; gridRow < rowParts.size(); gridRow++) {
			IntRange.Closed row = rowParts.get(gridRow);
			for (int gridColumn = 0; gridColumn < columnParts.size(); gridColumn++) {
				IntRange.Closed column = columnParts.get(gridColumn);

				ImmutableTableAttributes attributes = gridColumnTableAttributes.get(gridColumn);

				FloatArray headerRowHeight = hasHeader && gridRow == 0 || repeatHeader
					? dimensions.rowHeights().part(0, 1)
					: FloatArray.from();

				FloatArray rowHeights = hasHeader
					? headerRowHeight.append(dimensions.rowHeights().part(row.start() + 1, row.end() + 2))
					: headerRowHeight.append(dimensions.rowHeights().part(row.start(), row.end() + 1));

				Map<Integer, Float> map = StreamExtensions.mapIndexed(rowHeights)
					.collect(Collectors.toMap(
						StreamExtensions.Indexed::index,
						StreamExtensions.Indexed::value,
						(a, b) -> {
							throw new IllegalArgumentException("key collision: " + a + " ! " + b);
						},
						LinkedHashMap::new
					));

				result.add(new Split.Part(
					new Cell(gridColumn, gridRow),
					new Region(column, row),
					attributes.withRowHeights(map),
					repeatHeader || (gridRow == 0 && hasHeader)
				));
			}
		}

		return new Split(keyColumns, result);
	}

	/**
	 * repeatFirstN: X
	 * parts: X[  ][   ][     ]   X[ ][    ][  ]
	 * boxes: [                  ][              ]
	 */
	@VisibleForTesting
	static List<IntRange.Closed> split(FloatArray parts, int rangeOffset, int repeatFirstN, FloatArray boxes) {
		Preconditions.checkArgument(rangeOffset == repeatFirstN || repeatFirstN == 0 || rangeOffset == 0, "rangeOffset does not match repeatFirstN");
		Preconditions.checkArgument(parts.length() > 0, "parts is empty");
		Preconditions.checkArgument(repeatFirstN >= 0, "repeatingFirstN is negative");
		Preconditions.checkArgument(boxes.length() > 0, "boxes is empty");

		float repeatingWidth = repeatFirstN > 0
			? parts.part(0, repeatFirstN).sum()
			: 0f;

		Preconditions.checkArgument(
			boxes.allMatch(it -> it > repeatingWidth),
			"repeating part (%s) does not fit into all boxes (%s)", repeatingWidth, boxes
		);

		ArrayList<IntRange.Closed> result = new ArrayList<>();

		int box = 0;
		int current = repeatFirstN;
		while (current < parts.length() && box < boxes.length()) {
			float boxSize = boxes.get(box) - repeatingWidth;
			int last = parts.lastIndexOf(current, sumIsSmallerOrEqual(boxSize));
			Preconditions.checkArgument(last >= 0, "could not fit parts %s into box %s", parts.part(current), boxSize);
			result.add(IntRange.Closed.to(current - (current == 0 ? 0 : rangeOffset), last - rangeOffset));
			current = last + 1;
			box++;
		}

		Preconditions.checkState(current == parts.length(), "could not split %s into %s", parts, boxes);

		return result;
	}

	private static FloatPredicate sumIsSmallerOrEqual(float max) {
		return new FloatPredicate() {
			float sum = 0f;
			@Override
			public boolean test(float value) {
				sum += value;
				return sum < max || Floats.isNearBy(sum, max);
			}
		};
	}

}
