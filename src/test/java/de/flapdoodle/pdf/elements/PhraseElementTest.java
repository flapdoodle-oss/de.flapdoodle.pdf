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
import de.flapdoodle.pdf.DocumentFactoryAssert;
import de.flapdoodle.pdf.pages.TagDecorator;
import org.junit.jupiter.api.Test;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;

import java.awt.*;

class PhraseElementTest {

	@Test
	void customPhrase() {
		Font big = new Font(Font.HELVETICA, 12, Font.BOLD);
		Font base = new Font(Font.HELVETICA, 8, Font.BOLD);
		Font red = new Font(Font.COURIER, 10, Font.BOLD, Color.RED);
		Font green = new Font(Font.TIMES_ROMAN, 12, Font.BOLD, Color.GREEN);

		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.addOnPageEvents(TagDecorator.builder()
					.onGenericTag((writer, document, content, rectangle, text) -> {
						switch (text) {
							case "A" -> {
								content.setColorStroke(Color.RED);
								content.setLineWidth(1f);
								content.rectangle(rectangle.getLeft(), rectangle.getBottom() - 2f, rectangle.getWidth(), rectangle.getHeight() + 4f);
								content.stroke();
							}
							case "B" -> {
								content.setColorStroke(Color.GREEN);
								content.setLineWidth(1f);
								content.rectangle(rectangle.getLeft(), rectangle.getBottom() - 2f, rectangle.getWidth(), rectangle.getHeight() + 4f);
								content.stroke();
							}
						}
					})
					.build())
				.addBlocks((document, directContent) -> {
					document.add(PhraseElement.of("ohne").create());
					document.add(PhraseElement.of("mit", big).create());
					document.add(PhraseElement.builder()
						.font(base)
						.addParts(new PhraseElement.Part.Text("ohne"))
						.addParts(new PhraseElement.Part.Tag("rot(tag=A)", "A", red))
						.addParts(new PhraseElement.Part.Tag("rot(tag=B)", "B", red))
						.addParts(new PhraseElement.Part.Text("grün", green))
						.build().create());
				})
				.build())
			.expectRendering()
			.matchesResource(getClass(), "customPhrase.pdf");

	}
}