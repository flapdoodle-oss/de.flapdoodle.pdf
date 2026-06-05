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

import de.flapdoodle.pdf.extensions.ListExtensions;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.render.table.FindSmallestTableWidth;
import de.flapdoodle.pdf.render.table.MinimalTableWidth;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.Tables;
import de.flapdoodle.pdf.tables.virtual.GroupedTables;
import de.flapdoodle.pdf.tables.virtual.TableFromRegion;
import de.flapdoodle.pdf.types.FloatArray;
import de.flapdoodle.pdf.types.Floats;
import de.flapdoodle.pdf.types.IntRange;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record PosterSplitWithRepeatingFirstColumn(
	ColumnWidthsSlotMapper columnWidthsSlotMapper,
	MinimalTableWidth minimalTableWidth
) implements PosterSplitter {
	public PosterSplitWithRepeatingFirstColumn() {
		this(new PutColumnIntoSlotIfMostOfItWillFit(), new FindSmallestTableWidth());
	}
	@Override
	public Split split(TableRenderer tableRenderer, Grid grid, Table table) {
		var firstColumnTable = new TableFromRegion(table, table.maxRegion().firstColumn());
		var tableWithoutFirstColumn = new TableFromRegion(table, table.maxRegion().fromColumn(1)
				.orElseThrow(() -> new IllegalArgumentException("table should have more than one column")));

		var smallestGridCell = grid.innerWidths().stream()
			.min(Comparator.naturalOrder())
			.orElseThrow(() -> new IllegalArgumentException("no min?"));
		var firstColumnWidth = minimalTableWidth().of(tableRenderer, firstColumnTable, smallestGridCell).width();

		var gridWithoutFirstColumn = grid.innerWidths().stream().map(it -> it - firstColumnWidth).toList();
		var tableWidthWithoutFirstColumn = minimalTableWidth().of(tableRenderer, tableWithoutFirstColumn, Floats.sum(gridWithoutFirstColumn)).width();

		var columnWidths = Tables.columnWidths(
			IntRange.until(0, tableWithoutFirstColumn.columns()),
			tableWidthWithoutFirstColumn,
			tableWithoutFirstColumn.columnWeights()
		);

		var tableColumnsInGridColumns = columnWidthsSlotMapper.map(columnWidths, gridWithoutFirstColumn);

		var lastGridColumnIndex = tableColumnsInGridColumns.size() - 1;

		var columnRanges = ListExtensions.mapIndexed(tableColumnsInGridColumns, (index, range) -> {
			var width = (index == lastGridColumnIndex)
				? Optional.of(Floats.sum(range.mapToFloat(it -> Objects.requireNonNull(columnWidths.get(it)))))
				: Optional.<Float>empty();

			return new PosterSplitter.Part(index, range, width);
		});

		var tableWithRepeatedFirstColumn = new GroupedTables(columnRanges.stream().flatMap(it ->
			Stream.of(
				OverrideColumnWeights.using(firstColumnTable, List.of(firstColumnWidth)),
				OverrideColumnWeights.using(
					TableFromRegion.columns(tableWithoutFirstColumn, it.range()),
					 FloatArray.from(it.range().mapToFloat(columnWidths::get))
				)))
			.toList());


		var addFirstColumnToColumnRange = ListExtensions.mapIndexed(columnRanges, (index, it) ->
			new Part(
				it.column(),
				IntRange.to(it.range().start() + index, it.range().end() + index + 1),
				it.width().map(w -> w + firstColumnWidth)
			));

		return new Split(tableWithRepeatedFirstColumn, addFirstColumnToColumnRange);
	}
}
