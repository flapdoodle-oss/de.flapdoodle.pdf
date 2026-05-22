package de.flapdoodle.pdf.howto;

import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.elements.PdfPCellFactory;
import de.flapdoodle.pdf.elements.PhraseElement;
import de.flapdoodle.pdf.elements.TableElement;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.pages.PageDecorator;
import de.flapdoodle.pdf.tables.cells.BorderStyle;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.Dimension;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

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
			.addBlocks(new Text("Header and Footer"))
			.addOnPageEvents(header, footer)
			.build();
		recording.end();

		byte[] content = IO.withOutputStream(out -> {
			factory.render(out);
		});

		recording.file("pdf", "page-header.pdf", content);
		recording.file("png", "page-header.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}
}
