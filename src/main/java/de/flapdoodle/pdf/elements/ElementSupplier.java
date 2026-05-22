package de.flapdoodle.pdf.elements;

import com.lowagie.text.Element;

public interface ElementSupplier<E extends Element> {
	E create();
}
