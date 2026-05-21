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

import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Dimension;
import de.flapdoodle.pdf.types.Floats;
import de.flapdoodle.pdf.types.Region;

public interface TableRenderer {
	/**
	 * don't commit rendering to column from inside
	 */
	Result render(Table table, Region region, PageBox pageBox);

	record Result(
		int lastVisibleRow,
		float tableHeight, // can contain invisible parts bc of clipping
		Runnable commit
	) {	}

	default float minimalWidthOf(Table table, float startingWidth) {
		var minInitialHeight = tableHeight(table, startingWidth);

		var startRange = 0f;
		var endRange = startingWidth;

		var loops = 1;
		do {
			loops++;
			var testWidth = startRange + (endRange - startRange) / 2;
//      println("width($loops) -> $testWidth")
			var height = tableHeight(table, testWidth);
			if (height > minInitialHeight) {
				// to small
				startRange = testWidth;
			} else {
				// not small enought
				endRange = testWidth;
			}
		} while (!Floats.isNearBy(startRange, endRange) && loops < 100);

		return endRange;
	}

	private float tableHeight(Table table, float width) {
		return render(table, table.maxRegion(), new PageBox(PagePosition.ZERO, new Dimension(width, 100f))).tableHeight;
	}

}
