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
import org.openpdf.text.pdf.PdfWriter;
import de.flapdoodle.pdf.columns.ColumnFactory;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class PageParagraphDecorators extends EnhancedPdfPageEventHelper {
	protected abstract Optional<PageHeaderOrFooterFactory> headerFactory();
	protected abstract Optional<PageHeaderOrFooterFactory> footerFactory();
	
	@Value.Default
	protected float leadingOffset() {
		return 5f;
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		if (headerFactory().isPresent()) {
			var headerContent = headerFactory().get().forPage(document.getPageNumber());
			var heightWithLeading = headerContent.getTotalLeading() + leadingOffset();

			var headerColumn = ColumnFactory.DEFAULT.create(
				writer.getDirectContentUnder(),
				headerBox(document).withHeight(heightWithLeading)
			);

			headerColumn.addElement(headerContent);
			headerColumn.go();
		}

		if (footerFactory().isPresent()) {
			var footerContent = footerFactory().get().forPage(document.getPageNumber());

			var footerColumn = ColumnFactory.DEFAULT.create(
				writer.getDirectContentUnder(),
				footerBox(document)
			);
			footerColumn.addElement(footerContent);
			footerColumn.go();
		}
	}

	public static ImmutablePageParagraphDecorators.Builder builder() {
		return ImmutablePageParagraphDecorators.builder();
	}
}
