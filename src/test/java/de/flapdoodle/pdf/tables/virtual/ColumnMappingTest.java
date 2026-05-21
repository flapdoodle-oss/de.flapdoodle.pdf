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

import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.types.Cell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ColumnMappingTest {
	@Test
	@DisplayName("map tables side by side")
	void mapTablesSideBySide() {
		var first = TableFromMap.builder()
			.cells(Map.of(new Cell(0, 0), "A", new Cell(2, 0), "B"))
			.build();

		var second = TableFromMap.builder()
			.cells(Map.of(new Cell(1, 1), "C"))
			.build();

		var third = TableFromMap.builder()
			.cells(Map.of(new Cell(10, 2), "X"))
			.build();

		var testee = new ColumnMapping(List.of(first, second, third));

		assertThat(testee.get(0)).isEqualTo(new ColumnMapping.TableAndColumn(first, 0));
		assertThat(testee.get(2)).isEqualTo(new ColumnMapping.TableAndColumn(first, 2));
		assertThat(testee.get(3)).isEqualTo(new ColumnMapping.TableAndColumn(second, 0));
		assertThat(testee.get(4)).isEqualTo(new ColumnMapping.TableAndColumn(second, 1));
		assertThat(testee.get(5)).isEqualTo(new ColumnMapping.TableAndColumn(third, 0));
		assertThat(testee.get(14)).isEqualTo(new ColumnMapping.TableAndColumn(third, 9));

		assertThat(testee.get(new Cell(0, 0))).isEqualTo(new ColumnMapping.TableAndCell(first, new Cell(0, 0)));
		assertThat(testee.get(new Cell(2, 0))).isEqualTo(new ColumnMapping.TableAndCell(first, new Cell(2, 0)));
		assertThat(testee.get(new Cell(3, 0))).isEqualTo(new ColumnMapping.TableAndCell(second, new Cell(0, 0)));
		assertThat(testee.get(new Cell(4, 0))).isEqualTo(new ColumnMapping.TableAndCell(second, new Cell(1, 0)));
		assertThat(testee.get(new Cell(5, 0))).isEqualTo(new ColumnMapping.TableAndCell(third, new Cell(0, 0)));
		assertThat(testee.get(new Cell(14, 0))).isEqualTo(new ColumnMapping.TableAndCell(third, new Cell(9, 0)));
	}

}