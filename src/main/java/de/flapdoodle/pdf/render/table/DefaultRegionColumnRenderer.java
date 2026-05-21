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

import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import de.flapdoodle.pdf.render.column.ColumnTexts;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Region;

import java.util.Optional;

public class DefaultRegionColumnRenderer implements RegionColumnRenderer {
	private static final Font DEFAULT_FONT = new Font();

	@Override
	public Status render(ColumnText column, Table table, Region region) {
		var numberOfColumns = region.columns().count();
		var lastRenderedRow = 0;

		var pdfTable = new PdfPTable(numberOfColumns);
		pdfTable.setWidths(region.columns()
			.mapToFloat(it -> table.columnWeights().get(it).orElse(1f)));

		var headerDefaultCell = PdfPCells.clone(pdfTable.getDefaultCell());

		var tableHeader = table.header();

		if (tableHeader.isPresent()) {
			for (int c: region.columns().asRange()) {
				var cellStyle = tableHeader.get().styles().get(c);
				var value = tableHeader.get().get(c);

				addCell(pdfTable, headerDefaultCell, cellStyle, value);
			}
			pdfTable.setHeaderRows(1);
		}


		pdfTable.setWidthPercentage(100f);
		pdfTable.setSpacingAfter(0f);
		pdfTable.setSpacingBefore(0f);

		column.addElement(pdfTable);

		var bodyDefaultCell = PdfPCells.clone(pdfTable.getDefaultCell());

		for (int r : region.rows().asRange()) {
			for (int c : region.columns().asRange()) {
				var cell = new Cell(c, r);
				var cellStyle = table.styles().get(cell);
				var value = table.get(cell);

				addCell(pdfTable, bodyDefaultCell, cellStyle, value);
			}
			if (ColumnTexts.stillSpaceLeft(column)) {
				lastRenderedRow = r;
			}
		}

		return new RegionColumnRenderer.Status(lastRenderedRow, pdfTable.calculateHeights(false));
	}

	private void addCell(
		PdfPTable pdfTable,
		PdfPCell baseTableCell,
		CellStyle cellStyle,
		Optional<String> value) {
		PdfPCell cell = PdfPCells.clone(baseTableCell);
		PdfPCells.applyStyle(cell, cellStyle);

		pdfTable.addCell(
			PdfPCells.with(cell, new Phrase(value.orElse(null), cellStyle.font().orElse(DEFAULT_FONT)))
    );
	}
}
