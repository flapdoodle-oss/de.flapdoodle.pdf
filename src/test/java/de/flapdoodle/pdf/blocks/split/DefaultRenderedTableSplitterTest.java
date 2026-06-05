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
package de.flapdoodle.pdf.blocks.split;

import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.render.table.ImmutableTableAttributes;
import de.flapdoodle.pdf.render.table.RenderedTableDimension;
import de.flapdoodle.pdf.render.table.TableAttributes;
import de.flapdoodle.pdf.render.table.TableWidth;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.FloatArray;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRenderedTableSplitterTest {

	@Test
	void split3ColumnsInto2BoxesWithRepeatingFirstColumn() {
		List<IntRange.Closed> result = DefaultRenderedTableSplitter.split(
			FloatArray.from(5f, 10f, 10f),
			0,
			1,
			FloatArray.from(15f, 15f));

		assertThat(result)
			.hasSize(2)
			.containsExactly(
				IntRange.to(1, 1),
				IntRange.to(2, 2)
			);
	}

	@Test
	void split4ColumnsInto3Parts() {
		List<IntRange.Closed> result = DefaultRenderedTableSplitter.split(
			FloatArray.from(20.f, 20.f, 40.f, 10.f),
			0,
			0,
			FloatArray.from(45.f, 45.f, 45.f));

		assertThat(result)
			.hasSize(3)
			.containsExactly(
				IntRange.to(0, 1),
				IntRange.to(2, 2),
				IntRange.to(3, 3)
			);
	}

	@Test
	void splitTableIntoGrid() {
		DefaultRenderedTableSplitter testee = new DefaultRenderedTableSplitter();
		Grid grid = Grid.builder()
			.margin(Margin.none())
			.addWidths(50f, 50f)
			.addHeights(50f, 50f)
			.build();

		RenderedTableDimension dimensions = new RenderedTableDimension(-1f, -2f,
			// 7 colums
			FloatArray.from(5f, 10f, 10f, 10f, 10f, 10f, 10f),
			// header + 4 rows
			FloatArray.from(10f, 20f, 20f, 20f, 20f)
		);

		Split result = testee.split(grid, dimensions, ScaleToColumnWidth.NEVER, true, 1, true);

		ImmutableTableAttributes defaultAttributes = TableAttributes.defaults()
			.withHorizontalAlignment(HorizontalAlignment.LEFT);

		assertThat(result.keyColumns()).isEqualTo(1);
		assertThat(result.parts())
			.hasSize(4)
			.containsExactly(
				new Split.Part(
					new Cell(0, 0),
					new Region(IntRange.to(1, 4), IntRange.to(0, 1)),
					defaultAttributes.withTableWidth(TableWidth.absolute(45f))
						.withRowHeights(Map.of(0, 10f, 1, 20f, 2, 20f)),
					true
				),
				new Split.Part(
					new Cell(1, 0),
					new Region(IntRange.to(5, 6), IntRange.to(0, 1)),
					defaultAttributes.withTableWidth(TableWidth.absolute(25f))
						.withRowHeights(Map.of(0, 10f, 1, 20f, 2, 20f)),
					true
				),
				new Split.Part(
					new Cell(0, 1),
					new Region(IntRange.to(1, 4), IntRange.to(2, 3)),
					defaultAttributes.withTableWidth(TableWidth.absolute(45f))
						.withRowHeights(Map.of(0, 10f, 1, 20f, 2, 20f)),
					true
				),
				new Split.Part(
					new Cell(1, 1),
					new Region(IntRange.to(5, 6), IntRange.to(2, 3)),
					defaultAttributes.withTableWidth(TableWidth.absolute(25f))
						.withRowHeights(Map.of(0, 10f, 1, 20f, 2, 20f)),
					true
				)
			);
	}

	@Test
	void splitTableIntoGridWithoutRepeatingHeader() {
		DefaultRenderedTableSplitter testee = new DefaultRenderedTableSplitter();
		Grid grid = Grid.builder()
			.margin(Margin.none())
			.addWidths(50f, 50f)
			.addHeights(50f, 50f)
			.build();

		RenderedTableDimension dimensions = new RenderedTableDimension(-1f, -2f,
			// 7 colums
			FloatArray.from(5f, 10f, 10f, 10f, 10f, 10f, 10f),
			// header + 4 rows
			FloatArray.from(10f, 20f, 20f, 20f, 20f)
		);

		Split result = testee.split(grid, dimensions, ScaleToColumnWidth.NEVER, true, 1, false);

		ImmutableTableAttributes defaultAttributes = TableAttributes.defaults()
			.withHorizontalAlignment(HorizontalAlignment.LEFT);
		
		assertThat(result.keyColumns()).isEqualTo(1);
		assertThat(result.parts())
			.hasSize(4)
			.containsExactly(
				new Split.Part(
					new Cell(0, 0),
					new Region(IntRange.to(1, 4), IntRange.to(0, 1)),
					defaultAttributes.withTableWidth(TableWidth.absolute(45f))
						.withRowHeights(Map.of(0, 10f, 1, 20f, 2, 20f)),
					true
				),
				new Split.Part(
					new Cell(1, 0),
					new Region(IntRange.to(5, 6), IntRange.to(0, 1)),
					defaultAttributes.withTableWidth(TableWidth.absolute(25f))
						.withRowHeights(Map.of(0, 10f, 1, 20f, 2, 20f)),
					true
				),
				new Split.Part(
					new Cell(0, 1),
					new Region(IntRange.to(1, 4), IntRange.to(2, 3)),
					defaultAttributes.withTableWidth(TableWidth.absolute(45f))
						.withRowHeights(Map.of(0, 20f, 1, 20f)),
					false
				),
				new Split.Part(
					new Cell(1, 1),
					new Region(IntRange.to(5, 6), IntRange.to(2, 3)),
					defaultAttributes.withTableWidth(TableWidth.absolute(25f))
						.withRowHeights(Map.of(0, 20f, 1, 20f)),
					false
				)
			);
	}
}