package de.flapdoodle.pdf.pages;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
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
