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
package de.flapdoodle.pdf.elements;

import com.google.common.base.Preconditions;
import org.openpdf.text.Element;
import org.openpdf.text.Image;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import de.flapdoodle.pdf.render.table.PdfPCells;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@Value.Immutable
public abstract class PdfPCellFactory {

	protected abstract Optional<ElementSupplier<Phrase>> phrase();

	protected abstract Optional<ElementSupplier<Image>> image();

	protected abstract Optional<ElementSupplier<Element>> element();

	protected abstract Optional<CellHeight> cellHeight();

	@Value.Default
	protected CellStyle cellStyle() {
		return CellStyle.empty()
			.withHorizontalAlignment(HorizontalAlignment.CENTER);
	}

	@Value.Check
	protected void check() {
		long activeElements = Stream.of(phrase(), image(), element())
			.filter(Optional::isPresent)
			.count();
		
		Preconditions.checkArgument(activeElements <= 1, "you must only set one: phrase, image or element");
	}

	public PdfPCell create(PdfPCell defaultCell) {
		PdfPCell cell = PdfPCells.clone(defaultCell);

		cellHeight().ifPresent(cellHeight -> {
			switch (cellHeight) {
				case CellHeight.MinHeight min -> cell.setMinimumHeight(min.height);
				case CellHeight.FixedHeight fixed -> cell.setFixedHeight(fixed.height);
			}
		});
		
		PdfPCells.applyStyle(cell, cellStyle());

//		cell.setBorder(Rectangle.NO_BORDER);
//		cell.setBorderWidth(0f);
//		background().ifPresent(cell::setBackgroundColor);
//		cell.setPadding(2f);
//		cell.setPaddingBottom(5f);

		set(cell, phrase(), PdfPCell::setPhrase);
		set(cell, image(), PdfPCell::setImage);
		set(cell, element(), PdfPCell::addElement);
		return cell;
	}

	public sealed interface CellHeight {
		record FixedHeight(float height) implements CellHeight {}
		record MinHeight(float height) implements CellHeight {}
	}

	private static <T extends Element> void set(PdfPCell cell, Optional<ElementSupplier<T>> factory, BiConsumer<PdfPCell, T> setter) {
		factory.ifPresent(t -> setter.accept(cell, t.create()));
	}

	public static ImmutablePdfPCellFactory.Builder builder() {
		return ImmutablePdfPCellFactory.builder();
	}
}
