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

import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class ItemsInGridTest {
	@Test
	@DisplayName("render table with title")
	void renderTableWithTitle() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(
				ItemsInGrid.<TableWithTitle>builder()
					.gridFactory(it -> {
						var innerBox = PageBox.innerBox(it);
						return Grid.of(2, innerBox.width() / 2, 3, innerBox.height() / 3);
					})
					.itemRenderer(new TableWithTitleRenderer())
					.items(List.of(
						tableWithTitle("First", 3, 7),
						tableWithTitle("Second", 2, 8),
						tableWithTitle("Third", 4, 6),
						tableWithTitle("Fourth", 2, 9)
					))
					.build()
			)).build())
			.expectRendering()
			.matchesResource(getClass(), "itemsInGrid.pdf");
	}

	@Test
	@DisplayName("render table with title and shrink grid")
	void renderTableWithTitleAndShrinkGrid() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(
				ItemsInGrid.<TableWithTitle>builder()
					.gridFactory(it -> {
						var innerBox = PageBox.innerBox(it);
						return Grid.of(2, innerBox.width() / 2, 3, innerBox.height() / 3);
					})
					.shrinkGrid(true)
					.itemRenderer(new TableWithTitleRenderer())
					.items(List.of(
						tableWithTitle("First", 3, 7),
						tableWithTitle("Second", 2, 8),
						tableWithTitle("Third", 4, 6),
						tableWithTitle("Fourth", 2, 9)
					))
					.build()
			)).build())
			.expectRendering()
			.matchesResource(getClass(), "itemsInGridShrinkGrid.pdf");
	}

	private TableWithTitle tableWithTitle(String title, int columns, int rows) {
		return new TableWithTitle(title, TableFromMap.builder()
			.cells(new Region(IntRange.until(0, columns), IntRange.until(0, rows))
				.map(Cell::new)
				.stream()
				.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row())))
			.build());
	}

}