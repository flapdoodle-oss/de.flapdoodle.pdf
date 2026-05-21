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
package de.flapdoodle.pdf.types;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record Region(
	Range columns,
	Range rows
) {
	public Region(int columnStart, int columnEnd, int rowStart, int rowEnd) {
		this(new Range(columnStart, columnEnd), new Range(rowStart, rowEnd));
	}

	public boolean contains(Region other) {
		return columns.contains(other.columns) && rows.contains(other.rows);
	}

	public Region firstColumn() {
		return untilColumn(columns.start());
	}

	public Optional<Region> fromColumn(int column) {
		if (column <= columns.end()) {
			return Optional.of(new Region(new Range(column, columns.end()), rows));
		}
		return Optional.empty();
	}

	public Region untilColumn(int column) {
		Preconditions.checkArgument(column <= columns.end(), "column(%s) > end(%s)", column, columns.end());
		return new Region(new Range(columns.start(), column), rows);
	}

	public Region withColumns(Range columnRange) {
		Preconditions.checkArgument(columns.contains(columnRange), "columns(%s) not part of this region %s", columnRange, this);
		return new Region(columnRange, rows);
	}

	public Region untilRow(int row) {
		Preconditions.checkArgument(row <= rows.end(), "row(%s) > end(%s)", row, rows.end());
		return new Region(columns, new Range(rows.start(), row));
	}

	public Optional<Region> fromRow(int row) {
		if (row <= rows.end()) {
			return Optional.of(new Region(columns, new Range(row, rows.end())));
		}
		return Optional.empty();
	}

	public Optional<Region> nextRegionWithSameHeight(Region outer) {
		return (this.columns.end() + 1 <= outer.columns.end())
			? Optional.of(new Region(this.columns.end() + 1, outer.columns.end(), this.rows.start(), this.rows.end()))
			: Optional.empty();
	}

	public Optional<Region> regionUnderThisWithSameStartColumn(Region outer) {
		return (this.rows.end() + 1 <= outer.rows.end())
			? Optional.of(new Region(this.columns.start(), outer.columns.end(), this.rows.end() + 1, outer.rows.end()))
			: Optional.empty();
	}

	public void forEach(BiConsumer<Integer, Integer> action) {
		rows.forEach(r ->
			columns.forEach(c ->
				action.accept(c, r)
			)
		);
	}

	public <T> List<T> map(BiFunction<Integer, Integer, T> generator) {
		List<T> ret = new ArrayList<>();
		forEach((c, r) -> ret.add(generator.apply(c, r)));
		return Collections.unmodifiableList(ret);
	}
}
