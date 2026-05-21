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
package de.flapdoodle.pdf.grid.tablesplitter;

import com.google.common.base.Preconditions;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.TableSplitter;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SplitTableIntoRegionsWithSameHeight(
	TableRenderer tableRenderer
) implements TableSplitter {
	@Override
	public List<GridCellContent> split(Grid grid, List<Table> tables) {
		var result = new ArrayList<GridCellContent>();

		var columnPerRow = grid.columns();
		Cell currentCell = new Cell(0, 0);

		for (Table table : tables) {
			Region region = table.maxRegion();

			var rowsPerCell = rowsPerCell(columnPerRow, table.rows());

			while (region != null) {
				Cell cell = currentCell;
				if (cell == null) {
					 throw new IllegalArgumentException("no grid cells left");
				}

				var maxRow = Math.min(region.rows().start() + rowsPerCell - 1, region.rows().end());

				var pageBox = grid.get(cell).pageBoxAt(PagePosition.ZERO);
				var renderStatus = tableRenderer.render(table, region.untilRow(maxRow), pageBox);

				Preconditions.checkArgument(
					renderStatus.lastVisibleRow() >= maxRow,
					"could not render all rows into %s, height needed: %s",
					pageBox, renderStatus.tableHeight()
				);

				result.add(new TableSplitter.GridCellContent(
					cell,
					table,
					region.untilRow(maxRow),
					Optional.empty(),
					Optional.of(renderStatus.tableHeight())
				));

				region = region.fromRow(region.rows().start() + rowsPerCell).orElse(null);
				currentCell = grid.nextCell(cell).orElse(null);
			}
		}
		return List.copyOf(result);
	}

	static int rowsPerCell(int gridColumns, int rows) {
		return (rows + gridColumns - 1) / gridColumns;
	}
}
