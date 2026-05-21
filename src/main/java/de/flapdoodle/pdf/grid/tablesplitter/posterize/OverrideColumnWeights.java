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

import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumns;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.types.Cell;

import java.util.List;
import java.util.Optional;

public record OverrideColumnWeights(
	Table delegate,
	ColumnWeights columnWeights
) implements Table {

	@Override
	public Optional<TableColumns> header() {
		return delegate.header();
	}
	@Override
	public int columns() {
		return delegate.columns();
	}
	@Override
	public int rows() {
		return delegate.rows();
	}
	@Override
	public CellStyles styles() {
		return delegate().styles();
	}

	@Override
	public Optional<String> get(Cell cell) {
		return delegate().get(cell);
	}

	public static Table using(Table table, List<Float> weights) {
		return new OverrideColumnWeights(table, ColumnWeights.fromList(weights));
	}

}
