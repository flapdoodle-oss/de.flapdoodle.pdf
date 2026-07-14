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
package de.flapdoodle.pdf.render.table;

import org.openpdf.text.pdf.ColumnText;
import de.flapdoodle.pdf.tables.Table;
import de.flapdoodle.pdf.types.FloatArray;
import de.flapdoodle.pdf.types.Region;

public interface RegionColumnRenderer {
	@Deprecated
	default Status render(ColumnText column, Table table, Region region) {
		return render(column, table, TableAttributes.defaults(), region);
	}

	/**
	 * column text works this way:
	 *   you add stuff to it, and it will do nothing until you call
	 *   column.go()
	 *
	 * bc we can not prevent this from inside, but the renderer should only prepare
	 * stuff to render, you must not commit rendering to column from inside by calling
	 *   column.go()
	 */
	Status render(ColumnText column, Table table, TableAttributes tableAttributes, Region region);

	record Status(
		int lastVisibleRow,
		FloatArray columnWidths,
		FloatArray rowHeights,
		float tableWidth,
		float tableHeight // can contain invisible parts bc of clipping
	) {
	}

}
