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

import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridCellTableRegion;
import de.flapdoodle.pdf.grid.TableSplitter;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumns;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SplitTableOnLastRenderedRow(
	TableRenderer tableRenderer,
	boolean removeHeaderAfterSplit
) implements TableSplitter {
	public SplitTableOnLastRenderedRow(TableRenderer tableRenderer) {
		this(tableRenderer, false);
	}

	@Override
	public List<GridCellTableRegion> split(Grid grid, List<Table> tables) {
		var result = new ArrayList<GridCellTableRegion>();

		Cell currentCell = new Cell(0, 0);

		for (Table table : tables) {
			Region region = table.maxRegion();
			var isAfterSplit = false;
			while (region != null) {
				var cell = currentCell;
				if (cell != null) {
					var gridPageBox = grid.innerDimension(cell).pageBoxAt(PagePosition.ZERO);

					var tableToRender = headerCorrection(isAfterSplit, table);
					var status = tableRenderer.render(tableToRender, region, gridPageBox);

					result.add(new GridCellTableRegion(
						cell,
						tableToRender,
						region.untilRow(status.lastVisibleRow()),
						Optional.empty(),
						Optional.of(status.tableHeight())
					));

					region = region.fromRow(status.lastVisibleRow() + 1).orElse(null);
					currentCell = grid.nextCell(cell).orElse(null);
					isAfterSplit = true;
				}
			}
		}
		return List.copyOf(result);
	}

	private Table headerCorrection(boolean isAfterSplit, Table table) {
		return removeHeaderAfterSplit && isAfterSplit
			? new TableWithoutHeaderDelegate(table)
			: table;
	}

	record TableWithoutHeaderDelegate(Table delegate) implements Table {
		@Override
		public Optional<TableColumns> header() {
			return Optional.empty();
		}
		@Override
		public int columns() {
			return delegate.columns();
		}
		@Override
		public int rows() {
			return delegate.rows();
		}
		@Override
		public CellStyles styles() {
			return delegate.styles();
		}
		@Override
		public ColumnWeights columnWeights() {
			return delegate.columnWeights();
		}
		@Override
		public Optional<String> get(Cell cell) {
			return delegate.get(cell);
		}
	}
}
