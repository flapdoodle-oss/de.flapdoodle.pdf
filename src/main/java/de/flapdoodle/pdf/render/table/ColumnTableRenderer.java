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

import org.openpdf.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.columns.ColumnFactory;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Region;

public record ColumnTableRenderer(
	PdfContentByte directContent,
	RegionColumnRenderer regionColumnRenderer
) implements TableRenderer {

	@Override
	public Result render(Table table, TableAttributes attributes, Region region, PageBox pageBox) {
		var column = ColumnFactory.DEFAULT.create(directContent, pageBox);
		RegionColumnRenderer.Status render = regionColumnRenderer.render(column, table, attributes, region);
		return new TableRenderer.Result(
			render.lastVisibleRow(),
			render.tableWidth(),
			render.tableHeight(),
			render.columnWidths(),
			render.rowHeights(),
			column::go
		);
	}
}
