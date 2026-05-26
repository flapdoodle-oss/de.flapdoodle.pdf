package de.flapdoodle.pdf.blocks;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.render.direct.PdfContentByteExtension;

import java.util.function.Supplier;

public record Space(
	float offset
) implements Block {
	@Override
	public void render(Document document, Supplier<PdfContentByte> directContent) {
		PdfContentByteExtension.setVerticalPosition(directContent.get(), PdfContentByteExtension.verticalPosition(directContent.get()) - offset);
	}
}
