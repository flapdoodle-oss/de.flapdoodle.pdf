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
import org.openpdf.text.PageSize;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Dimension;

@FunctionalInterface
public interface MinimalTableWidth {
	RenderedTableDimension of(TableRenderer renderer, Table table, float startingWidth);

	@Deprecated
	static float tableHeight(TableRenderer renderer, Table table, float width) {
		return renderTable(renderer, table, width).tableHeight();
	}

	static TableRenderer.Result renderTable(TableRenderer renderer, Table table, float width) {
		TableRenderer.Result result = renderer.render(
			table,
			table.maxRegion(),
			new PageBox(PagePosition.ZERO, new Dimension(width, PageSize.A4.getHeight() * 100))
		);
		Preconditions.checkArgument(table.rows() == (result.lastVisibleRow() + 1),
			"table truncated at %s (%s)", result.lastVisibleRow(), table.rows());
		return result;
	}
}
