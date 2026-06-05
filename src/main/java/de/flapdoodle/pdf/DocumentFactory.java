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
import com.lowagie.text.pdf.*;
import de.flapdoodle.pdf.internals.PdfFileIdGenerator;
import de.flapdoodle.pdf.internals.StaticPdfFileIdGenerator;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.OnDocumentInit;
import de.flapdoodle.pdf.pages.PdfPageEvents;
import org.immutables.value.Value;

import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class DocumentFactory {
	public static final Margin DEFAULT_PAGE_MARGINS = Margin.of(50f, 50f, 50f, 50f);

//	private val pageSize: Rectangle,
	protected abstract Rectangle pageSize();

	protected abstract List<Block> blocks();

	@Value.Default
	protected Meta meta() {
		return Meta.empty();
	}

	@Value.Default
	protected Margin pageMargin() { return DEFAULT_PAGE_MARGINS; }

	protected abstract List<PdfPageEvent> onPageEvents();
	protected abstract Optional<OnDocumentInit> onDocumentInit();
	
	@Value.Default
 	protected PdfFileIdGenerator pdfFileIdGenerator() { return new StaticPdfFileIdGenerator(); }


	@Value.Auxiliary
	public void render(OutputStream outputStream) {
		var document = new Document(pageSize(), pageMargin().left(), pageMargin().right(), pageMargin().top(), pageMargin().bottom());
		var writer = pdfWriter(document, outputStream);

		PdfDictionary writerInfo = writer.getInfo();
		writerInfo.remove(PdfName.PRODUCER);

		// TODO somehow this line does not work
		// meta().creationDate().ifPresent(date -> document.addCreationDate(new PdfDate(GregorianCalendar.from(date))));
		// TODO and is replaced with that:
		meta().creationDate().ifPresentOrElse(
			date ->  writerInfo.put(PdfName.CREATIONDATE, new PdfDate(GregorianCalendar.from(date))),
			() -> writerInfo.remove(PdfName.CREATIONDATE)
		);


		meta().title().ifPresent(document::addTitle);
		meta().subject().ifPresent(document::addSubject);
		meta().author().ifPresent(document::addAuthor);
		meta().creator().ifPresent(document::addCreator);
		meta().producer().ifPresent(document::addProducer);

		if (!onPageEvents().isEmpty()) writer.setPageEvent(
			PdfPageEvents.all(onPageEvents())
		);

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
