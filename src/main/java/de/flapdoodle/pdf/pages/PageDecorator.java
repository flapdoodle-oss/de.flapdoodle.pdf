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

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import de.flapdoodle.pdf.columns.ColumnFactory;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.function.Function;

@Value.Immutable
public abstract class PageDecorator extends EnhancedPdfPageEventHelper {
	protected abstract Function<Document, PageBox> boxFactory	();
	protected abstract ElementFactory	elementFactory	();
	@Value.Default
	protected Function<PdfWriter, PdfContentByte> contentFactory() {
		return PdfWriter::getDirectContentUnder;
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		Optional<Element> element = elementFactory().forPage(document.getPageNumber());
		if (element.isPresent()) {
			PdfContentByte content = contentFactory().apply(writer);

			var column = ColumnFactory.DEFAULT.create(
				content,
				boxFactory().apply(document)
			);

			column.addElement(element.get());
			int result = column.go();
			
			// TODO muss ich damit was machen?
			switch (result) {
				case ColumnText.NO_MORE_TEXT -> System.out.println("fine...");
				case ColumnText.NO_MORE_COLUMN ->  System.out.println("cutted.. ");
			}
		}
	}

	public interface ElementFactory {
		Optional<Element> forPage(int page);
	}

	public static ImmutablePageDecorator.Builder builder() {
		return ImmutablePageDecorator.builder();
	}
}
