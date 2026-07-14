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

import org.openpdf.text.Document;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.blocks.split.*;
import de.flapdoodle.pdf.grid.*;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.Documents;
import de.flapdoodle.pdf.render.table.*;
import de.flapdoodle.pdf.tables.Table;
import org.immutables.value.Value;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public abstract class AutosplitTable implements Block {

	protected abstract Table table();

	@Value.Default
	protected ScaleToColumnWidth scaleToColumnWidth() {
		return ScaleToColumnWidth.NOT_LAST_COLUMNS;
	}

	@Value.Default
	protected RegionColumnRenderer columnRenderer() {
		return new DefaultRegionColumnRenderer();
	}

	@Value.Default
	protected BiFunction<PdfContentByte, RegionColumnRenderer, TableRenderer> tableRenderer() {
		return (ColumnTableRenderer::new);
	}

	@Value.Default
	protected RenderedTableSplitter renderedTableSplitter() {
		return new DefaultRenderedTableSplitter();
	}

	@Value.Default
	protected boolean repeatHeader() {
		return false;
	}

	@Value.Default
	protected int keyColumns() {
		return 0;
	}

	@Value.Default
	protected MinimalTableWidth minimalTableWidth() {
		return new FindSmallestTableWidthFromRendering();
	}

	@Override
	public void render(Document document, Supplier<PdfContentByte> directContent) {
		TableRenderer tableRenderer = tableRenderer().apply(directContent.get(), columnRenderer());
		PageBox innerBox = PageBox.innerBox(document);

		RenderedTableDimension dimensions = minimalTableWidth()
			.of(tableRenderer, table(), innerBox.width() * 10);

		var spaceLeftOnPage = Documents.pageHeightLeft(document, directContent);
		var spaceNeededOnNextPages = dimensions.height() - spaceLeftOnPage;

		if (dimensions.width() <= innerBox.width()) {
			// fits on page width
			float tableWidth = switch (scaleToColumnWidth()) {
				case NEVER -> dimensions.width();
				default -> innerBox.width();
			};

			if (dimensions.height() <= spaceLeftOnPage) {
				// fits on this page
				TableRenderer.Result result = tableRenderer.render(
					table(),
					TableAttributes.defaults(),
					table().maxRegion(),
					innerBox.withWidth(tableWidth)
				);
				result.go();
			} else {
				// needs more than one page
				var additionalGridRows = (int) ((spaceNeededOnNextPages + innerBox.height()) / innerBox.height());

				ImmutableGrid grid = Grid.builder()
					.addWidths(tableWidth)
					.addHeights(spaceLeftOnPage)
					.addAllHeights(Stream.generate(innerBox::height).limit(additionalGridRows).toList())
					.build();

				Split split = renderedTableSplitter()
					.split(grid, dimensions, scaleToColumnWidth(), table().header().isPresent(), keyColumns(), repeatHeader());

				var cellSplitTableMap = split.parts().stream()
					.collect(Collectors.toMap(
						Split.Part::cell,
						part -> new SplittedTable(table(), TableAttributes.defaults(), part.region(), part.hasHeader(), split.keyColumns())
					));

				CellContentLookup<SplittedTable> lookup = CellContentLookup.fromMap(cellSplitTableMap);

				CellContentConsumer<SplittedTable> renderer=new CellContentConsumer<SplittedTable>() {
					@Override
					public void fillCell(ColumnText columnText, SplittedTable table) {
						// TODO pass dimensions into renderer
						columnRenderer().render(columnText, table, table.maxRegion());
					}
				};

				GridRenderer.<SplittedTable>builder()
					.build()
					.render(
						document,
						directContent,
						grid,
						lookup,
						renderer
					);
			}
		} else {
			// needs more than one page
			var gridColumns = (int) (dimensions.width() / innerBox.width()) + 1;
			var gridRows = (int) (dimensions.height() / innerBox.height()) + 1;

			ImmutableGrid grid = Grid.builder()
				.addAllWidths(Stream.generate(innerBox::width).limit(gridColumns).toList())
				.addAllHeights(Stream.generate(innerBox::height).limit(gridRows).toList())
				.build();

			Split split = renderedTableSplitter()
				.split(grid, dimensions, scaleToColumnWidth(), table().header().isPresent(), keyColumns(), repeatHeader());

			var cellSplitTableMap = split.parts().stream()
				.collect(Collectors.toMap(
					Split.Part::cell,
					part -> new SplittedTable(table(), part.tableAttributes(), part.region(), part.hasHeader(), split.keyColumns())
				));

			CellContentLookup<SplittedTable> lookup = CellContentLookup.fromMap(cellSplitTableMap);

			CellContentConsumer<SplittedTable> renderer=new CellContentConsumer<SplittedTable>() {
				@Override
				public void fillCell(ColumnText columnText, SplittedTable table) {
					// TODO pass dimensions into renderer
					columnRenderer().render(columnText, table, table.tableAttributes(), table.maxRegion());
				}
			};

			GridRenderer.<SplittedTable>builder()
				.build()
				.render(
					document,
					directContent,
					grid,
					lookup,
					renderer
				);
		}
	}

	public static ImmutableAutosplitTable.Builder builder() {
		return ImmutableAutosplitTable.builder();
	}
}
