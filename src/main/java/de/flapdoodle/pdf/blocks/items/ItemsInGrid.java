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

import com.lowagie.text.Document;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.columns.ColumnFactory;
import de.flapdoodle.pdf.extensions.MapExtensions;
import de.flapdoodle.pdf.grid.*;
import de.flapdoodle.pdf.grid.layout.NoSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.column.ColumnTexts;
import de.flapdoodle.pdf.types.Cell;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class ItemsInGrid<T> implements Block {
	protected abstract Function<Document, Grid> gridFactory();

	@Value.Default
	protected Function<ItemRenderer<ColumnText, T>, ItemInGridPlacement<ColumnText, T>> itemInGridPlacementFactory() {
		return ItemPerCell::new;
	}

	protected abstract ItemRenderer<ColumnText, T> itemRenderer();
	protected abstract List<T> items();

	@Value.Default
	protected boolean shrinkGrid() {
		return false;
	}

	@Value.Default
	protected CellLayout cellLayout() {
		return new NoSpaceBetweenCellsLayouter();
	}

	protected abstract Optional<GridCellDecorator> cellBoxDecorator();
	protected abstract Optional<GridCellDecorator> renderBoxDecorator();

	@Override
	public void render(Document document, Supplier<PdfContentByte> directContent) {
		var grid = gridFactory().apply(document);

		Function<ColumnText, String> renderError = column -> !ColumnTexts.stillSpaceLeft(column)
			? "could not render item into column - bottom at ${column.yLine}"
			: null;

		List<ItemInGridPlacement.GridCellContent<T>> gridCells = itemInGridPlacementFactory().apply(itemRenderer())
			.placeInGrid(
				grid,
				it -> ColumnFactory.DEFAULT.create(directContent.get(), it),
				renderError,
				items());

		var cellContent = MapExtensions.indexedBy(gridCells, ItemInGridPlacement.GridCellContent::cell);

		if (shrinkGrid()) {
			Map<Cell, Float> cellHeights = cellContent.entrySet()
				.stream()
				.filter(it -> it.getValue().height().isPresent())
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					it -> it.getValue().height().get()));

			grid = grid.shrinkHeight(cellHeights);
		}

		CellContentConsumer<ItemInGridPlacement.GridCellContent<T>> onCell = (column, entry) -> {
			itemRenderer().render(column, entry.item());
		};

		BiFunction<ItemInGridPlacement.GridCellContent<T>, PageBox, PageBox> processRenderBox = (entry, pageBox) -> {
			return entry.width().isPresent() ? pageBox.withWidth(entry.width().get()) : pageBox;
		};


		GridRenderer.<ItemInGridPlacement.GridCellContent<T>>builder()
			.processRenderBox(processRenderBox)
			.gridLayouter(cellLayout())
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

	public static <T> ImmutableItemsInGrid.Builder<T> builder() {
		return ImmutableItemsInGrid.builder();
	}
}
