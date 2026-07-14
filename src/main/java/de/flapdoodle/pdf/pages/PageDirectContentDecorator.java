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

import org.openpdf.text.Document;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;
import org.immutables.value.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Value.Immutable
public abstract class PageDirectContentDecorator extends EnhancedPdfPageEventHelper {

	@Value.Default
	protected boolean decorateOnEndPage() {
		return true;
	}

	@Value.Default
	protected Function<PdfWriter, PdfContentByte> contentFactory() {
		return PdfWriter::getDirectContentUnder;
	}

	protected abstract BiConsumer<Document, PdfContentByte> renderer();

	@Override
	public void onStartPage(PdfWriter writer, Document document) {
		if (!decorateOnEndPage()) {
			decorate(writer, document);
		}
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		if (decorateOnEndPage()) {
			decorate(writer, document);
		}
	}

	private void decorate(PdfWriter writer, Document document) {
		PdfContentByte content = contentFactory().apply(writer);

		renderer().accept(document, content);
	}

	public static ImmutablePageDirectContentDecorator.Builder builder() {
		return ImmutablePageDirectContentDecorator.builder();
	}
}
