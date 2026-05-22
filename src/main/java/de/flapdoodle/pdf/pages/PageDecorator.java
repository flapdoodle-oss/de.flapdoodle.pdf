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
