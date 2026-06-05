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

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import de.flapdoodle.pdf.tables.cells.BorderStyle;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.BorderProperty;

import java.awt.*;
import java.util.Optional;

public class Elements {
	public static Element title(String text, Optional<Color> background, Optional<Font> font) {
		return TableElement.builder()
			.columns(TableElement.Columns.count(1))
			.widthPercentage(100.0f)
			.addCells(PdfPCellFactory.builder()
				.phrase(PhraseElement.of(text, font))
				.cellStyle(CellStyle.empty()
					.withBackgroundColor(background)
					.withHorizontalAlignment(HorizontalAlignment.CENTER)
					.withVerticalAlignment(VerticalAlignment.MIDDLE)
					.withBorder(BorderStyle.noBorder())
					.withPadding(BorderProperty.of(2f)
						.withBottom(5f)))
				.build())
			.build().create();
	}
}
