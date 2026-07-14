package de.flapdoodle.pdf.pages;

import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.howto.PageBorders;
import de.flapdoodle.pdf.render.Documents;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.Dimension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PageDirectContentDecoratorTest {
	@Test
	void renderShapeOntoPage() throws IOException {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new Text("text"))
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addOnPageEvents(PageDirectContentDecorator.builder()
				.renderer((doc, cb) -> {
					PageBox pageBox = PageBox.innerBox(doc);
					cb.setLineWidth(1f);
					cb.setRGBColorFill(128,0,0);
					cb.moveTo(pageBox.left(), pageBox.bottom());
					cb.lineTo(pageBox.left(), pageBox.bottom() + pageBox.height());
					cb.lineTo(pageBox.left() + pageBox.width(), pageBox.bottom());
					cb.lineTo(pageBox.left(), pageBox.bottom());
					cb.fill();
					cb.resetRGBColorFill();
				})
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "backgroundShape.pdf");
	}

}