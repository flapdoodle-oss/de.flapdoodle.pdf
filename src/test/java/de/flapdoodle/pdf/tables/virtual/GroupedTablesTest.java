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

import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumnsFromNameList;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.LayeredCellStyles;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class GroupedTablesTest {
	@Test
	@DisplayName("grouped tables")
	void groupedTables() {
		var rows = 5;
		var first = table("first", List.of("A", "B"), rows);
		var second = table("second", List.of("1", "2", "3"), rows);
		var third = table("third", List.of("Y", "Z"), rows);

		var testee = new GroupedTables(List.of(first, second, third));

		assertThat(testee.rows()).isEqualTo(rows);
		assertThat(testee.columns()).isEqualTo(first.columns() + second.columns() + third.columns());

		expectMapping("first", testee, first, 0);
		expectMapping("second", testee, second, first.columns());
		expectMapping("third", testee, third, first.columns() + second.columns());
	}

	private void expectMapping(String label, GroupedTables testee, Table source, int columnOffset) {
		for (int c : IntRange.until(0, source.columns())) {
			assertThat(testee.columnWeights().get(c + columnOffset))
				.describedAs(label+": column weights for "+c)
				.isEqualTo(source.columnWeights().get(c));

			assertThat(testee.header()).isPresent();
			assertThat(testee.header().get().get(c + columnOffset)).isEqualTo(source.header().get().get(c));
			assertThat(testee.header().get().styles().get(c + columnOffset)).isEqualTo(source.header().get().styles().get(c));

			for (int r : IntRange.until(0, testee.rows())) {
				assertThat(testee.get(new Cell(c + columnOffset, r))).isEqualTo(source.get(new Cell(c, r)));
				assertThat(testee.styles().get(new Cell(c + columnOffset, r))).isEqualTo(source.styles().get(new Cell(c, r)));
			}
		}
	}

	private TableFromMap table(String name, List<String> columns,int rows) {
		return TableFromMap.builder()
			.header(TableColumnsFromNameList.builder().columnNames(columns)
				.styles(cellStyles(columns.size(), 1)
					.asHeaderStyles())
				.build())
			.styles(cellStyles(columns.size(), rows))
			.columnWeights(ColumnWeights.fromMap(
				IntStream.range(0, columns.size())
					.boxed()
					.collect(Collectors.toMap(it -> it, it -> it * 2.f))))
			.cells(cells(name, columns.size(), rows))
			.build();
	}

	private Map<Cell, String> cells(String prefix, int columns,int rows) {
		var map = new LinkedHashMap<Cell, String>();

		for (int c : IntRange.until(0, columns)) {
			for (int r : IntRange.until(0, rows)) {
				map.put(new Cell(c, r), "$prefix-$c:$r");
			}
		}
		
		return map;
	}

	private LayeredCellStyles cellStyles(int columns,int rows) {
		var ret = LayeredCellStyles.empty();

		for (int c : IntRange.until(0, columns)) {
			for (int r : IntRange.until(0, rows)) {
				ret = ret.forCell(new Cell(c, r), CellStyle.empty().withBackgroundColor(new Color(255 * c / columns, 255 * r / rows, 0)));
			}
		}

		return ret;
	}


}