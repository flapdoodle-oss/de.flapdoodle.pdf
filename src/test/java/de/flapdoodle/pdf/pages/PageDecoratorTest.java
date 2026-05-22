package de.flapdoodle.pdf.pages;

import com.lowagie.text.*;
import com.lowagie.text.Image;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.Dimension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class PageDecoratorTest {
	@Test
	void placeImageIntoBackgroundCorner() throws IOException {
		URL resource = getClass().getResource("background.png");
		Image image = Image.getInstance(Objects.requireNonNull(resource,"image resource not found"));

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new Text("text"))
			.addOnPageEvents(PageDecorator.builder()
				.elementFactory(page -> Optional.of(image))
				.boxFactory(doc -> {
					PageBox pageBox = PageBox.fullPageBox(doc);
					return pageBox.boxAt(new Dimension(100.0f, 100.f), HorizontalAlignment.LEFT, VerticalAlignment.TOP);
				})
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "backgroundImagePlacement.pdf");
	}

}