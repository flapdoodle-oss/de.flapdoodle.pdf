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
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.*;
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;
import org.junit.jupiter.api.Test;
import org.openpdf.text.PageSize;

import java.awt.*;
import java.util.stream.Collectors;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

public class AutosplitTableTest {
	private final CellStyles DEFAULT_STYLES = LayeredCellStyles.empty();
	private final HeaderStyles DEFAULT_HEADER_STYLES = DEFAULT_STYLES.asHeaderStyles();

	@Test
	void dontSplitIfFitsOnPage() {
		TableFromMap table = table(20, "A", "B", "C", "D", "E", "F", "G");

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(AutosplitTable.builder()
				.table(table)
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "autosplit-dontSplitIfFitsOnPage.pdf");
	}

	@Test
	void splitLongTable() {
		TableFromMap table = table(30, "A", "B", "C", "D", "E", "F", "G");

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(AutosplitTable.builder()
				.table(table)
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "autosplit-split-long-table.pdf");
	}

	@Test
	void splitWideTable() {
		TableFromMap table = table(20, "A", "B", "C", "D", "E", "F", "G", "H", "I");

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(AutosplitTable.builder()
				.table(table)
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "autosplit-split-wide-table.pdf");
	}

	@Test
	void splitLongWideTable() {
		TableFromMap table = table(40, "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M",
			"O", "P", "Q", "R", "S", "T", "U", "V"
		);

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(AutosplitTable.builder()
				.table(table)
				.repeatHeader(true)
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "autosplit-split-long-wide-table.pdf");
	}

	@Test
	void splitLongWideTableWithKeyColumns() {
		TableFromMap table = table(40, "KEY", "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M",
			"O", "P", "Q", "R", "S", "T", "U", "V"
		);

		TableFromMap alteredTable = TableFromMap.builder()
			.from(table)
			.styles(table.styles().overrideWith(LayeredCellStyles.empty()
				.forColumn(0, CellStyle.empty().withBackgroundColor(Color.LIGHT_GRAY))))
			.build();

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(AutosplitTable.builder()
				.table(alteredTable)
				.repeatHeader(true)
				.keyColumns(1)
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "autosplit-split-long-wide-table-with-key-column.pdf");
	}



	private static TableFromMap table(int rows, String... columns) {
		return TableFromMap.builder()
			.header(TableColumnsFromNameList.builder()
				.addColumnNames(columns)
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
			.cells(new Region(IntRange.until(0, columns.length), IntRange.until(0, rows))
				.map(Cell::new)
				.stream()
				.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row())))
			.build();
	}

}