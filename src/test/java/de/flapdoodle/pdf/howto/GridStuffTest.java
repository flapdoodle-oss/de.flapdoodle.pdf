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
package de.flapdoodle.pdf.howto;

import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.Section;
import de.flapdoodle.pdf.blocks.grid.ImmutableRenderGrid;
import de.flapdoodle.pdf.blocks.grid.RenderGrid;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridCellDecorator;
import de.flapdoodle.pdf.grid.layout.HorizontalSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.grid.layout.NoSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class GridStuffTest {
	@RegisterExtension
	public static Recording recording = Recorder.with("Grids.md", TabSize.spaces(2));

	@Test
	public void twoColumns3Rows() {
		recording.begin();
		RenderGrid<String> renderGrid = RenderGrid.<String>builder()
			.grid(new Grid(Margin.of(5.0f, 5.0f, 5.0f, 5.0f),
				List.of(100.0f, 50.0f),
				List.of(50.0f, 150.0f, 50.0f)))
			.cellBoxDecorator(GridCellDecorator.renderBorder(Color.DARK_GRAY))
			.renderBoxDecorator(GridCellDecorator.renderBorder(Color.LIGHT_GRAY))
			.layouter(new NoSpaceBetweenCellsLayouter())
			.contentLookup(cell -> Optional.of(cell.column()+":"+cell.row()))
			.contentRenderer((column, value) -> column.addElement(new Phrase(value)))
			.build();
		recording.end();

		byte[] content = render(renderGrid);
		recording.file("pdf", "grid-2columns3rows.pdf", content);
		recording.file("png", "grid-2columns3rows.png", PdfImageGenerator.renderPageAsPng(content, 0));
	}

	private byte[] render(Block... blocks) {
		DocumentFactory factory = DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addOnPageEvents(PageBorders.renderDocumentHints())
			.addBlocks(blocks)
			.build();

		return IO.withOutputStream(factory::render);

	}

}
