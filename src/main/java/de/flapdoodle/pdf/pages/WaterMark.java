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
package de.flapdoodle.pdf.pages;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.watermark.WaterMarkContent;

import java.awt.*;
import java.io.IOException;

public class WaterMark extends PdfPageEventHelper {

	private final WaterMarkContent content;
	private final Color color;
	private final BaseFont baseFont;

	public WaterMark(
		WaterMarkContent content,
		Color color
	) throws IOException {
		this.content = content;
		this.color = color;
		this.baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
	}

	public WaterMark(WaterMarkContent content) throws IOException {
		this(content,  new Color(240, 240, 240));
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		renderWatermark(writer, document);
	}

	private void renderWatermark(PdfWriter writer, Document document) {
		var direct = writer.getDirectContentUnder();
		try {
			var box = PageBox.fullPageBox(document);

			direct.saveState();

			direct.setColorFill(color);
			direct.beginText();
			direct.setFontAndSize(baseFont, 20f);

			var message = content.repeatUntilLen(200);

			var lineHeight = 20f * 1.5f;
			var linesNeeded = (int) (box.height() / lineHeight) * 2;

			var startYoffset = -box.width();

			for (int l : new IntRange(0, linesNeeded)) {
				var xoffset = l * lineHeight * 0.3f;
				direct.showTextAligned(
					Element.ALIGN_LEFT,
					message,
					box.left() - (xoffset),
					box.bottom() + (l * lineHeight) + startYoffset,
					45f
				);
			}
			direct.endText();
		} finally {
			direct.restoreState();
		}
	}

}
