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

import de.flapdoodle.pdf.render.table.TableAttributes;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.TableColumns;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HeaderStyles;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.LayeredCellStyles;
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SplittedTableTest {

	@Test
	void mapColumnsToSourceColumn() {
		var table = table(7, "A", "B", "C", "D", "E", "F");

		int keyColumns = 1;
		int columnOffset = 2;
		int rowOffset = 2;

		SplittedTable testee = new SplittedTable(
			table,
			TableAttributes.defaults(),
			new Region(
				IntRange.to(columnOffset, 4),
				IntRange.to(rowOffset, 5)
			),
			true,
			keyColumns);

		assertThat(table.columns()).isEqualTo(6);
		assertThat(table.rows()).isEqualTo(7);

		assertThat(testee.columns()).isEqualTo(4);
		assertThat(testee.rows()).isEqualTo(4);

		// key column must match
		for (int row : IntRange.until(0, testee.rows())) {
			assertThat(testee.get(new Cell(0, row))).isPresent()
				.contains(table.get(new Cell(0, rowOffset + row)).get());
		}

		TableColumns tableHeader = table.header().get();
		TableColumns testeeHeader = testee.header().get();

		for (int column : IntRange.until(keyColumns, testee.columns())) {
			assertThat(testeeHeader.get(column)).isPresent()
				.contains(tableHeader.get(columnOffset + column - keyColumns).get());
			assertThat(testeeHeader.styles().get(column))
				.isEqualTo(tableHeader.styles().get(columnOffset + column - keyColumns));
			assertThat(testee.columnWeights().get(column))
				.isEqualTo(table.columnWeights().get(columnOffset + column - keyColumns));

			for (int row : IntRange.until(0, testee.rows())) {
				assertThat(testee.get(new Cell(column, row))).isPresent()
					.contains(table.get(new Cell(columnOffset + column - keyColumns, rowOffset + row)).get());
				assertThat(testee.styles().get(new Cell(column, row)))
					.isEqualTo(table.styles().get(new Cell(columnOffset + column - keyColumns, rowOffset + row)));
			}
		}
	}

	private static TableFromMap table(int rows, String... columns) {
		return TableFromMap.builder()
			.header(TableColumnsFromNameList.builder()
				.addColumnNames(columns)
				.styles(HeaderStyles.asHeaderStyles(LayeredCellStyles.empty()
					.withDefault(CellStyle.empty()
						.withBackgroundColor(Color.LIGHT_GRAY)
						.withPadding(BorderProperty.of(5.0f))
						.withHorizontalAlignment(HorizontalAlignment.CENTER))
					.forColumn(3, CellStyle.empty().withBackgroundColor(Color.YELLOW))))
				.build())
			.styles(LayeredCellStyles.empty()
				.withDefault(CellStyle.empty()
					.withPadding(BorderProperty.of(20.f, 10.0f, 20.f, 10.f))
					.withHorizontalAlignment(HorizontalAlignment.CENTER))
				.forRow(2, CellStyle.empty().withBackgroundColor(Color.BLACK))
				.forColumn(3, CellStyle.empty().withBackgroundColor(Color.RED))
				.forCell(new Cell(1, 1), CellStyle.empty().withBackgroundColor(Color.GREEN)))
			.columnWeights(ColumnWeights.fromMap(Map.of(1, 1.0f, 3, 3f, 5, 5f)))
			.cells(new Region(IntRange.until(0, columns.length), IntRange.until(0, rows))
				.map(Cell::new)
				.stream()
				.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row())))
			.build();
	}

}