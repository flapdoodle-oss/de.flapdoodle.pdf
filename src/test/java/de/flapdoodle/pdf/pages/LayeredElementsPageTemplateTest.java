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

import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.NewPage;
import de.flapdoodle.pdf.blocks.Section;
import de.flapdoodle.pdf.elements.PdfPCellFactory;
import de.flapdoodle.pdf.elements.TableElement;
import de.flapdoodle.pdf.howto.PageBorders;
import de.flapdoodle.pdf.tables.cells.BorderStyle;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.Dimension;
import org.junit.jupiter.api.Test;
import org.openpdf.text.PageSize;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.ColumnText;

import java.util.Optional;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LayeredElementsPageTemplateTest {

	@Test
	void renderElements() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(
				Section.builder().title("First Page").build(),
				new NewPage(),
				Section.builder().title("Second Page").build(),
				new NewPage()
			)
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addOnPageEvents(LayeredElementsPageTemplate.builder()
				.dimension(doc -> new Dimension(200f, 20f))
				.boxFactory(doc -> PageBox.fullPageBox(doc)
					.rowAt(20f, VerticalAlignment.BOTTOM))
				.templateElementFactory(pages -> {
					return TableElement.builder()
						.columns(TableElement.Columns.count(2))
						.addCells(PdfPCellFactory.builder()
							.build())
						.addCells(PdfPCellFactory.builder()
							.cellHeight(new PdfPCellFactory.CellHeight.FixedHeight(19.9f))
							.cellStyle(CellStyle.empty()
								.withHorizontalAlignment(HorizontalAlignment.LEFT)
								.withVerticalAlignment(VerticalAlignment.MIDDLE)
								.withBorder(BorderStyle.of(1f))
								.withPadding(BorderProperty.of(0f)))
							.phrase(() -> Phrase.getInstance("/"+pages))
							.build())
						.widthPercentage(99.9f)
						.build()
						.create();
				})
				.pageElementFactory(page -> {
					return Optional.of(TableElement.builder()
						.columns(TableElement.Columns.count(2))
						.addCells(PdfPCellFactory.builder()
							.cellHeight(new PdfPCellFactory.CellHeight.FixedHeight(19.9f))
							.cellStyle(CellStyle.empty()
								.withHorizontalAlignment(HorizontalAlignment.RIGHT)
								.withVerticalAlignment(VerticalAlignment.MIDDLE)
								.withBorder(BorderStyle.of(1f))
								.withPadding(BorderProperty.of(0f)))
							.phrase(() -> Phrase.getInstance("Page "+page))
							.build())
						.addCells(PdfPCellFactory.builder().build())
						.widthPercentage(99.9f)
						.build()
						.create());
				})
				.build()
				.asPageEvent())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "layeredPageTemplate.pdf");
	}

}