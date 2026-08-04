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

import de.flapdoodle.commons.checks.Preconditions;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public final class ColumnMapping {
	private static Map<Integer, Mapping> columnMap(List<Table> tables) {
		var offset = 0;
		var map = new LinkedHashMap<Integer, Mapping>();
		for (Table table : tables) {
			var entry = new Mapping(table, offset);
			for (int c : IntRange.until(0, table.columns())) {
				map.put(c + offset, entry);
			}
			offset = offset + table.columns();
		}
		return map;
	}

	private final List<Table> tables;
	private final Map<Integer, Mapping> map;

	public ColumnMapping(List<Table> tables) {
		Preconditions.checkArgument(!tables.isEmpty(), "not tables");
		this.tables = tables;
		this.map = columnMap(tables);
	}

	public TableAndColumn get(int column) {
		return Optional.ofNullable(map.get(column)).map(it ->
			new TableAndColumn(it.table, column - it.offset)
		).orElseThrow(() -> new IllegalArgumentException("no mapping found for "+column));
	}

	public TableAndCell get(Cell cell)  {
		return Optional.ofNullable(map.get(cell.column())).map(it ->
			new TableAndCell(it.table, new Cell(cell.column() - it.offset, cell.row()))
		).orElseThrow(() -> new IllegalArgumentException("no mapping found for "+cell));
	}

	int columns() {
		return map.size();
	}

	record Mapping(
		Table table,
		int offset
	) {
	}

	public record TableAndColumn(
		Table table,
		int column
	) {
		public <R> R call(BiFunction<Table, Integer, R> action) {
			return action.apply(table, column);
		}
	}

	public record TableAndCell(
		Table table,
		Cell cell
	) {
		public <R> R call(BiFunction<Table, Cell, R> action) {
			return action.apply(table, cell);
		}
	}

}
