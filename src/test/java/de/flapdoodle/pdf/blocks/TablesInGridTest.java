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
package de.flapdoodle.pdf.blocks;

import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridCellTableRegion;
import de.flapdoodle.pdf.grid.TableSplitter;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.tables.cells.HeaderStyles;
import de.flapdoodle.pdf.tables.cells.LayeredCellStyles;
import de.flapdoodle.pdf.types.Cell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.PageSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class TablesInGridTest {
	private final CellStyles DEFAULT_STYLES = LayeredCellStyles.empty();
	private final HeaderStyles DEFAULT_HEADER_STYLES = DEFAULT_STYLES.asHeaderStyles();

	@Test
	@DisplayName("render tables in grid")
	void renderTablesInGrid() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(TablesInGrid.builder()
				.gridFactory(it -> {
						var innerBox = PageBox.innerBox(it);
						return Grid.of(Margin.none(), 3, innerBox.width() / 3, 2, innerBox.height() / 2);
				})
				.tableSplitterFactory(it -> new PutOneTableIntoOneCell())
				.addTables(
						sampleTable("A", 10),
						sampleTable("B", 20),
						sampleTable("C", 3),
						sampleTable("D", 15)
				)
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "renderTablesInGrid.pdf");
	}

	@Test
	@DisplayName("render tables in shrinked grid")
	void renderTablesInShrinkedGrid() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(TablesInGrid.builder()
				.gridFactory(it -> {
					var innerBox = PageBox.innerBox(it);
					return Grid.of(Margin.none(), 3, innerBox.width() / 3, 2, innerBox.height() / 2);
				})
				.shrinkGrid(true)
				.tableSplitterFactory(it -> new PutOneTableIntoOneCell())
				.addTables(
					sampleTable("A", 10),
					sampleTable("B", 20),
					sampleTable("C", 3),
					sampleTable("D", 15)
				)
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "shrinkTablesInGrid.pdf");
	}

	private TableFromMap sampleTable(String name,int len) {
		return TableFromMap.builder()
			.header(TableColumnsFromNameList.builder()
				.addColumnNames(name)
				.styles(DEFAULT_HEADER_STYLES)
				.build())
			.cells(IntStream.range(0, len)
				.mapToObj(it -> new Cell(0, it))
				.collect(Collectors.toMap(
					it -> it,
					it -> "Line "+it.row())))
			.build();
	}

	static class PutOneTableIntoOneCell implements TableSplitter {
		@Override
		public List<GridCellTableRegion> split(Grid grid, List<Table> tables) {
				var ret = new ArrayList<GridCellTableRegion>();

				var current = new Cell(0, 0);

			for (Table it : tables) {
				ret.add(new GridCellTableRegion(
					current,
					it,
					it.maxRegion(),
					Optional.empty(),
					Optional.of(grid.get(current).height() / 2)
				));
				current = grid.nextCell(current).orElseThrow(() -> new IllegalArgumentException("tables does not fit into grid"));
			}

			return List.copyOf(ret);
		}
	}
}