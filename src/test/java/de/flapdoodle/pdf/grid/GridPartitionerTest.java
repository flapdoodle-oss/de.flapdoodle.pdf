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
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GridPartitionerTest {
	private float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	@Test
	@DisplayName("partition 3x2 grid")
	void partition3x2Grid() {
		var gridColumnWidth = nextFloat() * 70f + 30f;
		var gridRowHeight = nextFloat() * 110f + 50f;
		var pageSize = new Dimension((2 * gridColumnWidth) + 5f, gridRowHeight + 5f);
		var grid = Grid.of(Margin.none(), 3, gridColumnWidth, 2, gridRowHeight);

		var cellSets = GridPartitioner.partition(grid, 0f, pageSize);

		assertThat(cellSets).hasSize(4);
		assertThat(cellSets.get(0).cells()).containsExactly(new Cell(0, 0), new Cell(1, 0));
		assertThat(cellSets.get(1).cells()).containsExactly(new Cell(2, 0));
		assertThat(cellSets.get(2).cells()).containsExactly(new Cell(0, 1), new Cell(1, 1));
		assertThat(cellSets.get(3).cells()).containsExactly(new Cell(2, 1));
	}

	@Nested
	class Partition {

		@Test
		@DisplayName("6x2 grid will fit onto 4 pages")
		void gridWillFitOnto4Pages() {
			var width = nextFloat() * 99f + 1f;
			var height = nextFloat() * 99f + 1f;
			var grid = Grid.of(Margin.none(), 6, width, 2, height);
			var pageSize = new Dimension(width * 3.1f, height * 1.1f);

			var result = GridPartitioner.partition(grid, 0f, pageSize);

			assertThat(result).hasSize(4);

			assertThat(result.get(0).position()).isEqualTo(new Position(0f, 0f));
			assertThat(result.get(0).onNewPage()).isFalse();
			assertThat(result.get(0).cells()).containsExactly(new Cell(0, 0), new Cell(1, 0), new Cell(2, 0));

			assertThat(result.get(1).position()).isEqualTo(new Position(width * 3f, 0f));
			assertThat(result.get(1).onNewPage()).isTrue();
			assertThat(result.get(1).cells()).containsExactly(new Cell(3, 0), new Cell(4, 0), new Cell(5, 0));

			assertThat(result.get(2).position()).isEqualTo(new Position(0f, height));
			assertThat(result.get(2).onNewPage()).isTrue();
			assertThat(result.get(2).cells()).containsExactly(new Cell(0, 1), new Cell(1, 1), new Cell(2, 1));

			assertThat(result.get(3).position()).isEqualTo(new Position(width * 3f, height));
			assertThat(result.get(3).onNewPage()).isTrue();
			assertThat(result.get(3).cells()).containsExactly(new Cell(3, 1), new Cell(4, 1), new Cell(5, 1));
		}

		@Test
		@DisplayName("6x2 grid with initial offset will fit onto 4 pages with first cell on new page")
		void gridWithInitialOffsetWillFitOnto4PagesWitFirstCellOnNewPage() {
			var width = nextFloat() * 99f + 1f;
			var height = nextFloat() * 99f + 1f;
			var grid = Grid.of(Margin.none(), 6, width, 2, height);
			var pageSize = new Dimension(width * 3.1f, height * 1.1f);

			var result = GridPartitioner.partition(grid, height * 0.9f, pageSize);

			assertThat(result).hasSize(4);

			assertThat(result.get(0).position()).isEqualTo(new Position(0f, 0f));
			assertThat(result.get(0).onNewPage()).isTrue();
			assertThat(result.get(0).cells()).containsExactly(new Cell(0, 0), new Cell(1, 0), new Cell(2, 0));

			assertThat(result.get(1).position()).isEqualTo(new Position(width * 3f, 0f));
			assertThat(result.get(1).onNewPage()).isTrue();
			assertThat(result.get(1).cells()).containsExactly(new Cell(3, 0), new Cell(4, 0), new Cell(5, 0));

			assertThat(result.get(2).position()).isEqualTo(new Position(0f, height));
			assertThat(result.get(2).onNewPage()).isTrue();
			assertThat(result.get(2).cells()).containsExactly(new Cell(0, 1), new Cell(1, 1), new Cell(2, 1));

			assertThat(result.get(3).position()).isEqualTo(new Position(width * 3f, height));
			assertThat(result.get(3).onNewPage()).isTrue();
			assertThat(result.get(3).cells()).containsExactly(new Cell(3, 1), new Cell(4, 1), new Cell(5, 1));
		}
	}

	@Nested
	class PartitionFunction {

		@Test
		@DisplayName("element bigger than max will throw error")
		void elementBiggerThanMaxWillThrowError() {
			assertThatThrownBy(() ->
				GridPartitioner.partition(List.of(10f), 9f)
			).isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("single element will give one range")
		void singleElementWillGiveOneRange() {
			var value = nextFloat() * 99.9f + 0.1f;

			var result = GridPartitioner.partition(List.of(value), value * 1.1f);

			assertThat(result).hasSize(1);
			assertThat(result.get(0)).isEqualTo(IntRange.at(0));
		}

		@Test
		@DisplayName("single element with initialOffset bigger than value will give 2 ranges with first empty")
		void singleElementWithInitialOffsetWillGive2Ranges() {
			var value = nextFloat() * 99.9f + 0.1f;

			var result = GridPartitioner.partition(List.of(value), value * 1.1f, value * 0.9f);

			assertThat(result).hasSize(2);
			assertThat(result.get(0).isEmpty()).isTrue();
			assertThat(result.get(1)).isEqualTo(IntRange.at(0));
		}

		@Test
		@DisplayName("two elements will give two ranges if both does not fit into max")
		void twoElementsWillGiveTwoRanges() {
			var value = nextFloat() * 99.9f + 0.1f;

			var result = GridPartitioner.partition(List.of(value, value), value * 1.1f);

			assertThat(result).hasSize(2);
			assertThat(result.get(0)).isEqualTo(IntRange.at(0));
			assertThat(result.get(1)).isEqualTo(IntRange.at(1));
		}

		@Test
		@DisplayName("three elements will give two ranges")
		void threeElementsWillGiveTwoRanges() {
			var value = nextFloat() * 99.9f + 0.1f;

			var result = GridPartitioner.partition(List.of(value * 2f, value, value), value * 2f * 1.1f);

			assertThat(result).hasSize(2);
			assertThat(result.get(0)).isEqualTo(IntRange.at(0));
			assertThat(result.get(1)).isEqualTo(IntRange.to(1, 2));
		}

		@Test
		@DisplayName("each range has three columns")
		void eachRangeHasThreeColumns() {
			var value = nextFloat() * 99.9f + 0.1f;
			var size = ThreadLocalRandom.current().nextInt(1, 11);
			var values = IntStream.range(0, size)
				.boxed()
				.flatMap(it -> Stream.of(value, value, value))
				.toList();

			var result = GridPartitioner.partition(values, value * 3f * 1.1f);

			assertThat(result).hasSize(size);
			result.forEach(range -> {
				assertThat(range.size()).isEqualTo(3);
			});
		}

		@Test
		@DisplayName("dont repeat range if max is same as value")
		void dontRepeatRangeIfMaxIsSameAsValue() {
			var value = nextFloat() * 99.9f + 0.1f;
			var size = ThreadLocalRandom.current().nextInt(1, 11);
			var values = IntStream.range(0, size).boxed()
				.map(it -> value)
				.toList();

			var result = GridPartitioner.partition(values, value);

			assertThat(result).hasSize(size);
			result.forEach(range -> {
				assertThat(range.size()).isEqualTo(1);
			});
		}
	}
}
