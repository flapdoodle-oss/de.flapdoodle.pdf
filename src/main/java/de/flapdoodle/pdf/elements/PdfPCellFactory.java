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
import de.flapdoodle.pdf.render.table.PdfPCells;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import org.immutables.value.Value;
import org.openpdf.text.Image;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;

import java.util.Optional;
import java.util.stream.Stream;

@Value.Immutable
public abstract class PdfPCellFactory {

	protected abstract Optional<ElementSupplier<Phrase>> phrase();

	protected abstract Optional<ElementSupplier<Image>> image();

	protected abstract Optional<ElementSupplier<?>> element();

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

		phrase().ifPresent(it -> {
			Phrase phrase = it.create();
			cellStyle().font().ifPresent(font -> PhraseElement.setFont(phrase, font));
			cell.setPhrase(phrase);
		});
		image().ifPresent(it -> cell.setImage(it.create()));
		element().ifPresent(it -> cell.addElement(it.create()));
		return cell;
	}

	public sealed interface CellHeight {
		record FixedHeight(float height) implements CellHeight {}
		record MinHeight(float height) implements CellHeight {}
	}

	public static ImmutablePdfPCellFactory.Builder builder() {
		return ImmutablePdfPCellFactory.builder();
	}
}
