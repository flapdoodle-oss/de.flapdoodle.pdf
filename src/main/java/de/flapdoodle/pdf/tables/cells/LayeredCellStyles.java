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
package de.flapdoodle.pdf.tables.cells;

import de.flapdoodle.pdf.types.Cell;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable(singleton = true)
public abstract class LayeredCellStyles implements CellStyles {
	@Value.Default
	protected CellStyle defaultStyle() {
		return CellStyle.empty();
	}
	@Value.Default
	protected Map<Integer, CellStyle> columnStyle() {
		return Map.of();
	}
	@Value.Default
	protected Map<Integer, CellStyle> rowStyle() {
		return Map.of();
	}
	@Value.Default
	protected Map<Cell, CellStyle> cellStyle() {
		return Map.of();
	}

	@Override
	public CellStyle get(Cell cell) {
		return defaultStyle()
			.overrideWith(columnStyle().get(cell.column()))
			.overrideWith(rowStyle().get(cell.row()))
			.overrideWith(cellStyle().get(cell));
	}

	public LayeredCellStyles withDefault(CellStyle style) {
		return ImmutableLayeredCellStyles.copyOf(this)
			.withDefaultStyle(style);
	}

	public LayeredCellStyles forRow(int row, CellStyle style) {
		if (rowStyle().containsKey(row)) throw new IllegalArgumentException("style for row "+row+" already set");

		return ImmutableLayeredCellStyles.builder()
			.from(this)
			.putRowStyle(row, style)
			.build();
	}

	public LayeredCellStyles forColumn(int column, CellStyle style) {
		if (columnStyle().containsKey(column)) throw new IllegalArgumentException("style for column "+column+" already set");

		return ImmutableLayeredCellStyles.builder()
			.from(this)
			.putColumnStyle(column, style)
			.build();
	}

	public LayeredCellStyles forCell(Cell cell, CellStyle style) {
		if (cellStyle().containsKey(cell)) throw new IllegalArgumentException("style for cell "+cell+" already set");

		return ImmutableLayeredCellStyles.builder()
			.from(this)
			.putCellStyle(cell, style)
			.build();
	}

	public static LayeredCellStyles empty() {
		return ImmutableLayeredCellStyles.of();
	}
}
