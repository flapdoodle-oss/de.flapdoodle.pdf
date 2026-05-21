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
package de.flapdoodle.pdf.blocks.items;

import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import de.flapdoodle.pdf.render.column.ColumnTexts;
import de.flapdoodle.pdf.render.elements.Elements;
import de.flapdoodle.pdf.render.table.DefaultRegionColumnRenderer;
import de.flapdoodle.pdf.render.table.RegionColumnRenderer;

import java.awt.*;
import java.util.Optional;

public record TableWithTitleRenderer(
	RegionColumnRenderer regionColumnRenderer //: RegionColumnRenderer = DefaultRegionColumnRenderer()
) implements ItemRenderer<ColumnText, TableWithTitle> {

	private static final Font SPACER_FONT = new Font(Font.HELVETICA, 4f, Font.NORMAL, Color.BLACK);

	public TableWithTitleRenderer() {
		this(new DefaultRegionColumnRenderer());
	}

	@Override
	public Optional<Float> render(ColumnText box, TableWithTitle item) {
		var start = box.getYLine();

		box.addElement(Elements.title(item.title(), Optional.empty(), Optional.empty()));
		box.addElement(new Phrase("\n", SPACER_FONT));
		if (!item.table().isEmpty()) {
			regionColumnRenderer.render(box, item.table(), item.table().maxRegion());
		}

		return Optional.of(start - ColumnTexts.yLineIfCommited(box));
	}
}
