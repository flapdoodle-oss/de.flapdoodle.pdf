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

import de.flapdoodle.pdf.grid.Grid;
import de.flapdoodle.pdf.render.table.TableRenderer;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.Range;

import java.util.List;
import java.util.Optional;

public interface PosterSplitter {
	Split split(
		TableRenderer tableRenderer,
		Grid grid,
		Table table
	);

	record Split(
		Table table,
		List<Part> part
	) {}

	record Part(
		int column,
		Range range,
		Optional<Float> width
	) {}
}
