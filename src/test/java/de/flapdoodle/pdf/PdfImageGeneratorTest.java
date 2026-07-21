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

import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.howto.IO;
import org.junit.jupiter.api.Test;
import org.openpdf.text.PageSize;

import static org.assertj.core.api.Assertions.assertThat;

public class PdfImageGeneratorTest {

	@Test
	void renderImage2TimeShouldResultIntoSameBinary() {
		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addBlocks(new Text("test"))
			.build();

		byte[] pdfAsByteArray = IO.withOutputStream(factory::render);
		byte[] secondDocumentRender = IO.withOutputStream(factory::render);

		assertThat(pdfAsByteArray).isEqualTo(secondDocumentRender);

		byte[] firstRendering = PdfImageGenerator.renderPageAsPng(pdfAsByteArray, 0);
		byte[] secondRendering = PdfImageGenerator.renderPageAsPng(pdfAsByteArray, 0);

		assertThat(firstRendering).isEqualTo(secondRendering);
	}
}
