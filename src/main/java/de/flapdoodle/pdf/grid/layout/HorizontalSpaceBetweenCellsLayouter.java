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

import de.flapdoodle.pdf.checks.Preconditions;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Floats;
import de.flapdoodle.pdf.types.IntRange;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

public class HorizontalSpaceBetweenCellsLayouter extends AbstractCellsLayouter {
	@Override
	Function<Cell, Float> leftOffset(Grid grid, Set<Cell> cells, PageBox container) {
		return !cells.isEmpty()
			? internalLeftOffset(grid, cells, container)
			: it -> 0f;
	}

	protected static	Function<Cell, Float> internalLeftOffset(Grid grid, Set<Cell> cells, PageBox container) {
			float width;
			IntRange.Closed columnRange;

			var gridWidth = Floats.sum(grid.widths());
			if (gridWidth > container.width()) {
				BoundingBox boundingBox = boundingBox(grid, cells);
				width = boundingBox.mostRightPosition() - boundingBox.mostLeftPosition();
				columnRange = boundingBox.range();
			} else {
				width = gridWidth;
				columnRange = IntRange.until(0, grid.columns()).asClosed();
			}

			var spaceLeft = container.width() - width;
			Preconditions.checkArgument(spaceLeft >= 0f, "cells should fit in container: %s > %s", width, container.width());

			float spaceBefore;
			float spaceBetween;

			if (cells.size() == 1) {
				spaceBefore = spaceLeft / 2.0f;
				spaceBetween = 0f;
			} else {
				spaceBefore = 0f;
				spaceBetween = spaceLeft / (columnRange.size() - 1);
			}

			return it -> {
				var columnDiff = it.column() - columnRange.start();
				return spaceBefore + spaceBetween * columnDiff;
			};
		}

		private static BoundingBox boundingBox(Grid grid, Set<Cell> cells) {
			var leftAndRightPositions = cells.stream()
				.map(grid::asBox)
				.map(it -> new Pair<>(it.left(), it.left() + it.width()))
				.toList();

			var mostLeftPosition = leftAndRightPositions.stream()
				.map(Pair::first)
				.min(Comparator.naturalOrder())
				.orElseThrow(() -> new IllegalArgumentException("must be a min"));

			var mostRightPosition = leftAndRightPositions.stream()
				.map(it -> it.second)
				.max(Comparator.naturalOrder())
				.orElseThrow(() -> new IllegalArgumentException("must be a max"));

			var leftColumn = cells.stream()
				.map(Cell::column)
				.min(Comparator.naturalOrder())
				.orElseThrow(() -> new IllegalArgumentException("must be a min"));

			var rightColumn = cells.stream()
				.map(Cell::column)
				.max(Comparator.naturalOrder())
				.orElseThrow(() -> new IllegalArgumentException("must be a max"));

			return new BoundingBox(IntRange.to(leftColumn, rightColumn), mostLeftPosition, mostRightPosition);
		}

		record Pair<L, R>(L first, R second) {}
		record BoundingBox(IntRange.Closed range, float mostLeftPosition, float mostRightPosition) {}
}
