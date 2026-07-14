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
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.elements.PdfPCellFactory;
import de.flapdoodle.pdf.elements.TableElement;
import de.flapdoodle.pdf.howto.PageBorders;
import de.flapdoodle.pdf.render.column.ColumnTexts;
import de.flapdoodle.pdf.tables.cells.BorderStyle;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import org.junit.jupiter.api.Test;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;

import javax.security.auth.Subject;
import java.io.IOException;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PageTemplateTest {

	@Test
	void renderPageNumbers() throws IOException {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(
				Section.builder().title("First Page").build(),
				new NewPage(),
				Section.builder().title("Second Page").build(),
				new NewPage()
			)
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addOnPageEvents(PageTemplate.builder()
				.pageTemplateFactory(((document, content) -> {
					return content.createTemplate(0f, 0f);
				}))
				.pageTemplateDecorator((document, content, template) -> {
					content.addTemplate(template, 10f, 10f);
					content.addTemplate(template, 210f, 210f);
				})
				.pageTemplateRenderer(((writer, document, template) -> {
					template.setBoundingBox(new Rectangle(0f, 0f, 200f, 200f));

					BaseFont bf = null;
					try {
						bf = BaseFont.createFont();
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}

					template.beginText();
					template.setFontAndSize(bf, 12);
					template.showTextAlignedKerned(Element.ALIGN_CENTER,"Pages: "+(writer.getPageNumber() - 1), 100,100, 0);
					template.endText();

					template.rectangle(0.001f, 0.01f, template.getWidth()-0.01f, template.getHeight()-0.01f);
					template.stroke();
				}))
				.build()
				.asPageEvent())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "pageTemplate.pdf");
	}

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
			.addOnPageEvents(PageTemplate.builder()
				.pageTemplateFactory(((document, content) -> {
					return content.createTemplate(0f, 0f);
				}))
				.pageTemplateDecorator((document, content, template) -> {
					content.addTemplate(template, 10f, 10f);
					content.addTemplate(template, 210f, 210f);
				})
				.pageTemplateRenderer(((writer, document, template) -> {
					template.setBoundingBox(new Rectangle(0f, 0f, 200f, 200f));

					ColumnText columnText = new ColumnText(template);
					columnText.setSimpleColumn(0,0,200f, 200f);
					columnText.addElement(TableElement.builder()
							.columns(TableElement.Columns.count(1))
							.addCells(PdfPCellFactory.builder()
								.cellHeight(new PdfPCellFactory.CellHeight.FixedHeight(199f))
								.cellStyle(CellStyle.empty()
									.withHorizontalAlignment(HorizontalAlignment.CENTER)
									.withVerticalAlignment(VerticalAlignment.MIDDLE)
									.withBorder(BorderStyle.of(1f)))
								.phrase(() -> Phrase.getInstance("Pages: "+(writer.getPageNumber()-1)))
								.build())
							.widthPercentage(99.9f)
						.build()
						.create());

					columnText.go();
				}))
				.build()
				.asPageEvent())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "pageTemplate-elements.pdf");
	}
}