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
package de.flapdoodle.pdf.blocks.split;

import de.flapdoodle.commons.checks.Preconditions;
import de.flapdoodle.pdf.render.table.TableAttributes;
import de.flapdoodle.pdf.tables.ColumnWeights;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.tables.TableColumns;
import de.flapdoodle.pdf.tables.cells.CellStyle;
import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.tables.cells.HeaderStyles;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.Region;

import java.util.Optional;

public class SplittedTable implements Table {

	private final Table source;
	private final TableAttributes tableAttributes;
	private final VirtualCursor cursor;
	
	private final Optional<TableColumns> header;
	private final SplitCellStyle styles;
	private final SplitColumnWeights columnWeights;


	/**
	 * [header keyColumns][header region]
	 * [keyColumns       ][region]
	 */
	public SplittedTable(
		Table source,
		TableAttributes tableAttributes,
		Region region,
		boolean repeatHeader,
		int keyColumns
	) {
		this.source = source;
		this.tableAttributes = tableAttributes;
		Preconditions.checkArgument(!repeatHeader || source.header().isPresent(), "source table does not have any header: %s", source);
		Region sourceMaxRegion = source.maxRegion();
		Preconditions.checkArgument(keyColumns == 0 || sourceMaxRegion.columns().contains(keyColumns), "key columns exceed source range");

		this.cursor=new VirtualCursor(region, keyColumns);

		this.header = repeatHeader
			? source.header().map(it -> new SplitHeader(it, cursor))
			: Optional.empty();
		this.styles = new SplitCellStyle(source.styles(), cursor);
		this.columnWeights = new SplitColumnWeights(source.columnWeights(), cursor);
	}

	public TableAttributes tableAttributes() {
		return tableAttributes;
	}
	@Override
	public Optional<TableColumns> header() {
		return header;
	}

	@Override
	public int columns() {
		return cursor.columns();
	}
	@Override
	public int rows() {
		return cursor.rows();
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
		return source.get(new Cell(cursor.realColumn(cell.column()), cursor.realRow(cell.row())));
	}

	private static final class SplitHeader implements TableColumns {
		private final TableColumns source;
		VirtualCursor cursor;
		private final SplitHeaderStyles headerStyles;

		private SplitHeader(
			TableColumns source,
			VirtualCursor cursor
		) {
			this.source = source;
			this.cursor = cursor;
			this.headerStyles = new SplitHeaderStyles(source.styles(), cursor);
		}

		@Override
		public Optional<String> get(int column) {
			return source.get(cursor.realColumn(column));
		}

		@Override
		public int columns() {
			return cursor.columns();
		}
		
		@Override
		public HeaderStyles styles() {
			return headerStyles;
		}
	}

	private record VirtualCursor(
		Region region,
		int keyColumns
	) {
		public int realColumn(int column) {
			return column < keyColumns ? column : region.columns().start() + column - keyColumns;
		}

		public int realRow(int row) {
			return region.rows().start() + row;
		}

		public int columns() {
			return keyColumns + region.columns().size();
		}
		public int rows() {
			return region.rows().size();
		}
	}

	private static final class SplitHeaderStyles implements HeaderStyles {

		private final HeaderStyles styles;
		private final VirtualCursor cursor;

		public SplitHeaderStyles(HeaderStyles styles, VirtualCursor cursor) {
			this.styles = styles;
			this.cursor = cursor;
		}
		@Override
		public CellStyle get(int column) {
			return styles.get(cursor.realColumn(column));
		}
	}

	private class SplitCellStyle implements CellStyles {
		private final CellStyles source;
		private final VirtualCursor cursor;
		public SplitCellStyle(CellStyles source, VirtualCursor cursor) {
			this.source = source;
			this.cursor = cursor;
		}
		@Override
		public CellStyle get(Cell cell) {
			return source.get(new Cell(cursor.realColumn(cell.column()), cursor.realRow(cell.row())));
		}
	}

	private class SplitColumnWeights implements ColumnWeights {
		private final ColumnWeights source;
		private final VirtualCursor cursor;
		public SplitColumnWeights(ColumnWeights source, VirtualCursor cursor) {
			this.source = source;
			this.cursor = cursor;
		}

		@Override
		public Optional<Float> get(int column) {
			return source.get(cursor.realColumn(column));
		}
	}
}
