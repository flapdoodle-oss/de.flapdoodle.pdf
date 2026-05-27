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
import de.flapdoodle.pdf.blocks.ImmutableTablesInGrid;
import de.flapdoodle.pdf.blocks.Section;
import de.flapdoodle.pdf.blocks.TablesInGrid;
import de.flapdoodle.pdf.blocks.grid.ImmutableRenderGrid;
import de.flapdoodle.pdf.blocks.grid.RenderGrid;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.GridCellDecorator;
import de.flapdoodle.pdf.grid.layout.HorizontalSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.grid.layout.NoSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.grid.tablesplitter.posterize.DefaultPosterSplitter;
import de.flapdoodle.pdf.grid.tablesplitter.posterize.PutColumnIntoSlotIfAllOfItWillFit;
import de.flapdoodle.pdf.grid.tablesplitter.posterize.SplitTableIntoPoster;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.table.MinimalTableWidth;
import de.flapdoodle.pdf.tables.*;
import de.flapdoodle.pdf.tables.cells.*;
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Range;
import de.flapdoodle.pdf.types.Region;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@Test
	public void posterizeTable() {
		TableFromMap table = TableFromMap.builder()
			.header(TableColumnsFromNameList.builder()
				.addColumnNames("A", "B", "C", "D","E","F","G","H","I")
				.styles(HeaderStyles.asHeaderStyles(LayeredCellStyles.empty()
					.withDefault(CellStyle.empty()
						// TODO wenn der header breiter ist als die Spalte,
						//  dann wird der Header zusammen gestutzt?
						.withBackgroundColor(Color.LIGHT_GRAY)
						.withPadding(BorderProperty.of(5.0f))
						.withHorizontalAlignment(HorizontalAlignment.CENTER))))
				.build())
			.styles(LayeredCellStyles.empty()
				.withDefault(CellStyle.empty()
					.withPadding(BorderProperty.of(20.f, 10.0f, 20.f, 10.f))
					.withHorizontalAlignment(HorizontalAlignment.CENTER)))
			.columnWeights(ColumnWeights.EMPTY)
			.cells(new Region(new Range(0, 9 - 1), new Range(0, 30 - 1))
				.map(Cell::new)
				.stream()
				.collect(Collectors.toMap(it -> it, it -> it.column() + ":" + it.row())))
			.build();

		recording.begin();
		TablesInGrid tablesInGrid = TablesInGrid.builder()
			.gridFactory(doc -> {
				PageBox innerBox = PageBox.innerBox(doc);
				return new Grid(Margin.none(), 2, innerBox.width(), 4, innerBox.height());
			})
			.tableSplitterFactory(SplitTableIntoPoster::split)
			.addTables(table)
			.build();
		recording.end();

		byte[] content = render(tablesInGrid);
		recording.file("pdf", "grid-posterize.pdf", content);
		recording.file("png-0", "grid-posterize-0.png", PdfImageGenerator.renderPageAsPng(content, 0));
		recording.file("png-1", "grid-posterize-1.png", PdfImageGenerator.renderPageAsPng(content, 1));
		recording.file("png-2", "grid-posterize-2.png", PdfImageGenerator.renderPageAsPng(content, 2));
		recording.file("png-3", "grid-posterize-3.png", PdfImageGenerator.renderPageAsPng(content, 3));
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
