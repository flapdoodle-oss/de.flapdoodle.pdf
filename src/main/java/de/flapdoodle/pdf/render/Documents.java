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
package de.flapdoodle.pdf.render;

import org.openpdf.text.Document;
import org.openpdf.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.direct.PdfContentByteExtension;

import java.util.function.Supplier;

public class Documents {

	public static float partOfPageHeightLeft(Document document, Supplier<PdfContentByte> directContent) {
		var innerBox = PageBox.innerBox(document);
//		var pos = PdfContentByteExtension.verticalPosition(directContent.get());
//		return (pos - innerBox.bottom()) / innerBox.height();
		return pageHeightLeft(document, directContent) / innerBox.height();
	}

	public static float pageHeightLeft(Document document, Supplier<PdfContentByte> directContent) {
		var innerBox = PageBox.innerBox(document);
		var pos = PdfContentByteExtension.verticalPosition(directContent.get());
		return (pos - innerBox.bottom());
	}
}
