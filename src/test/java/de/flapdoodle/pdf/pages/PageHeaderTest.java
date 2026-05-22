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

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridCellDecorator;
import de.flapdoodle.pdf.grid.GridContent;
import de.flapdoodle.pdf.grid.GridRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class PageHeaderTest {
	@Test
	@DisplayName("render header")
	void renderHeader() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new FirstBlockAfterHeader())
			.addOnPageEvents(PageParagraphDecorators.builder()
				.headerFactory(new PageHeader("page header", Optional.of(
					new Font(Font.HELVETICA, 12f, Font.NORMAL, Color.BLACK)
				)))
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "headerPdf.pdf");
	}

	static class FirstBlockAfterHeader implements Block {
		@Override
		public void render(Document document, Supplier<PdfContentByte> directContent) {
			var innerBox = PageBox.innerBox(document);
			var grid = new Grid(innerBox.width(), innerBox.height());
			GridRenderer.<String>builder()
				.renderBoxDecorator(GridCellDecorator.renderBorder(Color.GRAY))
				.build()
				.render(document, directContent, grid, GridContent.fromMap(Map.of()), (columnText, s) -> {

				});
		}
	}

}