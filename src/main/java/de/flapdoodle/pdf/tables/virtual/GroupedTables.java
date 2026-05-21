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
package de.flapdoodle.pdf.tables.virtual;

import com.google.common.base.Preconditions;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumns;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.tables.cells.HeaderStyles;
import de.flapdoodle.pdf.types.Cell;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GroupedTables implements Table {
	private final List<Table> tables;

	private final int columns;
	private final int rows;
	private final ColumnMapping columnMapping;
	private final CellStyles styles;
	private final ColumnWeightsWithColumnMapping columnWeights;
	private final Optional<TableColumns> header;

	public GroupedTables(List<Table> tables) {
		Preconditions.checkArgument(!tables.isEmpty(), "no tables");
		int tableRowsSet = tables.stream().map(Table::rows).collect(Collectors.toSet()).size();
		Preconditions.checkArgument(tableRowsSet == 1, "tables with different rows: %s", tables);
		int tablesWithOrWithoutHeader = tables.stream().map(it -> it.header().isPresent()).collect(Collectors.toSet()).size();
		Preconditions.checkArgument(tablesWithOrWithoutHeader == 1, "tables with or without header: %s", tables);

		this.tables = tables;
		this.columns = tables.stream().mapToInt(Table::columns).sum();
		this.rows = tables.get(0).rows();
		this.columnMapping = new ColumnMapping(tables);
		this.styles = new CellStylesWithColumnMapping(columnMapping);
		this.columnWeights = new ColumnWeightsWithColumnMapping(columnMapping);
		this.header = (tables.stream().anyMatch(it -> it.header().isPresent()))
			? Optional.of(new TableColumnsWithColumnMapping(columnMapping))
			: Optional.empty();
	}

	@Override
	public Optional<TableColumns> header() {
		return header;
	}
	@Override
	public int columns() {
		return columns;
	}
	@Override
	public int rows() {
		return rows;
	}

	@Override
	public CellStyles styles() {
		return styles;
	}

	@Override
	public ColumnWeights columnWeights() {
		return columnWeights;
	}

	@Override
	public Optional<String> get(Cell cell) {
		return columnMapping.get(cell).call(Table::get);
	}

	record HeaderStylesWithColumnMapping(
		ColumnMapping columnMapping
	) implements HeaderStyles {
		@Override
		public CellStyle get(int column) {
			return columnMapping.get(column)
				.call((table, real) -> table.header()
					.map(it -> it.styles().get(real)))
				.orElseThrow(() -> new IllegalArgumentException("no styles for column " + column + "found in " + columnMapping));
		}
	}

	static final class TableColumnsWithColumnMapping implements TableColumns {
		private final ColumnMapping columnMapping;
		private final HeaderStylesWithColumnMapping styles;

		TableColumnsWithColumnMapping(
			ColumnMapping columnMapping
		) {
			this.columnMapping = columnMapping;
			this.styles = new HeaderStylesWithColumnMapping(columnMapping);
		}

		@Override
		public Optional<String> get(int column) {
			return columnMapping.get(column)
				.call((table, real) -> table.header().map(it -> it.get(real)))
				.orElseThrow(() -> new IllegalArgumentException("no styles for column " + column + "found in " + columnMapping));
		}
		@Override
		public int columns() {
			return columnMapping.columns();
		}
		@Override
		public HeaderStyles styles() {
			return styles;
		}
	}

	record CellStylesWithColumnMapping(
		ColumnMapping columnMapping
	) implements CellStyles {
		@Override
		public CellStyle get(Cell cell) {
			return columnMapping.get(cell).call((table, real) -> table.styles().get(real));
		}
	}

	record ColumnWeightsWithColumnMapping(
		ColumnMapping columnMapping
	) implements ColumnWeights {
		@Override
		public Optional<Float> get(int column) {
			return columnMapping.get(column).call((table, real) -> table.columnWeights().get(real));
		}
	}
}
