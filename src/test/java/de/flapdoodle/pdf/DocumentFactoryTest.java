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
package de.flapdoodle.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class DocumentFactoryTest {
	@Test
	@DisplayName("render empty pdf")
	void renderEmptyPdf() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.fileName("emptyPage.pdf")
			.addBlocks(new DummyBlock())
			.build())
			.expectRendering()
			.matchesResource(this.getClass(), "emptyPdf.pdf");
	}
	
	class DummyBlock implements Block {
		@Override
		public void render(Document document, Supplier<PdfContentByte> directContent) {
			document.add(new Phrase("test"));
		}
	}

}