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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPageEvent;
import com.lowagie.text.pdf.PdfWriter;
import de.flapdoodle.pdf.internals.PdfFileIdGenerator;
import de.flapdoodle.pdf.internals.StaticPdfFileIdGenerator;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.OnDocumentInit;
import org.immutables.value.Value;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class DocumentFactory {
	public static final Margin DEFAULT_PAGE_MARGINS = Margin.of(50f, 50f, 50f, 50f);

//	private val pageSize: Rectangle,
	protected abstract Rectangle pageSize();

	protected abstract List<Block> blocks();

	@Value.Default
	protected Margin pageMargin() { return DEFAULT_PAGE_MARGINS; }

	protected abstract Optional<PdfPageEvent> onPageEvents();
	protected abstract Optional<OnDocumentInit> onDocumentInit();
	
	@Value.Default
 	protected PdfFileIdGenerator pdfFileIdGenerator() { return new StaticPdfFileIdGenerator(); }


	@Value.Auxiliary
	public void render(OutputStream outputStream) {
		var document = new Document(pageSize(), pageMargin().left(), pageMargin().right(), pageMargin().top(), pageMargin().bottom());
		var writer = pdfWriter(document, outputStream);

//    val cal = Calendar.getInstance()
//    cal.set(2023, Calendar.JUNE, 15, 12, 0, 0)
//    writer.getInfo().put(PdfName.CREATIONDATE, PdfDate(cal))
		writer.getInfo().remove(PdfName.CREATIONDATE);
		writer.getInfo().remove(PdfName.PRODUCER);

		// TODO add metadata
		//document.addAuthor("Me")
		//document.addSubject("This is a test.")
		//document.setHeader()

		writer.setPageEvent(onPageEvents().orElse(null));

		if (onDocumentInit().isPresent()) {
			onDocumentInit().get().onInit(document);
		}

		document.open();

		blocks().forEach(it -> {
			it.render(document, writer::getDirectContent);
		});

		document.close();
	}

	private PdfWriter pdfWriter(Document document, OutputStream outputStream) {
		var writer = PdfWriter.getInstance(document, outputStream);
		writer.setFullCompression();
		writer.getInfo().put(PdfName.FILEID, pdfFileIdGenerator().generate());
		return writer;
	}



	public static ImmutableDocumentFactory.Builder builder() {
		return ImmutableDocumentFactory.builder();
	}
}
