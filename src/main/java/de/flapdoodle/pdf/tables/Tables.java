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

import de.flapdoodle.pdf.types.Floats;
import de.flapdoodle.pdf.types.Range;

import java.util.Map;
import java.util.stream.Collectors;

public final class Tables {
	private Tables() {
	}

	public static Map<Integer, Float> columnWidths(Range columns, float tableWidth, ColumnWeights columnWeights) {
		if (columns.isEmpty()) throw new IllegalArgumentException("columns==0");
		if (tableWidth <= 0f) throw new IllegalArgumentException("tableWidth <= 0 ("+ tableWidth +")");

		var allColumnWeigts = columns.asRange()
			.stream()
			.collect(Collectors.toMap(it -> it, it ->  columnWeights.get(it).orElse(1f)));

		var weightSum = Floats.sum(allColumnWeigts.values());
		var absoluteWidths = allColumnWeigts.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey,
				it -> (it.getValue() * tableWidth) / weightSum));

		return absoluteWidths;
	}

}
