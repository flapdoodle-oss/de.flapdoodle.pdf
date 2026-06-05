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
package de.flapdoodle.pdf.grid;

import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Dimension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class GridTest {
	private float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	@Test
	@DisplayName("grid with single cell")
	void gridWithSingleCell() {
		var width = nextFloat() * 999.9f + 0.1f;
		var height = nextFloat() * 999.9f + 0.1f;

		var testee = Grid.of(width, height);

		assertThat(testee.columns()).isEqualTo(1);
		assertThat(testee.rows()).isEqualTo(1);
		assertThat(testee.get(new Cell(0, 0))).isEqualTo(new Dimension(width, height));
	}

	@Test
	@DisplayName("grid with 3 columns and 4 rows cell")
	void gridWithThreeColumnsAnd4RowsCell() {
		var width = nextFloat() * 999.9f + 0.1f;
		var height = nextFloat() * 999.9f + 0.1f;

		var testee = Grid.of(Margin.none(), 3, width, 4, height);

		assertThat(testee.columns()).isEqualTo(3);
		assertThat(testee.rows()).isEqualTo(4);
		for (int c=0;c<testee.columns();c++) {
			for (int r=0;r<testee.rows();r++) {
				assertThat(testee.get(new Cell(c, r))).isEqualTo(new Dimension(width, height));
			}
		}
	}

	@Test
	@DisplayName("nextCell gives cell on the right or first on next line until last line")
	void nextCellGivesCellOnTheRightOrFirstOnNextLineUntilLastLine() {
		var width = nextFloat() * 999.9f + 0.1f;
		var height = nextFloat() * 999.9f + 0.1f;

		var testee = Grid.of(Margin.none(), 3, width, 4, height);

		assertThat(testee.nextCell(new Cell(0, 0))).contains(new Cell(1, 0));
		assertThat(testee.nextCell(new Cell(2, 0))).contains(new Cell(0, 1));
		assertThat(testee.nextCell(new Cell(2, 3))).isEmpty();
	}

	@Test
	@DisplayName("cellInNextRow gives cell gives cell if rows left")
	void cellInNextRowGivesCellIfRowsLeft() {
		var width = nextFloat() * 999.9f + 0.1f;
		var height = nextFloat() * 999.9f + 0.1f;

		var testee = Grid.of(Margin.none(), 3, width, 4, height);

		assertThat(testee.cellInNextRow(new Cell(0, 0))).contains(new Cell(0, 1));
		assertThat(testee.cellInNextRow(new Cell(2, 0))).contains(new Cell(2, 1));
		assertThat(testee.cellInNextRow(new Cell(2, 3))).isEmpty();
	}

	@Test
	@DisplayName("shrink grid height if possible")
	void shrinkGridHeightIfPossible() {
		var width = nextFloat() * 999.9f + 0.1f;
		var height = nextFloat() * 999.9f + 0.1f;

		var testee = Grid.of(Margin.none(), 3, width, 4, height);

		for (int c=0;c<testee.columns();c++) {
			assertThat(testee.get(new Cell(c, 1))).isEqualTo(new Dimension(width, height));
		}

		var shrinked = testee.shrinkHeight(Map.of(
			new Cell(0, 1),height / 2,
			new Cell(1, 1), height / 3,
			new Cell(2, 1), height / 4
			));

		for (int c=0;c<testee.columns();c++) {
			assertThat(shrinked.get(new Cell(c, 1))).isEqualTo(new Dimension(width, height / 2));
		}
	}

}