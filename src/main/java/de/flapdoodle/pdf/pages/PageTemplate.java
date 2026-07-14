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

import org.immutables.value.Value;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.util.function.Function;

/**
 * raw page template helper, you should know what you are doing.
 */
@Value.Immutable
public abstract class PageTemplate {

	@Value.Default
	protected Function<PdfWriter, PdfContentByte> contentFactory() {
		return PdfWriter::getDirectContentUnder;
	}

	@Value.Default
	protected boolean decorateOnEndPage() {
		return true;
	}

	protected abstract PageTemplateFactory pageTemplateFactory();

	protected abstract PageTemplateDecorator pageTemplateDecorator();

	protected abstract PageTemplateRenderer pageTemplateRenderer();

	@FunctionalInterface
	public interface PageTemplateFactory {
		PdfTemplate createTemplate(Document document, PdfContentByte content);
	}

	public interface PageTemplateDecorator {
		void onPage(Document document, PdfContentByte content, PdfTemplate template);
	}

	@FunctionalInterface
	public interface PageTemplateRenderer {
		void render(PdfWriter writer, Document document, PdfTemplate template);
	}

	@Value.Auxiliary
	public PdfPageEventHelper asPageEvent() {
		return new PdfPageEventHelper() {
			private PdfTemplate template;

			@Override
			public void onOpenDocument(PdfWriter writer, Document document) {
				PdfContentByte content = contentFactory().apply(writer);
				template = pageTemplateFactory().createTemplate(document, content);
			}

			@Override
			public void onStartPage(PdfWriter writer, Document document) {
				if (!decorateOnEndPage()) {
					pageTemplateDecorator().onPage(document, contentFactory().apply(writer), template);
				}
			}

			@Override
			public void onEndPage(PdfWriter writer, Document document) {
				if (decorateOnEndPage()) {
					pageTemplateDecorator().onPage(document, contentFactory().apply(writer), template);
				}
			}
			
			@Override
			public void onCloseDocument(PdfWriter writer, Document document) {
				pageTemplateRenderer().render(writer, document, template);
			}
		};
	}

	public static ImmutablePageTemplate.Builder builder() {
		return ImmutablePageTemplate.builder();
	}


}
