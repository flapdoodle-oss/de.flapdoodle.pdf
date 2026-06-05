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

import de.flapdoodle.pdf.tables.cells.CellStyles;
import de.flapdoodle.pdf.types.Cell;
import de.flapdoodle.pdf.types.IntRange;
import de.flapdoodle.pdf.types.Region;

import java.util.Optional;

public interface Table {
	Optional<TableColumns> header();

	int columns();

	int rows();

	CellStyles styles();

	ColumnWeights columnWeights();

	Optional<String> get(Cell cell);

	default boolean isEmpty() {
		return rows() == 0;
	}

	default Region maxRegion() {
		return new Region(IntRange.until(0, columns()).asClosed(), IntRange.until(0, rows()).asClosed());
	}
}
