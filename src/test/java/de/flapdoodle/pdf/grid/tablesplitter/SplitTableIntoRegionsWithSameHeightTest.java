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

import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.DocumentFactoryAssert;
import de.flapdoodle.pdf.blocks.TablesInGrid;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridCellDecorator;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.tables.cells.HeaderStyles;
import de.flapdoodle.pdf.tables.cells.LayeredCellStyles;
import de.flapdoodle.pdf.types.Cell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SplitTableIntoRegionsWithSameHeightTest {
	private final CellStyles DEFAULT_STYLES = LayeredCellStyles.empty();
	private final HeaderStyles DEFAULT_HEADER_STYLES = DEFAULT_STYLES.asHeaderStyles();

	@Test
	@DisplayName("rows per column")
	void rowsPerColumn() {
		assertThat(SplitTableIntoRegionsWithSameHeight.rowsPerCell(3, 9)).isEqualTo(3);
		assertThat(SplitTableIntoRegionsWithSameHeight.rowsPerCell(3, 10)).isEqualTo(4);
		assertThat(SplitTableIntoRegionsWithSameHeight.rowsPerCell(3, 11)).isEqualTo(4);
		assertThat(SplitTableIntoRegionsWithSameHeight.rowsPerCell(3, 12)).isEqualTo(4);
		assertThat(SplitTableIntoRegionsWithSameHeight.rowsPerCell(3, 13)).isEqualTo(5);
	}

	@Test
	@DisplayName("render tables in grid")
	void renderTablesInGrid() {
		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.blocks(List.of(
					TablesInGrid.builder()
						.gridFactory(it -> {
							var innerBox = PageBox.innerBox(it);
							return new Grid(Margin.none(), 3, innerBox.width() / 3, 4, innerBox.height() / 4);
						})
						.tableSplitterFactory(SplitTableIntoRegionsWithSameHeight::new)
						.tables(List.of(
							sampleTable("A", 10),
							sampleTable("B", 20),
							sampleTable("C", 3),
							sampleTable("D", 15)
						))
						.build()
				))
				.build())
			.expectRendering()
			.matchesResource(getClass(),"splitTableIntoRegionsWithSameHeight.pdf");
	}

	@Test
	@DisplayName("try render tables where height is not enough")
	void tryRenderTablesWhenHeightIsNotEnough() {
		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.blocks(List.of(
					TablesInGrid.builder()
						.gridFactory(it -> {
							var innerBox = PageBox.innerBox(it);
							return new Grid(Margin.none(), 3, innerBox.width() / 3, 4, innerBox.height() / 4);
						})
						.tableSplitterFactory(SplitTableIntoRegionsWithSameHeight::new)
						.tables(List.of(
							sampleTable("A", 31)
						))
						.cellBoxDecorator(GridCellDecorator.renderBorder(Color.LIGHT_GRAY))
						.build()
				))
				.build())
			.renderingThrows()
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("could not render all rows into PageBox");
	}

	private TableFromMap sampleTable(String name, int len) {
		return TableFromMap.builder()
			.header(TableColumnsFromNameList.builder()
				.addColumnNames(name)
				.styles(DEFAULT_HEADER_STYLES)
				.build())
			.cells(IntStream.range(0, len)
				.mapToObj(it -> new Cell(0, it))
				.collect(Collectors.toMap(
					it -> it,
					it -> "Line " + it.row())))
			.build();
	}
}