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
package de.flapdoodle.pdf.tables.cells;

import de.flapdoodle.pdf.types.Cell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;

class LayeredCellStylesTest {
	@Test
	@DisplayName("must give specific style")
	void mustGiveSpecificStyle() {
		var defaultCellSyle = CellStyle.empty().withBackgroundColor(Color.BLACK);

		var testee = LayeredCellStyles.empty().withDefault(defaultCellSyle)
			.forRow(1, defaultCellSyle.withBackgroundColor(Color.RED))
			.forColumn(2, defaultCellSyle.withBackgroundColor(Color.BLUE))
			.forCell(new Cell(2, 1), defaultCellSyle.withBackgroundColor(Color.GREEN));

		assertThat(testee.get(new Cell(0, 0)).backgroundColor()).contains(Color.BLACK);
		assertThat(testee.get(new Cell(0, 1)).backgroundColor()).contains(Color.RED);
		assertThat(testee.get(new Cell(2, 0)).backgroundColor()).contains(Color.BLUE);
		assertThat(testee.get(new Cell(2, 1)).backgroundColor()).contains(Color.GREEN);
	}
}