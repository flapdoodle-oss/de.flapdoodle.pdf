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
package de.flapdoodle.pdf.extensions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MapExtensions {
	public static <T, K> Map<K, T> indexedBy(Iterable<T> t, Function<T, K> keySelector) {
		return StreamSupport.stream(t.spliterator(), false)
			.collect(Collectors.toMap(
				keySelector,
				Function.identity(),
				(a, b) -> {
					throw new IllegalArgumentException("duplicate key " + keySelector.apply(a)+"("+a+") <-> "+keySelector.apply(b)+"("+b+")");
				},
				LinkedHashMap::new
			));
	}
}
