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
package de.flapdoodle.pdf.grid.tablesplitter.posterize;

import de.flapdoodle.pdf.extensions.MapExtensions;
import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.grid.TableSplitter;
import de.flapdoodle.pdf.pages.PagePosition;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;

import java.util.*;

public record SplitTableIntoPoster(
	TableRenderer tableRenderer,
	PosterSplitter posterSplitter
) implements TableSplitter {
	public SplitTableIntoPoster(TableRenderer tableRenderer) {
		this(tableRenderer, new PosterSplitWithRepeatingFirstColumn());
	}

	@Override
	public List<GridCellContent> split(Grid grid, List<Table> tables) {
		var result = new ArrayList<GridCellContent>();

		var currentGridLine = 0;
		for (Table src : tables) {
			PosterSplitter.Split split = posterSplitter.split(tableRenderer, grid, src);
			var table = split.table();
			var columnRangeList = split.part();
			var columnRanges = MapExtensions.indexedBy(columnRangeList, PosterSplitter.Part::column);

			Optional<Region> maxRegion = Optional.of(table.maxRegion());

			while (maxRegion.isPresent()) {
				var lineRegions = regions(grid, columnRanges, maxRegion.get(), currentGridLine, table);
				var minLastLine = minimalLastLine(lineRegions);

				result.addAll(lineRegions.stream().map(it -> {
					return new TableSplitter.GridCellContent(
						it.first,
						table,
						it.second.untilRow(minLastLine),
						Optional.ofNullable(columnRanges.get(it.first.column())).flatMap(PosterSplitter.Part::width),
						Optional.empty()
          );
				}).toList());

				maxRegion = maxRegion.get().fromRow(minLastLine + 1);
				currentGridLine++;
			}
		}
		return List.copyOf(result);
	}

	private List<Pair<Cell, Region>> regions(
		Grid grid,
		Map<Integer, PosterSplitter.Part> columnRanges,
		Region maxRegion,
		int currentGridLine,
		Table table
	) {
		var lineRegions = new ArrayList<Pair<Cell, Region>>();

		for (int gridColumn : IntRange.until(0, grid.columns())) {
			Optional.ofNullable(columnRanges.get(gridColumn)).ifPresent(columnRange -> {
				var region = maxRegion.withColumns(columnRange.range());
				var currentCell = new Cell(gridColumn, currentGridLine);
				var status = tableRenderer.render(
					table,
					region,
					grid.innerDimension(currentCell).pageBoxAt(PagePosition.ZERO)
				);
				var clippedRegion = region.untilRow(status.lastVisibleRow());
				lineRegions.add(new Pair<>(currentCell, clippedRegion));
			});
		}
		return lineRegions;
	}

	private int  minimalLastLine(List<Pair<Cell, Region>> lineRegions) {
		return (lineRegions.stream().map(it -> it.second().rows().end())
			.min(Comparator.naturalOrder()).orElseThrow(() -> new IllegalArgumentException("there must be an minimum")));
	}

	record Pair<F,S>(F first, S second) {}
}
