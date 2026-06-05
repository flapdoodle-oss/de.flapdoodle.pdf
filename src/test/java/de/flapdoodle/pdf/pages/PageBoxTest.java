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
package de.flapdoodle.pdf.pages;

import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.Dimension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageBoxTest {

	@Nested
	class BoxPlacement {
		private static final PageBox outer = new PageBox(10.0f, 10.0f, 200.0f, 100.0f);
		private static final Dimension box = new Dimension(30.0f, 30.0f);

		record Placement(
			HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment,
			float leftOffet, float bottomOffset
		) {}

		public static List<Placement> placements() {
			float centerOffset = outer.width() / 2.0f - box.width() / 2.0f;
			float rightOffset = outer.width()- box.width();
			float middleOffset = outer.height() / 2.0f - box.height() / 2.0f;
			float topOffset = outer.height() - box.height();

			return List.of(
				new Placement(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM, 0, 0),
				new Placement(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM, centerOffset, 0),
				new Placement(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM, rightOffset, 0),
				new Placement(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE, rightOffset, middleOffset),
				new Placement(HorizontalAlignment.RIGHT, VerticalAlignment.TOP, rightOffset, topOffset),
				new Placement(HorizontalAlignment.CENTER, VerticalAlignment.TOP, centerOffset, topOffset),
				new Placement(HorizontalAlignment.LEFT, VerticalAlignment.TOP, 0, topOffset),
				new Placement(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE, 0, middleOffset),
				new Placement(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, centerOffset, middleOffset)
			);
		}

		@ParameterizedTest
		@MethodSource("placements")
		void boxPlacement(Placement placement) {
			PageBox result = outer.boxAt(
				box,
				placement.horizontalAlignment(),
				placement.verticalAlignment()
			);

			assertThat(result.left()).describedAs("left").isEqualTo(outer.left()+placement.leftOffet());
			assertThat(result.bottom()).describedAs("bottom").isEqualTo(outer.bottom()+placement.bottomOffset());
		}

		@Test
		void rowAt() {
			assertThat(outer.rowAt(10.0f, VerticalAlignment.BOTTOM))
				.isEqualTo(new PageBox(outer.left(), outer.bottom(), outer.width(), 10.0f));
			assertThat(outer.rowAt(10.0f, VerticalAlignment.MIDDLE))
				.isEqualTo(new PageBox(outer.left(), outer.bottom()+(outer.height()-10.0f)/2.0f, outer.width(), 10.0f));
			assertThat(outer.rowAt(10.0f, VerticalAlignment.TOP))
				.isEqualTo(new PageBox(outer.left(), outer.bottom()+ outer.height()-10.0f, outer.width(), 10.0f));
		}

		@Test
		void columnAt() {
			assertThat(outer.columnAt(10.0f, HorizontalAlignment.LEFT))
				.isEqualTo(new PageBox(outer.left(), outer.bottom(), 10.0f, outer.height()));
			assertThat(outer.columnAt(10.0f, HorizontalAlignment.CENTER))
				.isEqualTo(new PageBox(outer.left()+(outer.width()-10.0f)/2.0f, outer.bottom(), 10.0f, outer.height()));
			assertThat(outer.columnAt(10.0f, HorizontalAlignment.RIGHT))
				.isEqualTo(new PageBox(outer.left()+outer.width()-10.0f, outer.bottom(), 10.0f, outer.height()));
		}
	}

}