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

import de.flapdoodle.pdf.render.direct.PdfContentByteExtension;
import org.immutables.value.Value;
import org.openpdf.text.Document;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Value.Immutable
public abstract class TagDecorator extends EnhancedPdfPageEventHelper {
	@Value.Default
	protected Function<PdfWriter, PdfContentByte> contentFactory() {
		return PdfWriter::getDirectContent;
	}

	protected abstract OnGenericTag onGenericTag();

	@Override
	public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
		PdfContentByte content = contentFactory().apply(writer);
		PdfContentByteExtension.withRestoreState(content, () -> {
			onGenericTag().decorate(writer, document, content, rect, text);
		});
	}

	@FunctionalInterface
	public interface OnGenericTag {
		void decorate(PdfWriter writer, Document document, PdfContentByte content, Rectangle rectangle, String text);
	}

	public static ImmutableTagDecorator.Builder builder() {
		return ImmutableTagDecorator.builder();
	}
}
