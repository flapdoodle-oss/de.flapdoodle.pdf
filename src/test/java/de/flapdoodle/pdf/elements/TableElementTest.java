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
package de.flapdoodle.pdf.elements;

import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.tables.cells.BorderStyle;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.types.BorderProperty;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Phrase;

import java.awt.*;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class TableElementTest {

	@Test
	void renderTable() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks((document, directContent) -> {
				document.add(TableElement.builder()
					.columns(TableElement.Columns.relativeWeights(1, 3))
					.addCells(PdfPCellFactory.builder()
						.cellStyle(CellStyle.noBorder()
							.withFont(new Font(Font.HELVETICA, 14, Font.BOLD)))
						.phrase(() -> Phrase.getInstance("Bold text"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.cellStyle(CellStyle.empty()
							.withFont(new Font(Font.HELVETICA, 4, Font.NORMAL)))
						.phrase(() -> Phrase.getInstance("Normal text"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.cellStyle(CellStyle.empty()
							.withPadding(BorderProperty.of(10f))
							.withBordeStyle(BorderProperty.<BorderStyle>builder()
								.left(BorderStyle.of(Color.RED, 1f))
								.top(BorderStyle.of(Color.BLUE, 0.1f))
								.right(BorderStyle.of(Color.GREEN, 2f))
								.bottom(BorderStyle.of(Color.BLACK, 1.5f))
								.build()))
						.phrase(PhraseElement.of("Borders:)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.cellStyle(CellStyle.empty()
							.withBackgroundColor(Color.LIGHT_GRAY))
						.build())
					.build()
					.create());
			})
			.build())
			.expectRendering()
			.matchesResource(getClass(), "tableElement.pdf");
	}

	@Test
	void renderTableWithColAndRowspan() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks((document, directContent) -> {
				document.add(TableElement.builder()
					.columns(TableElement.Columns.count(3))
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(0,0)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(0,1)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(0,2)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.colSpan(2)
						.phrase(() -> Phrase.getInstance("(1,0-1)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(1,2)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.rowSpan(2)
						.phrase(() -> Phrase.getInstance("(2-3,0)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(2,1)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(2,2)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(3,1)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(3,2)"))
						.build())
					.build()
					.create());
			})
			.build())
			.expectRendering()
			.matchesResource(getClass(), "tableElement-cellSpan.pdf");
	}

	@Test
	void brokenTableWithColAndRowspan() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks((document, directContent) -> {
				document.add(TableElement.builder()
					.columns(TableElement.Columns.count(3))
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(0,0)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(0,1)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(0,2)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(1,0)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.colSpan(3)
						.phrase(() -> Phrase.getInstance("(1,1-3)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.rowSpan(2)
						.phrase(() -> Phrase.getInstance("(2-3,0)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(2,1)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(2,2)"))
						.build())
					.addCells(PdfPCellFactory.builder()
						.phrase(() -> Phrase.getInstance("(3,1)"))
						.build())
					.build()
					.create());
			})
			.build())
			.expectRendering()
			.matchesResource(getClass(), "tableElement-broken.pdf");
	}
}