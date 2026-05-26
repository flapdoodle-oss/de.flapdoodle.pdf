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
package de.flapdoodle.pdf.blocks;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.render.Documents;
import de.flapdoodle.pdf.elements.Elements;
import org.immutables.value.Value;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

@Value.Immutable
public interface Section extends Block {
	String title();

	Optional<Color> backgroundColor();

	Optional<Font> font();

	@Value.Default
	default float minPageHeightLeft() {
		return 0.2f;
	}

	@Override
	@Value.Auxiliary
	default void render(Document document, Supplier<PdfContentByte> directContent) {
		if (Documents.partOfPageHeightLeft(document, directContent) < minPageHeightLeft()) {
			document.newPage();
		}

		document.add(Elements.title(title(), backgroundColor(), font()));
	}

	static ImmutableSection.Builder builder() {
		return ImmutableSection.builder();
	}
}
