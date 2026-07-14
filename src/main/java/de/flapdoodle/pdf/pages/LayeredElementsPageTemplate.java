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

import de.flapdoodle.pdf.columns.ColumnFactory;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.Dimension;
import org.immutables.value.Value;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.*;

import java.util.Optional;
import java.util.function.Function;

@Value.Immutable
public abstract class LayeredElementsPageTemplate {

	@Value.Default
	protected Function<PdfWriter, PdfContentByte> contentFactory() {
		return PdfWriter::getDirectContentUnder;
	}

	@Value.Default
	protected boolean decorateOnEndPage() {
		return true;
	}

	protected abstract Function<Document, Dimension> dimension();

	protected abstract Function<Document, PageBox> boxFactory();

	@Value.Default
	protected HorizontalAlignment horizontalAlignment() {
		return HorizontalAlignment.CENTER;
	}

	@Value.Default
	protected VerticalAlignment verticalAlignment() {
		return VerticalAlignment.MIDDLE;
	}

	protected abstract PageElementFactory pageElementFactory();

	protected abstract TemplateElementFactory templateElementFactory();

	@Value.Auxiliary
	public PdfPageEventHelper asPageEvent() {
		return new PdfPageEventHelper() {
			private PdfTemplate template;

			@Override
			public void onOpenDocument(PdfWriter writer, Document document) {
				PdfContentByte content = contentFactory().apply(writer);
				template = content.createTemplate(0f, 0f);
			}

			@Override
			public void onStartPage(PdfWriter writer, Document document) {
				if (!decorateOnEndPage()) {
					decorate(writer, document, contentFactory().apply(writer));
				}
			}

			@Override
			public void onEndPage(PdfWriter writer, Document document) {
				if (decorateOnEndPage()) {
					decorate(writer, document, contentFactory().apply(writer));
				}
			}

			private void decorate(PdfWriter writer, Document document, PdfContentByte content) {
				Optional<Element> pageElement = pageElementFactory().forPage(document.getPageNumber());
				pageElement.ifPresent(element -> {
					PageBox pageBox = boxFactory().apply(document);
					PageBox pageElementBox = pageBox.boxAt(dimension().apply(document), horizontalAlignment(), verticalAlignment());

					content.addTemplate(template, pageElementBox.left(), pageElementBox.bottom());

					ColumnText columnText = ColumnFactory.DEFAULT.create(content, pageElementBox);
					columnText.addElement(element);
					columnText.go();
				});
			}

			@Override
			public void onCloseDocument(PdfWriter writer, Document document) {
				PageBox boundingBox = dimension().apply(document).pageBoxAt(PagePosition.ZERO);

				template.setBoundingBox(boundingBox.asRectangle());

				ColumnText columnText = ColumnFactory.DEFAULT.create(template, boundingBox);
				Element element = templateElementFactory().forPages(writer.getPageNumber() - 1);
				columnText.addElement(element);
				columnText.go();
			}
		};
	}

	public interface PageElementFactory {
		Optional<Element> forPage(int page);
	}

	public interface TemplateElementFactory {
		Element forPages(int pages);
	}

	public static ImmutableLayeredElementsPageTemplate.Builder builder() {
		return ImmutableLayeredElementsPageTemplate.builder();
	}
}
