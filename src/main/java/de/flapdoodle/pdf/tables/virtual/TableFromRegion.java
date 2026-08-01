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

import de.flapdoodle.pdf.checks.Preconditions;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumns;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.tables.cells.HeaderStyles;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;

import java.util.Optional;

public class TableFromRegion implements Table {

	static Cell realCell(Region region, Cell cell) {
		return new Cell(
			cell.column() + region.columns().start(),
			cell.row() + region.rows().start()
		);
	}

	static int realColumn(Region region, int column) {
		return column + region.columns().start();
	}

	public static Table columns(TableFromRegion src, IntRange.Closed range) {
		return new TableFromRegion(src, src.maxRegion().withColumns(range));
	}

	private final Table table;
	private final Region region;
	private final Optional<TableColumns> header;
	private final ColumnWeights columnWeights;
	private final CellStyles cellStyles;

	public TableFromRegion(Table table, Region region) {
		Preconditions.checkArgument(table.maxRegion().contains(region), "region %s not inside %s", region, table.maxRegion());

		this.table = table;
		this.region = region;
		this.header = table.header().map(it -> new TableColumnsFromRegion(it, region));
		this.columnWeights = new ColumnWeightsFromRegion(table.columnWeights(), region);
		this.cellStyles = new CellStylesFromRegion(table.styles(), region);
	}

	@Override
	public Optional<TableColumns> header() {
		return header;
	}

	@Override
	public int columns() {
		return region.columns().size();
	}

	@Override
	public int rows() {
		return region.rows().size();
	}

	@Override
	public CellStyles styles() {
		return cellStyles;
	}

	@Override
	public ColumnWeights columnWeights() {
		return columnWeights;
	}

	@Override
	public Optional<String> get(Cell cell) {
		return table.get(realCell(region, cell));
	}
	
	record ColumnWeightsFromRegion(
		ColumnWeights columnWeights,
		Region region
	) implements ColumnWeights {
		@Override
		public Optional<Float> get(int column) {
			return columnWeights.get(realColumn(region, column));
		}
	}

	static class TableColumnsFromRegion implements TableColumns {
		private final TableColumns tableColumns;
		private final Region region;
		private final HeaderStylesFromRegion headerStyles;

		public TableColumnsFromRegion(
			TableColumns tableColumns,
			Region region
		) {
			this.tableColumns = tableColumns;
			this.region = region;
			this.headerStyles = new HeaderStylesFromRegion(tableColumns.styles(), region);
		}

		@Override
		public Optional<String> get(int column) {
			return tableColumns.get(realColumn(region, column));
		}
		@Override
		public int columns() {
			return region.columns().size();
		}
		@Override
		public HeaderStyles styles() {
			return headerStyles;
		}
	}
	
	record CellStylesFromRegion(
		CellStyles cellStyles,
		Region region
	) implements CellStyles {

		@Override
		public CellStyle get(Cell cell) {
			return cellStyles.get(realCell(region, cell));
		}
	}

	record HeaderStylesFromRegion(
		HeaderStyles headerStyles,
		Region region
	) implements HeaderStyles {

		@Override
		public CellStyle get(int column) {
			return headerStyles.get(realColumn(region, column));
		}
	}

}
