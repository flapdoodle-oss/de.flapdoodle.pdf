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
package de.flapdoodle.pdf.grid;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.pages.PageBox;

import java.awt.*;

public interface GridCellDecorator {
	void decorate(PdfContentByte directContent, PageBox cellBox);

	static GridCellDecorator renderBorder() {
		return renderBorder(new Color(235, 235, 235));
	}
	static GridCellDecorator renderBorder(Color color) {
		return (PdfContentByte directContent, PageBox cellBox) -> {
			Rectangle rect = cellBox.asRectangle();
			rect.setBorder(Rectangle.BOX);
			rect.setBorderColor(color);
			rect.setBorderWidth(0.5f);
			directContent.rectangle(rect);
		};
	}
}
