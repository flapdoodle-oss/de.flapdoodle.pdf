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
import de.flapdoodle.pdf.DocumentFactoryAssert;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.LayeredCellStyles;
import de.flapdoodle.pdf.types.Cell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfContentByte;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ColumnTableRendererTest {
	private TableFromMap table = TableFromMap.builder()
		.header(TableColumnsFromNameList.builder()
			.addColumnNames("A", "B", "C", "D", "E")
			.styles(LayeredCellStyles.empty().asHeaderStyles())
			.build())
		.cells(IntStream.range(0, 30)
			.mapToObj(it -> new Cell(0, it))
			.collect(Collectors.toMap(
				it -> it,
				it -> "Row: "+it.row()
			)))
		.build();
	
	@Test
	@DisplayName("minimal table size for table with same column weights")
	void minimalTableSizeForTableWithSameColumnWeights() {
		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.blocks(List.of(new Render(table)))
				.build())
			.expectRendering()
			.matchesResource(getClass(), "tableMinWidth.pdf");
	}

	@Test
	@DisplayName("minimal table size for table with bigger first column")
	void minimalTableSizeForTableWithBiggerFirstColumn() {
		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.blocks(List.of(new Render(TableFromMap.builder()
					.from(table)
					.columnWeights(ColumnWeights.fromMap(Map.of(0, 2f)))
					.build())))
				.build())
			.expectRendering()
			.matchesResource(getClass(), "tableMinWidthWithBiggerColumn.pdf");
	}

	record Render(
		Table table
	) implements Block {

	@Override
	public void render(Document document, Supplier<PdfContentByte> directContent) {
			var testee = new ColumnTableRenderer(directContent.get(), new DefaultRegionColumnRenderer());
			var innerBox = PageBox.innerBox(document);
		var minWidth = new FindSmallestTableWidth().of(testee, table, innerBox.width()).width();
			testee.render(table, table.maxRegion(), innerBox.withWidth(minWidth))
				.commit().run();
		}
	}
}