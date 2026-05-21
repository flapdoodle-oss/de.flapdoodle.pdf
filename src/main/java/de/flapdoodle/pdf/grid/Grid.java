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

import com.google.common.base.Preconditions;
import de.flapdoodle.pdf.extensions.ListExtensions;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.types.Box;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Dimension;
import de.flapdoodle.pdf.types.Position;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Grid {
	private final Margin margin;
	private final List<Float> widths;
	private final List<Float> heights;

	public Grid(Margin margin, List<Float> widths, List<Float> heights) {
		Preconditions.checkArgument(!widths.isEmpty(), "widths are empty");
		Preconditions.checkArgument(!heights.isEmpty(), "heights are empty");
		this.margin = margin;
		this.widths = List.copyOf(widths);
		this.heights = List.copyOf(heights);
	}

	public Grid(List<Float> widths, List<Float> heights) {
		this(Margin.none(), widths, heights);
	}

	public Grid(float with, float heigh) {
		this(Margin.none(), List.of(with), List.of(heigh));
	}

	public Grid(Margin margin, int columns, float width, int rows, float heigh) {
		this(margin,
			Stream.generate(() -> width).limit(columns).toList(),
			Stream.generate(() -> heigh).limit(rows).toList());
	}

	public Grid(int columns, float width, int rows, float heigh) {
		this(Margin.none(), columns, width, rows, heigh);
	}

	static float sum(List<Float> list) {
		var sum = 0.0f;
		for (float element : list) {
			sum += element;
		}
		return sum;
	}
	private static <T> List<T> append(List<T> src, T value) {
		return Stream.concat(src.stream(), Stream.of(value)).toList();
	}

	public Grid addColumn(float width) {
//		return copy(widths = widths + width)
		return new Grid(margin, append(widths, width), heights);
	}
	public Grid addRow(float height) {
		return new Grid(margin, widths, append(heights, height));
	}

	public int columns() {
		return widths.size();
	}

	public int rows() {
		return heights.size();
	}

	public Dimension get(Cell cell) {
		return new Dimension(widths.get(cell.column()), heights.get(cell.row()));
	}

	public Dimension innerDimension(Cell cell) {
		return new Dimension(
			widths.get(cell.column()) - margin.left() - margin.right(),
			heights.get(cell.row()) - margin.top() - margin.bottom()
		);
	}

	public List<Float> innerWidths() {
		return widths.stream().map(it -> it - margin.left() - margin.right()).toList();
	}

	public List<Float> widths() {
		return widths;
	}

	public List<Float> heights() {
		return heights;
	}
	public Margin margin() {
		return margin;
	}

	public Optional<Cell> nextCell(Cell current) {
		if (current.column() + 1 < columns() && current.row() < rows())
			return Optional.of(new Cell(current.column() + 1, current.row()));
		if (current.row() + 1 < rows())
			return Optional.of(new Cell(0, current.row() + 1));
		return Optional.empty();
	}

	public Optional<Cell> cellInNextRow(Cell current) {
		if (current.row() + 1 < rows())
			return Optional.of(new Cell(current.column(), current.row() + 1));
		return Optional.empty();
	}

	public Box asBox(Cell cell) {
		return new Box(positionOf(cell), get(cell));
	}

	public Position positionOf(Cell cell) {
		return new Position(
			sum(widths.subList(0, cell.column())),
			sum(heights.subList(0, cell.row()))
		);
	}

	public Box innerBox(Cell cell) {
		var topLeft = new Position(
			sum(widths.subList(0, cell.column())) + margin.left(),
			sum(heights.subList(0, cell.row())) + margin.top()
		);
		var dim = innerDimension(cell);

		return new Box(topLeft, dim);
	}

	public Grid trim(GridContent<?> gridContent) {
		var maxColumn = 0;
		var maxRow = 0;

		for (int c = 0; c < columns(); c++) {
			for (int r = 0; r < rows(); r++) {
				if (gridContent.get(new Cell(c, r)).isPresent()) {
					maxColumn = Math.max(maxColumn, c);
					maxRow = Math.max(maxRow, r);
				}
			}
		}

		return new Grid(
			margin,
			widths.subList(0, maxColumn + 1),
			heights.subList(0, maxRow + 1)
		);
	}

	public Grid shrinkHeight(Map<Cell, Float> cellHeights) {
		var rowHeights = cellHeights.entrySet().stream()
			.filter(it -> it.getValue() != null)
			.collect(Collectors.groupingBy(
				it -> it.getKey().row(),
				Collectors.mapping(Map.Entry::getValue, Collectors.reducing(0f, Math::max))
			));

		return new Grid(margin, widths, ListExtensions.mapIndexed(heights, (row, height) -> {
			Float h = rowHeights.get(row);
			return h != null ? Float.min(height, h) : height;
		}));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Grid) obj;
		return Objects.equals(this.margin, that.margin) &&
			Objects.equals(this.widths, that.widths) &&
			Objects.equals(this.heights, that.heights);
	}
	@Override
	public int hashCode() {
		return Objects.hash(margin, widths, heights);
	}
	@Override
	public String toString() {
		return "Grid[" +
			"margin=" + margin + ", " +
			"widths=" + widths + ", " +
			"heights=" + heights + ']';
	}

}
