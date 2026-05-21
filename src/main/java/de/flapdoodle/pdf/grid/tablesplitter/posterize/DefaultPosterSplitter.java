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
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.Tables;
import de.flapdoodle.pdf.types.Floats;
import de.flapdoodle.pdf.types.Range;

import java.util.List;
import java.util.Optional;

public record DefaultPosterSplitter(
	ColumnWidthsSlotMapper columnWidthsSlotMapper
) implements PosterSplitter {
	public DefaultPosterSplitter() {
		this(new PutColumnIntoSlotIfMostOfItWillFit());
	}

	@Override
	public Split split(TableRenderer tableRenderer, Grid grid, Table table) {
		return new Split(table, columnRanges(tableRenderer, grid, table));
	}

	private List<Part> columnRanges(
		TableRenderer tableRenderer,
		Grid grid,
		Table table
	) {
		var columnWeights = table.columnWeights();
		var gridWidth = Floats.sum(grid.innerWidths());
		var tableWidth = tableRenderer.minimalWidthOf(table, gridWidth);

		var columnWidths = Tables.columnWidths(new Range(0, table.columns() - 1), tableWidth, columnWeights);

		var mapped = columnWidthsSlotMapper.map(columnWidths, grid.innerWidths());
		var lastGridColumnIndex = mapped.size() - 1;

		return ListExtensions.mapIndexed(mapped, (index, range) -> {
			var width = (index == lastGridColumnIndex)
				? Floats.sum(range.mapToFloat(columnWidths::get))
				:	null;
			return new Part(index, range, Optional.ofNullable(width));
		});
	}
}
