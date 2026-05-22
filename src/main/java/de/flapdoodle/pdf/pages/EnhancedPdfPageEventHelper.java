package de.flapdoodle.pdf.pages;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPageEvent;
import com.lowagie.text.pdf.PdfPageEventHelper;

public class EnhancedPdfPageEventHelper extends PdfPageEventHelper {

	protected static PageBox headerBox(Document document) {
		var innerBox = PageBox.innerBox(document);
		var pageHeight = document.getPageSize().getHeight();

		return new PageBox(
			innerBox.left(),
			innerBox.bottom() + innerBox.height(),
			innerBox.width(),
			pageHeight - (innerBox.bottom() + innerBox.height())
		);
	}

	protected static PageBox footerBox(Document document) {
		var innerBox = PageBox.innerBox(document) ;
		return new PageBox(innerBox.left(), 0f, innerBox.width(), innerBox.bottom());
	}


}
