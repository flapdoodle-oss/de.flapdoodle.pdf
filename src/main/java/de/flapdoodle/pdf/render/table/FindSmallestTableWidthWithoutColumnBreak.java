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
import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Floats;

/**
 * @see FindSmallestTableWidthFromRendering
 */
@Deprecated
public class FindSmallestTableWidthWithoutColumnBreak implements MinimalTableWidth {
	private static final int MAX_LOOPS = 100;

	@Override
	public RenderedTableDimension of(TableRenderer renderer, Table table, float startingWidth) {
		TableRenderer.Result firstResult = MinimalTableWidth.renderTable(renderer, table, startingWidth);
		var height = firstResult.tableHeight();

		var lowerBound = 0f;
		var middle = startingWidth;
		var upperBound = PageSize.A0.getWidth()*1000;

		var loops = 1;
		do {
			loops++;

			// check if table must be bigger
			var bigger = (middle + upperBound) / 2.f;
			var bigger_height = MinimalTableWidth.tableHeight(renderer, table, bigger);
			if (bigger_height < height) {
				// must be bigger
				lowerBound = middle;
				middle = bigger;
				height = bigger_height;
			} else {
				upperBound = bigger;
				// check if table can be smaller
				var smaller = (middle + lowerBound) / 2.f;
				var smaller_height = MinimalTableWidth.tableHeight(renderer, table, smaller);
				if (smaller_height <= height) {
					// yes, can be smaller, because height does not increase
					upperBound = middle;
					middle = smaller;
					height = smaller_height;
				} else {
					lowerBound = smaller;
				}
			}

		} while (!Floats.isNearBy(lowerBound, upperBound) && loops < MAX_LOOPS);

		Preconditions.checkArgument(loops < 100, "exceeded maximum number of loops (%s - %s - %s)", lowerBound, middle, upperBound);

		TableRenderer.Result result = MinimalTableWidth.renderTable(renderer, table, middle);

		return new RenderedTableDimension(
			middle, result.tableHeight(),
			result.columnWidths(), result.rowHeights()
		);
	}
}
