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
package de.flapdoodle.pdf.grid.layout;

import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridLayouter;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Position;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractCellsLayouter implements GridLayouter {
	@Override
	public List<CellLayout> layout(
		Grid grid,
		Position topLeft,
		Set<Cell> cells,
		PageBox container
	) {
		Function<Cell, Float> leftOffset = leftOffset(grid, cells, container);

		return cells.stream().map(cell -> {
			var cellBox = grid.asBox(cell)
			.translate(-topLeft.x() + leftOffset.apply(cell), -topLeft.y())
			.asPageBox(container);
			var renderBox = grid.innerBox(cell)
				.translate(-topLeft.x() + leftOffset.apply(cell), -topLeft.y())
				.asPageBox(container);
			return new GridLayouter.CellLayout(
				cell,
				cellBox,
				renderBox
			);
		}).toList();
	}

	abstract Function<Cell, Float> leftOffset(Grid grid, Set<Cell> cells, PageBox container);
}
