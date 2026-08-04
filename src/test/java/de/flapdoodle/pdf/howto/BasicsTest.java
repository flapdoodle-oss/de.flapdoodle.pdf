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

import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.Meta;
import de.flapdoodle.pdf.PdfImageGenerator;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.commons.testdoc.Recorder;
import de.flapdoodle.commons.testdoc.Recording;
import de.flapdoodle.commons.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class BasicsTest {
	@RegisterExtension
	public static Recording recording = Recorder.with("Basics.md", TabSize.spaces(2));

	@Test
	public void helloWorld() throws IOException {
		recording.begin("factory");
		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(new Text("hello world!"))
			.build();
		recording.end();

		byte[] content = IO.withOutputStream(out -> {
			recording.beginInLambda("render");
			factory.render(out);
			recording.endInLambda();
		});

		recording.file("pdf", "hello-world.pdf", content);
		recording.file("png", "hello-world.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}

	@Test
	public void metaData() {
		ZonedDateTime creationDate = ZonedDateTime.of(
			LocalDate.of(2026, 3, 12),
			LocalTime.of(13, 0),
			ZoneId.systemDefault()
		);

		recording.begin("factory");
		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.meta(Meta.empty()
				.withCreationDate(creationDate)
				.withTitle("MetaData Sample")
				.withSubject("this is how we do it")
				.withAuthor("Its me:)")
				.withCreator("flapdoodle test")
				.withProducer("OpenPDF"))
			.addBlocks(new Text("created at "+creationDate))
			.build();
		recording.end();

		byte[] content = IO.withOutputStream(factory::render);

		recording.file("pdf", "meta-data.pdf", content);
		recording.file("png", "meta-data.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}
}
