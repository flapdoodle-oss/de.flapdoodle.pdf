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
package de.flapdoodle.pdf;

import de.flapdoodle.pdf.pages.EnhancedPdfPageEventHelper;
import de.flapdoodle.pdf.pages.PageBox;
import org.openpdf.text.Document;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEvent;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.*;

public class PageBorders {
	public static PdfPageEvent renderDocumentHints() {
		return new EnhancedPdfPageEventHelper() {
			@Override
			public void onEndPage(PdfWriter writer, Document document) {
				PdfContentByte canvas = writer.getDirectContent();

				canvas.saveState();

//				canvas.setLineWidth(1f);
				canvas.setLineDash(5f, 3f, 0f);  // 5pt Strich, 3pt Lücke

				Rectangle innerBox = PageBox.innerBox(document).asRectangle();
				innerBox.setBorder(Rectangle.BOX);
				innerBox.setBorderColor(Color.LIGHT_GRAY);
				innerBox.setBorderWidth(0.5f);
				canvas.rectangle(innerBox);
				canvas.stroke();

				Rectangle pageBox = PageBox.fullPageBox(document).asRectangle();
				pageBox.setBorder(Rectangle.BOX);
				pageBox.setBorderColor(Color.DARK_GRAY);
				pageBox.setBorderWidth(0.5f);
				pageBox.setRight(pageBox.getRight() - 2*0.5f);
				pageBox.setTop(pageBox.getTop() - 0.5f);
				pageBox.setBottom(pageBox.getBottom() + 0.5f);
				canvas.rectangle(pageBox);
				canvas.stroke();


				canvas.restoreState();
			}
		};
	}
}
