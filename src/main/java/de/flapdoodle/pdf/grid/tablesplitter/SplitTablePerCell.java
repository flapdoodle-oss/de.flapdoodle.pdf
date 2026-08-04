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

import de.flapdoodle.commons.checks.Preconditions;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridCellTableRegion;
import de.flapdoodle.pdf.grid.TableSplitter;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SplitTablePerCell(
	TableRenderer tableRenderer
) implements TableSplitter {
	@Override
	public List<GridCellTableRegion> split(Grid grid, List<Table> tables) {
		var ret = new ArrayList<GridCellTableRegion>();

		var current = new Cell(0, 0);

		for (Table it : tables) {
			var pageBox = grid.get(current).pageBoxAt(PagePosition.ZERO);
			var renderStatus = tableRenderer.render(it, it.maxRegion(), pageBox);

			Preconditions.checkArgument(
				renderStatus.lastVisibleRow() >= it.maxRegion().rows().end(),
				"could not render all rows into %s, height needed: %s",
				pageBox, renderStatus.tableHeight()
			);

			ret.add(new GridCellTableRegion(
				current,
				it,
				it.maxRegion(),
				Optional.empty(),
				Optional.of(renderStatus.tableHeight())
			));

			current = grid.nextCell(current)
				.orElseThrow(() -> new IllegalArgumentException("tables does not fit into grid"));
		}

		return List.copyOf(ret);
	}
}
