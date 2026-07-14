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

import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.Section;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;

import java.io.IOException;

public class BlocksTest {
	public static final String someText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

	@RegisterExtension
	public static Recording recording = Recorder.with("Blocks.md", TabSize.spaces(2));

	@Test
	public void title() {
		recording.begin();
		Section title = Section.builder()
			.title("Title")
			.font(new Font(Font.HELVETICA, 24, Font.BOLD))
			.build();
		recording.end();

		byte[] content = render(title);
		recording.file("pdf", "block-title.pdf", content);
		recording.file("png", "block-title.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}

	@Test
	public void text() throws IOException {
		recording.begin();
		Text text = new Text(someText);
		recording.end();

		byte[] content = render(text);
		recording.file("pdf", "block-text.pdf", content);
		recording.file("png", "block-text.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}

	@Test
	public void titleNewPage() throws IOException {
		recording.begin();
		Text text = new Text(someText);
		Section title = Section.builder()
			.title("Title")
			.font(new Font(Font.HELVETICA, 24, Font.BOLD))
			.build();
		Section withSpace = Section.builder()
			.title("... need more Space:o")
			.font(new Font(Font.HELVETICA, 24, Font.BOLD))
			.minPageHeightLeft(PageSize.A4.getHeight()*0.66f)
			.build();
		recording.end();

		byte[] content = render(text, title, withSpace);
		recording.file("pdf", "block-title-newpage.pdf", content);
		recording.file("png-0", "block-title-newpage-0.png", PdfImageGenerator.renderPageAsPng(content, 0));
		recording.file("png-1", "block-title-newpage-1.png", PdfImageGenerator.renderPageAsPng(content, 1));
	}

	private byte[] render(Block ... blocks) {
		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addBlocks(blocks)
			.build();

		return IO.withOutputStream(factory::render);

	}
}
