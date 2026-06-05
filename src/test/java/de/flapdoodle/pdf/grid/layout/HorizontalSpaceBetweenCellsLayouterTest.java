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
package de.flapdoodle.pdf.grid.layout;

import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.types.Cell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HorizontalSpaceBetweenCellsLayouterTest {

	@Test
	@DisplayName("free space around if only one cell")
	void freeSpaceAroundIfOnlyOneCell() {
		var grid = Grid.of(1, 100f, 3, 50f);
		var cells = Set.of(new Cell(0, 1));
		var container = new PageBox(0f, 0f, 200f, 30f);

		var leftOffset = HorizontalSpaceBetweenCellsLayouter.internalLeftOffset(grid, cells, container);

		assertThat(leftOffset.apply(new Cell(0, 1))).isEqualTo(50f);
	}

	@Test
	@DisplayName("free space between more than one cell")
	void freeSpaceBetweenMoreThanOneCell() {
		var grid = Grid.of(2, 100f, 3, 50f);
		var cells = Set.of(new Cell(0, 1), new Cell(1, 1));
		var container = new PageBox(0f, 0f, 250f, 30f);

		var leftOffset = HorizontalSpaceBetweenCellsLayouter.internalLeftOffset(grid, cells, container);

		assertThat(leftOffset.apply(new Cell(0, 1))).isEqualTo(0f);
		assertThat(leftOffset.apply(new Cell(1, 1))).isEqualTo(50f);
	}

}