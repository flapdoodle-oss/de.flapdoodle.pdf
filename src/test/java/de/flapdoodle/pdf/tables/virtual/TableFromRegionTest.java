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
package de.flapdoodle.pdf.tables.virtual;

import de.flapdoodle.pdf.tables.*;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.tables.cells.HeaderStyles;
import de.flapdoodle.pdf.tables.cells.LayeredCellStyles;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TableFromRegionTest {
	private CellStyles styles = LayeredCellStyles.empty()
		.forCell(new Cell(2, 2), CellStyle.empty().withBackgroundColor(Color.RED));

	private HeaderStyles headerStyles = LayeredCellStyles.empty()
		.forCell(new Cell(3, 0), CellStyle.empty().withBackgroundColor(Color.BLUE))
		.asHeaderStyles();

	private TableColumns header = TableColumnsFromNameList.builder()
		.addColumnNames("A", "B", "C", "D", "E")
		.styles(headerStyles)
		.build();

	private ColumnWeights columWeights = ColumnWeights.fromMap(Map.of(0, 2f, 1, 4f, 2, 8f, 3, 6f, 4, 3f));

	private Table sourceTable = TableFromMap.builder()
		.header(header)
		.styles(styles)
		.columnWeights(columWeights)
		.cells(cells(5, 7))
		.build();

	@Test
	  @DisplayName("instance with same region as source table should behave the same")
	void instanceWithSameRegionAsSourceTableShouldBehaveTheSame() {
		var subRegion = sourceTable.maxRegion();
		var testee = new TableFromRegion(sourceTable, subRegion);

		assertThat(testee.columns()).isEqualTo(sourceTable.columns());
		assertThat(testee.rows()).isEqualTo(sourceTable.rows());

		for (int c : IntRange.until(0, testee.columns())) {
			assertThat(testee.columnWeights().get(c)).isEqualTo(sourceTable.columnWeights().get(c));

			assertThat(testee.header()).isPresent();
			assertThat(sourceTable.header()).isPresent();
			
			assertThat(testee.header().get().get(c)).isEqualTo(sourceTable.header().get().get(c));
			assertThat(testee.header().get().styles().get(c)).isEqualTo(sourceTable.header().get().styles().get(c));

			for (int r : IntRange.until(0, testee.rows())) {
				assertThat(testee.get(new Cell(c, r))).isEqualTo(sourceTable.get(new Cell(c, r)));
				assertThat(testee.styles().get(new Cell(c, r))).isEqualTo(sourceTable.styles().get(new Cell(c, r)));
			}
		}
	}

	@Test
	@DisplayName("instance with sub region must map source to new region")
	void instanceWithSubRegionMustMapSourceToNewRegion() {
		var columnOffset = 2;
		var rowOffset = 1;

		var subRegion = sourceTable.maxRegion()
			.fromRow(rowOffset).get()
        .untilRow(6)
			.fromColumn(columnOffset).get()
        .untilColumn(4);

		var testee = new TableFromRegion(sourceTable, subRegion);

		assertThat(testee.columns()).isEqualTo(subRegion.columns().count());
		assertThat(testee.rows()).isEqualTo(subRegion.rows().count());

		for (int c : IntRange.until(0, testee.columns())) {
			assertThat(testee.columnWeights().get(c)).isEqualTo(sourceTable.columnWeights().get(c + columnOffset));

			assertThat(testee.header()).isPresent();
			assertThat(sourceTable.header()).isPresent();

			assertThat(testee.header().get().get(c)).isEqualTo(sourceTable.header().get().get(c + columnOffset));
			assertThat(testee.header().get().styles().get(c)).isEqualTo(sourceTable.header().get().styles().get(c + columnOffset));

			for (int r : IntRange.until(0,  testee.rows())) {
				assertThat(testee.get(new Cell(c, r))).isEqualTo(sourceTable.get(new Cell(c + columnOffset, r + rowOffset)));
				assertThat(testee.styles().get(new Cell(c, r))).isEqualTo(sourceTable.styles().get(new Cell(c + columnOffset, r + rowOffset)));
			}
		}
	}

	private Map<Cell, String> cells(int columns, int rows) {
		var map = new LinkedHashMap<Cell, String>();
		for (int c : IntRange.until(0, columns)) {
			for (int r : IntRange.until(0, rows)) {
				map.put(new Cell(c, r), "$c:$r");
			}
		}
		return map;
	}
}