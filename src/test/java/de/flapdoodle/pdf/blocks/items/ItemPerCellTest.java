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
package de.flapdoodle.pdf.blocks.items;

import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.pages.PageBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemPerCellTest {

	private float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	@Test
	@DisplayName("can place items in grid")
	void canPlaceItemsInGrid() {
		var gridWidth = nextFloat() * 90f + 10f;
		var gridHeight = nextFloat() * 90f + 10f;
		var renderHeight = nextFloat() * 90f + 10f;

		var grid = new Grid(2, gridWidth, 2, gridHeight);
		ItemRenderer<Column, String> itemRenderer = (box, item) -> Optional.of(renderHeight);

		var testee = new ItemPerCell<>(itemRenderer);

		var result = testee.placeInGrid(
			grid,
			Column::new,
			it -> null,
			List.of("A", "B", "C", "D")
		);

		assertThat(result).hasSize(4);
		assertThat(result.get(0).item()).isEqualTo("A");
		assertThat(result.get(0).height()).contains(renderHeight);
		assertThat(result.get(1).item()).isEqualTo("B");
		assertThat(result.get(1).height()).contains(renderHeight);
		assertThat(result.get(2).item()).isEqualTo("C");
		assertThat(result.get(2).height()).contains(renderHeight);
		assertThat(result.get(3).item()).isEqualTo("D");
		assertThat(result.get(3).height()).contains(renderHeight);
	}

	@Test
	@DisplayName("too many items for grid")
	void tooManyItemsForGrid() {
		var gridWidth = nextFloat() * 90f + 10f;
		var gridHeight = nextFloat() * 90f + 10f;
		var renderHeight = nextFloat() * 90f + 10f;

		var grid = new Grid(2, gridWidth, 2, gridHeight);
		ItemRenderer<Column, String> itemRenderer = (box, item) -> Optional.of(renderHeight);

		var testee = new ItemPerCell<>(itemRenderer);

		assertThatThrownBy(() -> testee.placeInGrid(
			grid,
			Column::new,
			it -> null,
			List.of("A", "B", "C", "D", "E")
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("fail on render error")
	void failOnRenderError() {
		var gridWidth = nextFloat() * 90f + 10f;
		var gridHeight = nextFloat() * 90f + 10f;

		var grid = new Grid(2, gridWidth, 2, gridHeight);
		ItemRenderer<Column, String> itemRenderer = (box, item) -> Optional.empty();

		var testee = new ItemPerCell<>(itemRenderer);

		assertThatThrownBy(() -> testee.placeInGrid(
			grid,
			Column::new,
			it -> "error",
			List.of("A")
		)).isInstanceOf(IllegalArgumentException.class);
	}

	record Column(PageBox pageBox) {
	}
}