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
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import de.flapdoodle.pdf.Block;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.DocumentFactoryAssert;
import de.flapdoodle.pdf.columns.ColumnFactory;
import de.flapdoodle.pdf.pages.PageBox;
import de.flapdoodle.pdf.render.table.DefaultRegionColumnRenderer;
import de.flapdoodle.pdf.tables.TableFromMap;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Range;
import de.flapdoodle.pdf.types.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TableWithTitleRendererTest {
	@Test
	@DisplayName("render table with title")
	void renderTableWithTitle() {
		var table = TableFromMap.builder()
			.cells(new Region(new Range(0, 1), new Range(0, 10))
				.map(Cell::new)
				.stream()
				.collect(Collectors.toMap(it -> it, it -> it.column()+":"+it.row())))
			.build();

		var tableWithTitle = new TableWithTitle("title", table);

		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.blocks(List.of(
				new Delegate(tableWithTitle, it -> {
			assertThat(it)
				.describedAs("render height")
				.contains(201f);
				})
			)).build())
			.expectRendering()
			.matchesResource(getClass(), "tableWithTitle.pdf");
	}

	record Delegate(
		TableWithTitle tableWithTitle,
		Consumer<Optional<Float>> onResult
  ) implements Block {
		@Override
		public void render(Document document, Supplier<PdfContentByte> directContent) {
			var innerBox = PageBox.innerBox(document);
			var column = ColumnFactory.DEFAULT.create(directContent.get(), innerBox.withMargin(innerBox.width() / 4));

			var renderHeight = new TableWithTitleRenderer(new DefaultRegionColumnRenderer())
				.render(column, tableWithTitle);

			onResult.accept(renderHeight);

			column.go();
		}
	}

}