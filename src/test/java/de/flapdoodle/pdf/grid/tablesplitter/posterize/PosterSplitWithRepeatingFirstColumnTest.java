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

import com.google.common.math.DoubleMath;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.table.MinimalTableWidth;
import de.flapdoodle.pdf.render.table.RenderedTableDimension;
import de.flapdoodle.pdf.render.table.TableAttributes;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.FloatArray;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PosterSplitWithRepeatingFirstColumnTest {
	@Test
	@DisplayName("split poster")
	void splitPoster() {
		var gridColumnWidth = 100f;
		var gridColumns = 3;
		var tableColumns = 10;
		var tableColumnsWithoutFirstColumn = tableColumns - 1;

		var firstColumnWidth = 10f;
		var tableWithoutFirstColumnWidth = 250f;

		var minimalTableWith = new MinimalTableWidth() {
			@Override
			public RenderedTableDimension of(TableRenderer renderer, Table table, float startingWidth) {
				if (table.columns() == 1)
					return new RenderedTableDimension(firstColumnWidth, 0f, FloatArray.from(), FloatArray.from());
				if (table.columns() == tableColumnsWithoutFirstColumn)
					return new RenderedTableDimension(tableWithoutFirstColumnWidth, 0f, FloatArray.from(), FloatArray.from());
				throw new IllegalArgumentException("unexpected columns: "+table.columns());
			}
		};

		var tableRenderer = new TableRenderer() {
			@Override
			public Result render(Table table, TableAttributes attributes, Region region, PageBox pageBox) {
				throw new UnsupportedOperationException();
			}
		};

		var grid = Grid.of(Margin.none(), gridColumns, gridColumnWidth, 2, 200f);
		var table = TableFromMap.builder()
			.cells(new Region(IntRange.to(0, tableColumnsWithoutFirstColumn), IntRange.to(0, 4))
				.map(Cell::new)
				.stream().collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row())))
			.build();

		PosterSplitter.Split split = new PosterSplitWithRepeatingFirstColumn(
			new PutColumnIntoSlotIfMostOfItWillFit(),
			minimalTableWith
		)
			.split(tableRenderer, grid, table);
		var newTable = split.table();
		var parts = split.part();

		assertThat(newTable.columns()).isEqualTo(12);

		var tableColumnWith = tableWithoutFirstColumnWidth / tableColumnsWithoutFirstColumn;
		var numberOfColumnsGridCell = DoubleMath.roundToInt(((gridColumnWidth - firstColumnWidth) / tableColumnWith), RoundingMode.HALF_DOWN);
		var numberOfColumsInLastCell = tableColumnsWithoutFirstColumn - 2 * numberOfColumnsGridCell;

		var expectedLastPartWidth = firstColumnWidth + numberOfColumsInLastCell * tableColumnWith;

		assertThat(parts).hasSize(3);

		assertThat(parts.get(0).column()).isEqualTo(0);
		assertThat(parts.get(0).range()).isEqualTo(IntRange.to(0, 3));
		assertThat(parts.get(0).width()).isEmpty();

		assertThat(parts.get(1).column()).isEqualTo(1);
		assertThat(parts.get(1).range()).isEqualTo(IntRange.to(4, 7));
		assertThat(parts.get(1).width()).isEmpty();

		assertThat(parts.get(2).column()).isEqualTo(2);
		assertThat(parts.get(2).range()).isEqualTo(IntRange.to(8, 11));
		assertThat(parts.get(2).width()).contains(expectedLastPartWidth);
	}

}