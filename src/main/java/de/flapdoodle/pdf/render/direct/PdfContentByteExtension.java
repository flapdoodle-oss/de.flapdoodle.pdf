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
package de.flapdoodle.pdf.render.direct;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.types.Floats;

public class PdfContentByteExtension {

	private static final Phrase SMALL_NEW_LINE = new Phrase("\n", FontFactory.getFont(FontFactory.HELVETICA, 1f, Font.NORMAL));

	public static float verticalPosition(PdfContentByte it) {
		return it.getPdfWriter().getVerticalPosition(true);
	}

	public static void setVerticalPosition(PdfContentByte it, float position) {
		if (Floats.isNearBy(position, it.getPdfDocument().bottom())) {
			it.getPdfDocument().newPage();
			return;
		}
		if (position <= it.getPdfDocument().bottom()) {
			throw new IllegalArgumentException("position unreachable: "+position+" < "+it.getPdfDocument().bottom());
		}

		var lastPosition = verticalPosition(it);
		while (position <= verticalPosition(it)) {
			it.getPdfDocument().add(SMALL_NEW_LINE);
			var newVerticalPosition = verticalPosition(it);

			if (newVerticalPosition > lastPosition) {
				throw new IllegalArgumentException("something bad at "+lastPosition);
			} else {
				lastPosition = newVerticalPosition;
			}
		}
	}

}
