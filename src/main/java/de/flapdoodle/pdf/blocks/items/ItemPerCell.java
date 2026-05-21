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
package de.flapdoodle.pdf.blocks.items;

import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.types.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public record ItemPerCell<C, T> (
	ItemRenderer<C, T> itemRenderer
) implements ItemInGridPlacement<C, T> {

	@Override
	public List<GridCellContent<T>> placeInGrid(
		Grid grid,
		Function<PageBox, C> columnFactory,
		Function<C, String> renderError,
		List<T> items
	) {
		var ret = new ArrayList<GridCellContent<T>>();

		final AtomicReference<Cell> current = new AtomicReference<>(new Cell(0, 0));

		items.forEach(it -> {
			var cell = current.get();
			if (cell==null) throw new IllegalArgumentException("items does not fit into grid");

			var column = columnFactory.apply(grid.get(cell).pageBoxAt(PagePosition.ZERO));
			var height = itemRenderer.render(column, it);
			var error = renderError.apply(column);
			if (error != null) {
				throw new IllegalArgumentException(error);
			}
			ret.add(new ItemInGridPlacement.GridCellContent<>(
				cell,
				it,
				Optional.empty(),
				height
			));
			current.set(grid.nextCell(cell).orElse(null));
		});

		return List.copyOf(ret);
	}
}
