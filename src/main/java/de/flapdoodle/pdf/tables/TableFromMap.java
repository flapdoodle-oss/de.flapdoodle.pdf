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
package de.flapdoodle.pdf.tables;

import de.flapdoodle.pdf.checks.Preconditions;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.types.Cell;
import org.immutables.value.Value;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

@Value.Immutable
public abstract class TableFromMap implements Table {
	public abstract Optional<TableColumns> header();

	@Value.Default
	public CellStyles styles() {
		return it -> CellStyle.empty();
	}

	@Value.Default
	public ColumnWeights columnWeights() {
		return ColumnWeights.EMPTY;
	}

	@Value.Default
	protected Map<Cell, String> cells() {
		return Map.of();
	}

	@Value.Derived
	public int columns() {
		return Math.max(
			cells().keySet().stream()
				.map(it -> it.column() + 1)
				.max(Comparator.naturalOrder())
				.orElse(0),
			header().map(TableColumns::columns).orElse(0)
		);
	}

	@Value.Derived
	public int rows() {
		return cells().keySet().stream()
			.map(it -> it.row() + 1)
			.max(Comparator.naturalOrder())
			.orElse(0);
	}

	@Value.Check
	protected void check() {
		boolean moreColumnsThanHeaderColumns = header().isPresent() && columns() > header().get().columns();
		Preconditions.checkArgument(!moreColumnsThanHeaderColumns, "more columns in cells than in headerRows");
	}

	@Override
	public Optional<String> get(Cell cell) {
		return Optional.ofNullable(cells().get(cell));
	}

	public static ImmutableTableFromMap.Builder builder() {
		return ImmutableTableFromMap.builder();
	}
}
