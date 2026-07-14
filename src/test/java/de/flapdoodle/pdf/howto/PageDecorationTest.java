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
package de.flapdoodle.pdf.howto;

import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.NewPage;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.elements.PdfPCellFactory;
import de.flapdoodle.pdf.elements.PhraseElement;
import de.flapdoodle.pdf.elements.TableElement;
import de.flapdoodle.pdf.pages.*;
import de.flapdoodle.pdf.tables.cells.*;
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.Dimension;
import de.flapdoodle.pdf.types.ImmutableBorderProperty;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfPageEventHelper;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class PageDecorationTest {
	@RegisterExtension
	public static Recording recording = Recorder.with("PageDecoration.md", TabSize.spaces(2));

	@Test
	void backgroundImage() throws IOException {
		URL resource = getClass().getResource("background.png");
		Image image = Image.getInstance(Objects.requireNonNull(resource,"image resource not found"));

		recording.begin("factory");
		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new Text("Background Image"))
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addOnPageEvents(PageDecorator.builder()
				.elementFactory(pageNumber -> Optional.of(image))
				.boxFactory(doc -> PageBox.fullPageBox(doc)
					.boxAt(new Dimension(50.0f, 50.0f), HorizontalAlignment.LEFT, VerticalAlignment.TOP))
				.build())
			.build();
		recording.end();

		byte[] content = IO.withOutputStream(factory::render);

		recording.file("pdf", "page-backgroundImage.pdf", content);
		recording.file("png", "page-backgroundImage.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}

	@Test
	void directContent() throws IOException {
		recording.begin("factory");
		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new Text("Direct Content"))
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addOnPageEvents(PageDirectContentDecorator.builder()
				.renderer((doc, cb) -> {
					PageBox pageBox = PageBox.innerBox(doc);
					float left = pageBox.left();
					float right = pageBox.left() + pageBox.width() / 10f;
					float bottom = pageBox.bottom();
					float top = pageBox.bottom() + pageBox.height() / 10f;

					cb.setLineWidth(1f);
					cb.setRGBColorFill(128,0,0);
					cb.moveTo(left, bottom);
					cb.lineTo(left, top);
					cb.lineTo(right, bottom);
					cb.lineTo(left, bottom);
					cb.fill();
					cb.resetRGBColorFill();
				})
				.build())
			.build();
		recording.end();

		byte[] content = IO.withOutputStream(factory::render);

		recording.file("pdf", "page-direct-content.pdf", content);
		recording.file("png", "page-direct-content.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}

	@Test
	void pageHeaderAndFooter() {
		recording.begin("factory");
		PageDecorator header = PageDecorator.builder()
			.elementFactory(page -> Optional.of(TableElement.builder()
				.columns(TableElement.Columns.relativeWeights(1.0f))
				.addCells(PdfPCellFactory.builder()
					.phrase(PhraseElement.of("Page "+page))
					.cellStyle(CellStyle.empty()
						.withHorizontalAlignment(HorizontalAlignment.CENTER)
						.withBorder(BorderProperty.<BorderStyle>of(BorderStyle.noBorder())
							.withBottom(BorderStyle.of(Color.BLACK, 0.5f))))
					.build())
				.build().create()))
			.boxFactory(document -> PageBox.fullPageBox(document)
				.rowAt(20.0f, VerticalAlignment.TOP))
			.build();

		PageDecorator footer = PageDecorator.builder()
			.elementFactory(page -> Optional.of(TableElement.builder()
				.columns(TableElement.Columns.relativeWeights(1.0f))
				.addCells(PdfPCellFactory.builder()
					.phrase(PhraseElement.of("-- Page "+page+" --"))
					.cellStyle(CellStyle.empty()
						.withHorizontalAlignment(HorizontalAlignment.CENTER)
						.withBorder(BorderProperty.<BorderStyle>of(BorderStyle.noBorder())
							.withTop(BorderStyle.of(Color.BLACK, 0.5f))))
					.build())
				.build().create()))
			.boxFactory(document -> PageBox.fullPageBox(document)
				.rowAt(20.0f, VerticalAlignment.BOTTOM))
			.build();

		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addBlocks(new Text("Header and Footer"))
			.addOnPageEvents(header, footer)
			.build();
		recording.end();

		byte[] content = IO.withOutputStream(factory::render);

		recording.file("pdf", "page-headerRows.pdf", content);
		recording.file("png", "page-headerRows.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}

	@Test
	void layeredPageHeader() {
		recording.begin("factory");
		BorderProperty<BorderStyle> cellBorder = BorderProperty.<BorderStyle>of(BorderStyle.noBorder())
			.withBottom(BorderStyle.of(Color.BLACK, 0.5f));

		CellStyle leftCellStyle = CellStyle.empty()
			.withHorizontalAlignment(HorizontalAlignment.RIGHT)
			.withPadding(BorderProperty.of(4f).withRight(0f))
			.withBorder(cellBorder);

		CellStyle rightCellStyle = CellStyle.empty()
			.withHorizontalAlignment(HorizontalAlignment.LEFT)
			.withPadding(BorderProperty.of(4f).withLeft(0f))
			.withBorder(cellBorder);

		PdfPageEventHelper header = LayeredElementsPageTemplate.builder()
			.pageElementFactory(page -> Optional.of(TableElement.builder()
				.columns(TableElement.Columns.count(2))
				.addCells(PdfPCellFactory.builder()
					.phrase(PhraseElement.of("Page " + page))
					.cellStyle(leftCellStyle)
					.build())
				.addCells(PdfPCellFactory.builder()
					.cellStyle(rightCellStyle)
					.build())
				.build()
				.create()))
			.templateElementFactory(pages -> TableElement.builder()
				.columns(TableElement.Columns.count(2))
				.addCells(PdfPCellFactory.builder()
					.cellStyle(leftCellStyle)
					.build())
				.addCells(PdfPCellFactory.builder()
					.phrase(PhraseElement.of("/" + pages))
					.cellStyle(rightCellStyle)
					.build())
				.build()
				.create())
			.boxFactory(document -> PageBox.fullPageBox(document)
				.rowAt(20f, VerticalAlignment.TOP))
			.dimension(doc -> PageBox.fullPageBox(doc)
				.rowAt(20f, VerticalAlignment.TOP)
				.dimension())
			.build()
			.asPageEvent();

		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addBlocks(new Text("First Page"), new NewPage(), new Text("Second Page"))
			.addOnPageEvents(header)
			.build();
		recording.end();

		byte[] content = IO.withOutputStream(factory::render);

		recording.file("pdf", "page-layered-headerRows.pdf", content);
		recording.file("png-0", "page-layered-headerRows-0.png", PdfImageGenerator.renderPageAsPng(content, 0));
		recording.file("png-1", "page-layered-headerRows-1.png", PdfImageGenerator.renderPageAsPng(content, 1));
	}
}
