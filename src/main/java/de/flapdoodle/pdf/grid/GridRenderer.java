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
package de.flapdoodle.pdf.grid;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.columns.ColumnFactory;
import de.flapdoodle.pdf.grid.layout.NoSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.direct.PdfContentByteExtension;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Value.Immutable
public abstract class GridRenderer<T> {
	protected abstract Optional<GridCellDecorator> cellBoxDecorator();
	protected abstract Optional<GridCellDecorator> renderBoxDecorator();

	@Value.Default
	protected GridLayouter gridLayouter() {
		return new NoSpaceBetweenCellsLayouter();
	};

	@Value.Default
	protected BiFunction<T, PageBox, PageBox> processRenderBox() {
		return (t, it) -> it;
	}

	public void render(
		Document document,
		Supplier<PdfContentByte> directContent,
	Grid grid,
	GridContent<T> cellContent,
	BiConsumer<ColumnText, T> onCell
  ) {
		final AtomicReference<Float> currentOffset = new AtomicReference<>(PdfContentByteExtension.verticalPosition(directContent.get()));
		var innerBox = PageBox.innerBox(document);
		var pageDim = innerBox.dimension();

		var sets = GridPartitioner.partition(grid.trim(cellContent),
			document.top() - currentOffset.get(), pageDim);


		sets.forEach(set -> {
			if (set.onNewPage()) {
				document.newPage();
				currentOffset.set(PdfContentByteExtension.verticalPosition(directContent.get()));
			}

			var layoutedCells = gridLayouter().layout(grid, set.position(), set.cells(), innerBox);

			layoutedCells.forEach(cellLayout -> {
				cellBoxDecorator().ifPresent(it -> it.decorate(directContent.get(), cellLayout.cellBox()));
				renderBoxDecorator().ifPresent(it -> it.decorate(directContent.get(), cellLayout.renderBox()));

				var cellCon = cellContent.get(cellLayout.cell());
				if (cellCon.isPresent()) {
					var column = ColumnFactory.DEFAULT.create(directContent.get(), processRenderBox().apply(cellCon.get(), cellLayout.renderBox()));
					onCell.accept(column, cellCon.get());
					column.go();
				}

				currentOffset.set(cellLayout.cellBox().bottom());
			});
		});

		PdfContentByteExtension.setVerticalPosition(directContent.get(), currentOffset.get());
	}



	public static <T> ImmutableGridRenderer.Builder<T> builder() {
		return ImmutableGridRenderer.builder();
	}
}
