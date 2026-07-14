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
package de.flapdoodle.pdf.render.table;

import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.*;
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfContentByte;

import java.awt.*;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class FindSmallestTableWidthFromRenderingTest {
	private final CellStyles DEFAULT_STYLES = LayeredCellStyles.empty();
	private final HeaderStyles DEFAULT_HEADER_STYLES = DEFAULT_STYLES.asHeaderStyles();

	@Test
	void minWidthOfSmallTable() {
		var table = TableFromMap.builder()
			.header(defaultHeader("Column"))
			.cells(Map.of(new Cell(0, 0), "Value"))
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(expectMinWidth(table, 45.343f))
			.build())
			.expectRendering()
			.matchesResource(getClass(),"minimalTable-rendering--small.pdf");
	}

	@Test
	void minWidthOfWideTable() {
		int columns = 15;

		String[] columnNames = IntRange.until(0, columns)
			.stream().boxed()
			.map(it -> "C " + it)
			.toArray(String[]::new);

		Map<Cell, String> cells = IntRange.until(0, columns)
			.stream().boxed()
			.map(it -> new Cell(it, 0))
			.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row()));

		var table = TableFromMap.builder()
			.header(defaultHeader(columnNames))
			.cells(cells)
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(expectMinWidth(table, 440.160f))
			.build())
			.expectRendering()
			.matchesResource(getClass(),"minimalTable-rendering--wide.pdf");
	}

	@Test
	void minWidthOfWideTableStartingSmall() {
		int columns = 15;

		String[] columnNames = IntRange.until(0, columns)
			.stream().boxed()
			.map(it -> "C " + it)
			.toArray(String[]::new);

		Map<Cell, String> cells = IntRange.until(0, columns)
			.stream().boxed()
			.map(it -> new Cell(it, 0))
			.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row()));

		var table = TableFromMap.builder()
			.header(defaultHeader(columnNames))
			.cells(cells)
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(expectMinWidth(table, 440.16138f, PageSize.A4.getWidth()/2.0f))
			.build())
			.expectRendering()
			.matchesResource(getClass(),"minimalTable-rendering--wide-small-start.pdf");
	}


	@Test
	void minWidthOfSmallLongTable() {
		int columns = 5;
		int rows = 100;

		String[] columnNames = IntRange.until(0, columns)
			.stream().boxed()
			.map(it -> "C " + it)
			.toArray(String[]::new);

		Map<Cell, String> cells = IntRange.until(0, rows)
			.stream().boxed()
			.map(it -> new Cell( 0, it))
			.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row()));

		var table = TableFromMap.builder()
			.header(defaultHeader(columnNames))
			.cells(cells)
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(expectMinWidth(table, 136.763f))
			.build())
			.expectRendering()
			.matchesResource(getClass(),"minimalTable-rendering--long.pdf");
	}

	@Test
	void minWidthBug() {
		TableFromMap table = TableFromMap.builder()
			.header(TableColumnsFromNameList.builder()
				.addColumnNames("A", "B", "C", "D","E","F","G","H","I")
				.styles(HeaderStyles.asHeaderStyles(LayeredCellStyles.empty()
					.withDefault(CellStyle.empty()
						// TODO wenn der headerRows breiter ist als die Spalte,
						//  dann wird der Header zusammen gestutzt?
						.withBackgroundColor(Color.LIGHT_GRAY)
						.withPadding(BorderProperty.of(5.0f))
						.withHorizontalAlignment(HorizontalAlignment.CENTER))))
				.build())
			.styles(LayeredCellStyles.empty()
				.withDefault(CellStyle.empty()
					.withPadding(BorderProperty.of(20.f, 10.0f, 20.f, 10.f))
					.withHorizontalAlignment(HorizontalAlignment.CENTER)))
			.columnWeights(ColumnWeights.EMPTY)
			.cells(new Region(IntRange.to(0, 9 - 1), IntRange.to(0, 30 - 1))
				.map(Cell::new)
				.stream()
				.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row())))
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(expectMinWidth(table, 570.16f))
			.build())
			.expectRendering()
			.matchesResource(getClass(),"minimalTable-rendering--bug.pdf");
	}

	private static Block expectMinWidth(Table table, float expectedMinWidth) {
		return expectMinWidth(table, expectedMinWidth, PageSize.A4.getWidth()*10.0f);
	}

	private static Block expectMinWidth(Table table, float expectedMinWidth, float startingWidth) {
		return new Block() {
			@Override
			public void render(Document document, Supplier<PdfContentByte> directContent) {
				var testee = new FindSmallestTableWidthFromRendering();
				ColumnTableRenderer renderer = new ColumnTableRenderer(directContent.get(), new DefaultRegionColumnRenderer());

				float minWidth = testee.of(renderer, table, startingWidth).width();
				Assertions.assertThat(minWidth)
					.isCloseTo(expectedMinWidth, Percentage.withPercentage(1.0d));

				renderer.render(table, table.maxRegion(), PageBox.innerBox(document).withWidth(minWidth)).go();
			}
		};
	}

	private TableColumnsFromNameList defaultHeader(String... names) {
		return TableColumnsFromNameList.builder()
			.addColumnNames(names)
			.styles(DEFAULT_HEADER_STYLES)
			.build();
	}

}