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

import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.grid.RenderGrid;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.grid.layout.HorizontalSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.grid.layout.NoSpaceBetweenCellsLayouter;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.types.Cell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class GridRendererTest {
	private Margin testMargin = Margin.of(2f, 4f, 4f, 2f);

	@Test
	@DisplayName("single cell grid must fit to page")
	void singleCellGridMustFitToPage() {
		var grid = new Grid(testMargin, 1, 100f, 1, 100f);

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(render(grid)))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "singleGrid.pdf");
	}

	@Test
	@DisplayName("grid must render after last content")
	void gridMustRenderAfterLastContent() {
		var grid = new Grid(testMargin, 1, 100f, 1, 100f);

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(new Text("before grid"), render(grid)))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "gridAfterContent.pdf");
	}

		@Test
	@DisplayName("grid with page size cells must split to number of cells pages")
	void gridWithPageSizeCellsMustSplitToNumberOfCellsPages() {
		var pageBox = PageBox.asPageBox(PageSize.A4).withMargin(DocumentFactory.DEFAULT_PAGE_MARGINS);

		var grid = new Grid(testMargin, 2, pageBox.width(), 2, pageBox.height());

			assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.blocks(List.of(render(grid)))
				.build())
				.expectRendering()
				.matchesResource(getClass(), "splitToPages.pdf");
	}

	@Test
	@DisplayName("grid with page size cells must split to number of cells pages with first on new page")
	void gridWithPageSizeCellsMustSplitToNumberOfCellsPagesWithFirstOnNewPage() {
		var pageBox = PageBox.asPageBox(PageSize.A4).withMargin(DocumentFactory.DEFAULT_PAGE_MARGINS);

		var grid = new Grid(testMargin, 2, pageBox.width(), 2, pageBox.height());

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(new Text("before grid"), render(grid)))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "splitToPagesWithNewPage.pdf");
	}

	@Test
	@DisplayName("grid with custom layouter")
	void gridWithCustomLayouter() {
		var pageBox = PageBox.asPageBox(PageSize.A4).withMargin(DocumentFactory.DEFAULT_PAGE_MARGINS);
		var grid = new Grid(testMargin, 4, (pageBox.width() / 2) - 20f, 4, pageBox.height() / 2);

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(new Text("before grid"), render(grid, new HorizontalSpaceBetweenCellsLayouter())))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "splitToPagesWithCustomLayout.pdf");
	}

	@Test
	@DisplayName("grid with custom layouter and grid fits to page")
	void gridWithCustomLayouterAndGridFitsToPage() {
		var pageBox = PageBox.asPageBox(PageSize.A4).withMargin(DocumentFactory.DEFAULT_PAGE_MARGINS);
		var grid = new Grid(testMargin, 2, (pageBox.width() / 2) - 20f, 4, pageBox.height() / 2);

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(new Text("before grid"), render(grid, new HorizontalSpaceBetweenCellsLayouter())))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "splitToPagesWithCustomLayoutFitsToPage.pdf");
	}

	@Test
	@DisplayName("grid must split on multiple pages as poster")
	void gridMustSplitOnMultiplePagesAsPoster() {
		var pageBox = PageBox.asPageBox(PageSize.A4).withMargin(DocumentFactory.DEFAULT_PAGE_MARGINS);

		var columnsPerPage = 3;
		var rowsPerPage = 3;
		var grid = new Grid(testMargin, 2 * columnsPerPage, pageBox.width() / columnsPerPage, 2 * rowsPerPage, pageBox.height() / rowsPerPage);

		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(new Text("something on this page will move the start down"), render(grid, new HorizontalSpaceBetweenCellsLayouter())))
			.build())
			.expectRendering()
			.matchesResource(getClass(), "splitCellGroupsToPages.pdf");
	}

	private RenderGrid<String> render(Grid grid) {
		return render(grid, new NoSpaceBetweenCellsLayouter());
	}

	private RenderGrid<String> render(Grid grid, CellLayout layouter) {
		return RenderGrid.<String>builder()
			.grid(grid)
			.cellBoxDecorator(GridCellDecorator.renderBorder())
			.renderBoxDecorator(GridCellDecorator.renderBorder())
			.layouter(layouter)
			.contentLookup(new CellAsStringLookup())
			.contentRenderer(new RenderString())
			.build();
	}

	private static class CellAsStringLookup implements CellContentLookup<String> {
		@Override
		public Optional<String> get(Cell cell) {
			return Optional.of(cell.column() + ":" + cell.row());
		}
	}

	private static class RenderString implements CellContentConsumer<String> {
		@Override
		public void fillCell(ColumnText columnText, String s) {
			columnText.addElement(new Phrase(s));
		}
	}

}