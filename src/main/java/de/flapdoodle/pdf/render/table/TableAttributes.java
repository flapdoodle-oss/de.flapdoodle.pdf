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

import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable(singleton = true)
public abstract class TableAttributes {
	@Value.Default
	public TableWidth tableWidth() {
		return new TableWidth.Relative(100f);
	}

	@Value.Default
	public HorizontalAlignment  horizontalAlignment() {
		return HorizontalAlignment.CENTER;
	}

	@Value.Default
	public Map<Integer, Float> rowHeights() {
		return Map.of();
	}

	@Value.Auxiliary
	public Optional<Float> rowHeight(int row) {
		return Optional.ofNullable(rowHeights().get(row));
	}

	public static ImmutableTableAttributes defaults() {
		return ImmutableTableAttributes.of();
	}
}
