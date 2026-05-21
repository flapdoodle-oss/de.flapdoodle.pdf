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
package de.flapdoodle.pdf.render.elements;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import de.flapdoodle.pdf.render.table.PdfPCells;

import java.awt.*;
import java.util.Optional;

public class Elements {
	public static Element title(String text, Optional<Color> background, Optional<Font> font) {
		var table = new PdfPTable(1);
		table.setWidthPercentage(100f);

		var cell = PdfPCells.clone(table.getDefaultCell());
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setBorderWidth(0f);
		background.ifPresent(cell::setBackgroundColor);
		cell.setPadding(2f);
		cell.setPaddingBottom(5f);

		table.addCell(PdfPCells.with(cell, new Phrase(text, font.orElse(new Font()))));
		return table;
	}
}
