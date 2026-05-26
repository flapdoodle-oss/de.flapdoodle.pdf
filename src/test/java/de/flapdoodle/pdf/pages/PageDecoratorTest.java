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