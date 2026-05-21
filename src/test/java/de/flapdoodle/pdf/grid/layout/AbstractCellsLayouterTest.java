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
import de.flapdoodle.pdf.types.Floats;
import de.flapdoodle.pdf.types.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractCellsLayouterTest {
	private float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}
	
	@RepeatedTest(100)
	@DisplayName("position cells relative to offset")
	void positionCellsRelativeToOffset() {
		var left = nextFloat() * 1010f - 10f;
		var top = nextFloat() * 1010f - 10f  ;
		var pageLeft = nextFloat() * 1010f - 10f;
		var pageBottom = nextFloat() * 1010f - 10f;

		var pageHeight = 200f;
		var leftOffset = 11f;

		var grid = new Grid(3, 100f, 4, 200f);
		var cells = Set.of(new Cell(0, 0), new Cell(1, 2));

		var testee = new AbstractCellsLayouter() {
			@Override
			Function<Cell, Float> leftOffset(Grid grid, Set<Cell> cells, PageBox container) {
				return it -> leftOffset;
			}
		};

		var layoutedCells = testee
			.layout(grid, new Position(left, top), cells, new PageBox(pageLeft, pageBottom, 10f, pageHeight));

		layoutedCells.forEach( cellLayout -> {
			var gridBox = grid.asBox(cellLayout.cell());
			assertThat(Floats.isNearBy(cellLayout.cellBox().left(), gridBox.left() - left + pageLeft + leftOffset))
				.describedAs("left for cell "+cellLayout.cell()+" (page: "+pageLeft+","+pageBottom+")")
				.isTrue();
			assertThat(Floats.isNearBy(cellLayout.cellBox().bottom(), pageBottom + pageHeight - gridBox.top() - gridBox.height() + top))
				.describedAs("bottom for cell "+cellLayout.cell()+" (page: "+pageLeft+","+pageBottom+")")
				.isTrue();
		});
	}

}