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

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.columns.ColumnFactory;
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

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	@DisplayName("render table without header")
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
					0.5f,
					0.3f,
					new RegionColumnRenderer.Status(11, 1616f)
				))
				.build())
				.expectRendering()
				.matchesResource(getClass(), "singleColumnTableWithToManyRows.pdf");
	}

	private TableColumnsFromNameList defaultHeader(String... names) {
		return TableColumnsFromNameList.builder()
			.addColumnNames(names)
			.styles(DEFAULT_HEADER_STYLES)
			.build();
	}

	record RenderTableIntoColum(
		Table table,
		float columnWidthFactor,
		float columnHeightFactor,
		RegionColumnRenderer.Status expectedStatus
	) implements Block {

		RenderTableIntoColum(Table table) {
			this(table, 1f, 1f, null);
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

			var currentStatus = new DefaultRegionColumnRenderer().render(
				column,
				table,
				table.maxRegion()
			);

			if (expectedStatus != null) {
				assertThat(currentStatus).isEqualTo(expectedStatus);
			}

			column.go();
		}

	}

}