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
package de.flapdoodle.pdf.render.table;

import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;

public class PdfPCells {
	public static PdfPCell applyStyle(PdfPCell cell, CellStyle style) {
		if (style.backgroundColor().isPresent()) {
			cell.setBackgroundColor(style.backgroundColor().get());
		}

		cell.setHorizontalAlignment(switch (style.horizontalAlignment().orElse(HorizontalAlignment.LEFT)) {
			case CENTER -> Element.ALIGN_CENTER;
			case RIGHT -> Element.ALIGN_RIGHT;
			default -> Element.ALIGN_LEFT;
		});

		if (style.sameStyleForAllBorders()) {
			style.border().ifPresent(borderStyle -> {
				cell.setBorder(Rectangle.BOX);
				borderStyle.width().ifPresent(cell::setBorderWidth);
				borderStyle.color().ifPresent(cell::setBorderColor);
			});
		} else {
			cell.setUseVariableBorders(true);
			cell.setBorder(Rectangle.NO_BORDER);
			style.borderTop().ifPresent(borderStyle -> {
				cell.enableBorderSide(Rectangle.TOP);
				borderStyle.width().ifPresent(cell::setBorderWidthTop);
				borderStyle.color().ifPresent(cell::setBorderColorTop);
			});

			style.borderLeft().ifPresent(borderStyle -> {
				cell.enableBorderSide(Rectangle.LEFT);
				borderStyle.width().ifPresent(cell::setBorderWidthLeft);
				borderStyle.color().ifPresent(cell::setBorderColorLeft);
			});

			style.borderRight().ifPresent(borderStyle -> {
				cell.enableBorderSide(Rectangle.RIGHT);
				borderStyle.width().ifPresent(cell::setBorderWidthRight);
				borderStyle.color().ifPresent(cell::setBorderColorRight);
			});

			style.borderBottom().ifPresent(borderStyle -> {
				cell.enableBorderSide(Rectangle.BOTTOM);
				borderStyle.width().ifPresent(cell::setBorderWidthBottom);
				borderStyle.color().ifPresent(cell::setBorderColorBottom);
			});
		}

		return cell;
	}

	public static PdfPCell clone(PdfPCell cell)  {
		return new PdfPCell(cell);
	}

	public static PdfPCell with(PdfPCell cell, Phrase phrase) {
		PdfPCell cloned = clone(cell);
		cloned.setPhrase(phrase);
		return cloned;
	}

}
