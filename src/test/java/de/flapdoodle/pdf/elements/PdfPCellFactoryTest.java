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

import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.DocumentFactoryAssert;
import de.flapdoodle.commons.checks.Preconditions;
import de.flapdoodle.pdf.tables.cells.BorderStyle;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.BorderProperty;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfPCellFactoryTest {
	@Test
	void usageTest() {
		ImmutablePdfPCellFactory cellFactory = PdfPCellFactory.builder()
			.element(TableElement.builder()
				.columns(TableElement.Columns.count(1))
				.build())
			.build();

		assertThat(cellFactory).isNotNull();
	}

	@Test
	void checksTest() {
		assertThatThrownBy(() -> PdfPCellFactory.builder().colSpan(0).build())
			.hasMessageContaining("colSpan");
		assertThatThrownBy(() -> PdfPCellFactory.builder().rowSpan(0).build())
			.hasMessageContaining("rowSpan");
		assertThatThrownBy(() -> PdfPCellFactory.builder()
			.phrase(PhraseElement.of("phrase"))
			.element(PhraseElement.of("element"))
			.build())
			.hasMessageContaining("you must only set one");
	}

	@Test
	void renderTableInCell() {
		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.addBlocks((document, directContent) -> {
					document.add(TableElement.builder()
							.columns(TableElement.Columns.count(1))
							.addCells(PdfPCellFactory.builder()
								.cellStyle(CellStyle.empty().withBordeStyle(BorderProperty.of(BorderStyle.of(Color.RED, 1f))))
								.element(TableElement.builder()
									.columns(TableElement.Columns.count(1))
									.addCells(PdfPCellFactory.builder()
										.cellStyle(CellStyle.empty().withBordeStyle(BorderProperty.of(BorderStyle.of(Color.BLUE, 1f))))
										.phrase(PhraseElement.of("Test"))
										.build())
									.build())
								.build())
							.build().create());
				})
				.build())
			.expectRendering()
			.matchesResource(getClass(), "tableInCell.pdf");

	}

	@Test
	void renderImageInCell() {
		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.addBlocks((document, directContent) -> {
					document.add(TableElement.builder()
						.columns(TableElement.Columns.relativeWeights(2f, 1f))

						.addCells(PdfPCellFactory.builder()
							.cellStyle(CellStyle.empty().withBordeStyle(BorderProperty.of(BorderStyle.of(Color.BLACK, 1f))))
							.image(image("quad.jpg"))
							.build())
						.addCells(PdfPCellFactory.builder()
							.cellStyle(CellStyle.empty().withBordeStyle(BorderProperty.of(BorderStyle.of(Color.BLACK, 1f))))
							.image(image("rectangle.png"))
							.build())
						.addCells(PdfPCellFactory.builder()
							.cellStyle(CellStyle.empty()
								.withBordeStyle(BorderProperty.of(BorderStyle.of(Color.BLACK, 1f)))
								.withHorizontalAlignment(HorizontalAlignment.CENTER)
								.withVerticalAlignment(VerticalAlignment.MIDDLE))
							.image(image("rectangle.png"))
							.build())
						.addCells(PdfPCellFactory.builder()
							.cellStyle(CellStyle.empty()
								.withBordeStyle(BorderProperty.of(BorderStyle.of(Color.BLACK, 1f)))
								.withHorizontalAlignment(HorizontalAlignment.CENTER)
								.withVerticalAlignment(VerticalAlignment.MIDDLE))
							.image(image("quad.jpg"))
							.build())
						.build().create());
				})
				.build())
			.expectRendering()
			.matchesResource(getClass(), "imageInCell.pdf");
	}

	private static ElementSupplier<Image> image(String name) {
		return () -> {
			try {
				return Image.getInstance(Preconditions.checkNotNull(PdfPCellFactoryTest.class.getResource(name),"image $s not found", name));
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}
}