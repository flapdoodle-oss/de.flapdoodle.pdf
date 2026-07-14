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
import de.flapdoodle.pdf.blocks.AutosplitTableTest;
import de.flapdoodle.pdf.columns.ColumnFactory;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.*;
import de.flapdoodle.pdf.types.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfContentByte;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultRegionColumnRendererTest {
	private final CellStyles DEFAULT_STYLES = LayeredCellStyles.empty();
	private final HeaderStyles DEFAULT_HEADER_STYLES = DEFAULT_STYLES.asHeaderStyles();

	@Test
	@DisplayName("render table with one column")
	void renderTableWithOneColumn() {
		var table = TableFromMap.builder()
			.header(defaultHeader("Column"))
			.cells(Map.of(new Cell(0, 0), "Value"))
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new RenderTableIntoColum(table))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "singleColumnTable.pdf");
	}

	@Test
	void renderTableWithCustomTableAttributes() {
		var table = TableFromMap.builder()
			.header(defaultHeader("A", "B"))
			.cells(Map.of(
				new Cell(0, 0), "A:0",
				new Cell(0, 1), "A:1",
				new Cell(1, 0), "B:0",
				new Cell(1, 1), "B:1"
			))
			.columnWeights(ColumnWeights.fromList(List.of(2.0f, 1.0f)))
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new RenderTableIntoColum(
				table,
				TableAttributes.defaults()
					.withTableWidth(TableWidth.absolute(200.f))
					.withHorizontalAlignment(HorizontalAlignment.LEFT)
					.withRowHeights(Map.of(1, 50.0f)),
				1f, 1f,
				new RegionColumnRenderer.Status(
					1,
					FloatArray.from(133.33333f, 66.666664f),
					FloatArray.from(16f, 50f, 16f),
					200f,
					82f
				)
			))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "singleColumnTable-with-attributes.pdf");
	}

	@Test
	@DisplayName("render table without headerRows")
	void renderTableWithoutHeader() {
		var table = TableFromMap.builder()
			.cells(Map.of(new Cell(0, 0), "Value"))
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new RenderTableIntoColum(table))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "singleColumnTableWithoutHeader.pdf");
	}

		@Test
	@DisplayName("render table with more rows than visible")
	void renderTableWithMoreRowsThanVisible() {
			var table = TableFromMap.builder()
				.header(defaultHeader("Column"))
				.cells(IntStream.range(0, 100)
					.boxed()
					.map(it -> new Cell(0, it))
					.collect(Collectors.toMap(it -> it, it -> "Row: " + it.row())))
				.build();

			assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.addBlocks(new RenderTableIntoColum(
					table,
					TableAttributes.defaults(),
					0.5f,
					0.3f,
					new RegionColumnRenderer.Status(
						11,
						FloatArray.from(247.5f),
						FloatArray.from(Stream.generate(() -> 16.0f).limit(100 + 1).collect(Collectors.toList())),
						247.5f,
						1616f
					)
				))
				.build())
				.expectRendering()
				.matchesResource(getClass(), "singleColumnTableWithToManyRows.pdf");
	}

	/**
	 * @see AutosplitTableTest#dontSplitIfFitsOnPage()
	 */
	@Test
	void renderTableIntoSmallBoxBug() {
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
			.addBlocks(new RenderTableIntoColum(
				table,
				TableAttributes.defaults(),
				1.0f,
				1.0f,
				new RegionColumnRenderer.Status(
					15,
					FloatArray.from(Stream.generate(() -> 55.0f).limit(9).collect(Collectors.toList())),
					FloatArray.from(Stream.concat(Stream.of(22.0f), Stream.generate(() -> 44.0f)).limit(31).collect(Collectors.toList())),
					495.0f,
					1342f
				)
			))
			.build())
			.expectRendering();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new RenderTableIntoColum(
				table,
				TableAttributes.defaults(),
				0.5f,
				1f,
				new RegionColumnRenderer.Status(
					29,
					FloatArray.from(Stream.generate(() -> 27.5f).limit(9).collect(Collectors.toList())),
					FloatArray.from(Stream.concat(Stream.of(22.0f), Stream.generate(() -> 20.0f)).limit(31).collect(Collectors.toList())),
					247.5f,
					622f
				)
			))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "renderTableIntoSmallBoxBug.pdf");
	}

	@Test
	void renderSmallTable() {
		TableFromMap table = TableFromMap.builder()
			.header(TableColumnsFromNameList.builder()
				.addColumnNames("A", "B", "C")
				.styles(HeaderStyles.asHeaderStyles(LayeredCellStyles.empty()
					.withDefault(CellStyle.empty()
						.withBackgroundColor(Color.LIGHT_GRAY)
						.withPadding(BorderProperty.of(5.0f))
						.withHorizontalAlignment(HorizontalAlignment.CENTER))))
				.build())
			.styles(LayeredCellStyles.empty()
				.withDefault(CellStyle.empty()
					.withPadding(BorderProperty.of(20.f, 10.0f, 20.f, 10.f))
					.withHorizontalAlignment(HorizontalAlignment.CENTER)))
			.columnWeights(ColumnWeights.EMPTY)
			.cells(new Region(IntRange.to(0, 2), IntRange.to(0, 2))
				.map(Cell::new)
				.stream()
				.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row())))
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new RenderTableIntoColum(
				table,
				TableAttributes.defaults(),
				0.5f,
				1.0f,
				new RegionColumnRenderer.Status(
					2,
					FloatArray.from(Stream.generate(() -> 82.5f).limit(3).collect(Collectors.toList())),
					FloatArray.from(Stream.concat(Stream.of(22.0f), Stream.generate(() -> 32f)).limit(4).collect(Collectors.toList())),
					247.5f,
					118f
				)
			))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "renderSmallTable.pdf");
	}

	private TableColumnsFromNameList defaultHeader(String... names) {
		return TableColumnsFromNameList.builder()
			.addColumnNames(names)
			.styles(DEFAULT_HEADER_STYLES)
			.build();
	}

	record RenderTableIntoColum(
		Table table,
		TableAttributes tableAttributes,
		float columnWidthFactor,
		float columnHeightFactor,
		RegionColumnRenderer.Status expectedStatus
	) implements Block {

		RenderTableIntoColum(Table table) {
			this(table, TableAttributes.defaults(),  1f, 1f, null);
		}

		@Override
		public void render(Document document, Supplier<PdfContentByte> directContent) {
			var innerBox = PageBox.innerBox(document);
			var columnBox = new PageBox(
				innerBox.left(),
				innerBox.bottom(),
				innerBox.width() * columnWidthFactor,
				innerBox.height() * columnHeightFactor
			);

			var column = ColumnFactory.DEFAULT.create(directContent.get(), columnBox);

			RegionColumnRenderer regionColumnRenderer = new DefaultRegionColumnRenderer();
			Region region = table.maxRegion();
			var currentStatus = regionColumnRenderer
				.render(column, table, tableAttributes, region);

			if (expectedStatus != null) {
				assertThat(currentStatus).isEqualTo(expectedStatus);
			}

			column.go();
		}

	}

}