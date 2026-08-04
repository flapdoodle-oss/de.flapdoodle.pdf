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

import de.flapdoodle.commons.checks.Preconditions;
import org.openpdf.text.PageSize;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.FloatBisect;

public class FindSmallestTableWidthFromRendering implements MinimalTableWidth {
	private static final int MAX_LOOPS = 100;

	@Override
	public RenderedTableDimension of(TableRenderer renderer, Table table, float startingWidth) {
		TableRenderer.Result firstResult = MinimalTableWidth.renderTable(renderer, table, PageSize.A0.getWidth()*1000);

		var height = firstResult.tableHeight(); // PageSize.A0.getWidth()*1000;
		var bounds = new FloatBisect(0f, startingWidth, PageSize.A0.getWidth()*1000);

		TableRenderer.Result newResult;

		var loops = 1;
		do {
			loops++;

			newResult = MinimalTableWidth.renderTable(renderer, table, bounds.middle());
			var new_height = newResult.tableHeight();
			if (new_height == height) {
				// can go smaller
				height=new_height;
				bounds = bounds.lowerHalf();
			} else {
				// must be wider
				bounds=bounds.upperHalf();
			}
		} while (!bounds.isCloseEnough() && loops < MAX_LOOPS);

		Preconditions.checkArgument(loops < 100, "exceeded maximum number of loops (%s)", bounds);

		// TODO kann man sich vielleicht sparen
		TableRenderer.Result result = MinimalTableWidth.renderTable(renderer, table, bounds.middle());

		return new RenderedTableDimension(
			bounds.middle(), result.tableHeight(),
			result.columnWidths(), result.rowHeights()
		);

	}
}
