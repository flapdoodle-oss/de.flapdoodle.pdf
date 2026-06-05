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

import de.flapdoodle.pdf.types.FloatArray;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ColumnWeights {
	Optional<Float> get(int column);

	ColumnWeights EMPTY = it -> Optional.empty();

	static ColumnWeights fromMap(Map<Integer, Float> map) {
		return column -> Optional.ofNullable(map.get(column));
	}

	static ColumnWeights fromList(List<Float> weights) {
		return column -> Optional.ofNullable(weights.get(column));
	}

	static ColumnWeights fromList(FloatArray weights) {
		return column -> Optional.of(weights.get(column));
	}
}
