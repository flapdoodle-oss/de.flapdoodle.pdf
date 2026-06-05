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
package de.flapdoodle.pdf.grid;

import de.flapdoodle.pdf.extensions.ListExtensions;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Dimension;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Position;

import java.util.*;

public final class GridPartitioner {
	private GridPartitioner() {
	}

	public record CellSet(
		Position position,
		boolean onNewPage,
		Set<Cell> cells
	) {
	}

	public static List<CellSet> partition(Grid grid, float firstTopMargin, Dimension pageSize) {
		var horizontalPageBreaks = partition(grid.widths(), pageSize.width());
		var verticalPageBreaks = partition(grid.heights(), pageSize.height(), firstTopMargin);

		var cellSets = new ArrayList<CellSet>();

		ListExtensions.forEachIndexed(verticalPageBreaks, (ridx, rows) -> {
			ListExtensions.forEachIndexed(horizontalPageBreaks, (cidx, columns) -> {
				if (!rows.isEmpty() && !columns.isEmpty()) {
					var topLeftCell = grid.positionOf(new Cell(columns.start(), rows.start()));
					cellSets.add(new CellSet(
						new Position(topLeftCell.x(), topLeftCell.y() - ((ridx == 0 && cidx == 0) ? firstTopMargin : 0f)),
						(ridx != 0) || (cidx != 0),
						getAll(columns, rows)
					));
				}
			});
		});

		return List.copyOf(cellSets);
	}

	private static Set<Cell> getAll(IntRange.Closed columns, IntRange.Closed rows) {
		var set = new LinkedHashSet<Cell>();
		rows.forEach(r ->
			columns.forEach(c ->
				set.add(new Cell(c, r))));
		return Collections.unmodifiableSet(set);
	}

	protected static List<IntRange.Closed> partition(List<Float> list, float max) {
		return partition(list, max, 0f);
	}

	protected static List<IntRange.Closed> partition(List<Float> list, float max, float initialOffset) {
		if (list.isEmpty()) throw new IllegalArgumentException("list is empty");

		var ret = new ArrayList<IntRange.Closed>();

		var sum = initialOffset;
		var lastStart = 0;
		var lastFit = -1;

		for (var index = 0; index < list.size(); index++) {
			var value = list.get(index);
			if (value > max) throw new IllegalArgumentException(value + "+ at " + index + " > " + max);
			if (sum + value <= max) {
				lastFit = index;
				sum = sum + value;
			} else {
				if (lastFit == -1) {
					// first value did not fit, because of initialOffset
					ret.add(IntRange.Closed.EMPTY);
				} else {
					ret.add(IntRange.to(lastStart, lastFit));
				}
				lastStart = index;
				lastFit = lastStart;
				sum = value;
			}
		}

		ret.add(IntRange.to(lastStart, lastFit));
		return List.copyOf(ret);
	}

}
