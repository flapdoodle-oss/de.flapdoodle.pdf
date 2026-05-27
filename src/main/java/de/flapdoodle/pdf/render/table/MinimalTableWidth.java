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
package de.flapdoodle.pdf.render.table;

import com.google.common.base.Preconditions;
import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Dimension;
import de.flapdoodle.pdf.types.Floats;

@FunctionalInterface
public interface MinimalTableWidth {
	float of(TableRenderer renderer, Table table, float startingWidth);

	class Default implements MinimalTableWidth {

		public static final int MAX_LOOPS = 100;

		@Override
		public float of(TableRenderer renderer, Table table, float startingWidth) {
			var minInitialHeight = tableHeight(renderer, table, startingWidth);

			var startRange = 0f;
			var endRange = startingWidth;

			var loops = 1;
			do {
				loops++;
				var testWidth = startRange + (endRange - startRange) / 2;
//      println("width($loops) -> $testWidth")
				var height = tableHeight(renderer, table, testWidth);
				if (height > minInitialHeight) {
					// to small
					startRange = testWidth;
				} else {
					// not small enought
					endRange = testWidth;
				}
			} while (!Floats.isNearBy(startRange, endRange) && loops < MAX_LOOPS);

			Preconditions.checkArgument(loops < 100,"exceeded maximum number of loops (%s ... %s)", startRange, endRange);

			return endRange;
		}

		private static float tableHeight(TableRenderer renderer, Table table, float width) {
			TableRenderer.Result result = renderer.render(
				table,
				table.maxRegion(),
				new PageBox(PagePosition.ZERO, new Dimension(width, PageSize.A4.getHeight()*100))
			);
			Preconditions.checkArgument(table.rows() == (result.lastVisibleRow() +1),
				"table truncated at %s (%s)", result.lastVisibleRow(), table.rows());
			
			return result.tableHeight();
		}
	}
}
