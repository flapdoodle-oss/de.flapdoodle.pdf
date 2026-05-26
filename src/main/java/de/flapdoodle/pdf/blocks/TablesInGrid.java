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
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.extensions.MapExtensions;
import de.flapdoodle.pdf.grid.*;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.table.ColumnTableRenderer;
import de.flapdoodle.pdf.render.table.DefaultRegionColumnRenderer;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class TablesInGrid implements Block {
	protected abstract Function<Document, Grid> gridFactory();

	protected abstract Function<TableRenderer, TableSplitter> tableSplitterFactory();

	protected abstract List<Table> tables();

	@Value.Default
	protected boolean shrinkGrid() {
		return false;
	}
	protected abstract Optional<GridCellDecorator> cellBoxDecorator();

	protected abstract Optional<GridCellDecorator> renderBoxDecorator();

	@Override
	public void render(Document document, Supplier<PdfContentByte> directContent) {
		var grid = gridFactory().apply(document);

		var regionRenderer = new DefaultRegionColumnRenderer();
		var tableRenderer = new ColumnTableRenderer(directContent.get(), regionRenderer);

		var cellContent = MapExtensions.indexedBy(tableSplitterFactory().apply(tableRenderer)
			.split(grid, tables()), TableSplitter.GridCellContent::cell);

		if (shrinkGrid()) {
			grid = grid.shrinkHeight(cellContent.entrySet().stream()
				.filter(it -> it.getValue().height().isPresent())
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					it -> it.getValue().height().get())
				));
		}

		CellContentConsumer<TableSplitter.GridCellContent> onCell = (column, entry) ->
			regionRenderer.render(column, entry.table(), entry.region());

		BiFunction<TableSplitter.GridCellContent, PageBox, PageBox> processRenderBox = (entry, pageBox) ->
			entry.width().isPresent() ? pageBox.withWidth(entry.width().get()) : pageBox;

		GridRenderer.<TableSplitter.GridCellContent>builder()
			.processRenderBox(processRenderBox)
			.cellBoxDecorator(cellBoxDecorator())
			.renderBoxDecorator(renderBoxDecorator())
			.build()
			.render(
				document,
				directContent,
				grid,
				CellContentLookup.fromMap(cellContent),
				onCell
			);
	}

	public static ImmutableTablesInGrid.Builder builder() {
		return ImmutableTablesInGrid.builder();
	}
}
