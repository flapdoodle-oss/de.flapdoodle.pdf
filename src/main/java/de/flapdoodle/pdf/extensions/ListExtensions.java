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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ListExtensions {

	public static <T, D> List<D> mapIndexed(List<T> list, BiFunction<Integer, T, D> mapper) {
		List<D> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			T element = list.get(i);
			result.add(mapper.apply(i, element));
		}
		return List.copyOf(result);
	}

	public static <T> void forEachIndexed(List<T> list, BiConsumer<Integer, T> action) {
		for (int i = 0; i < list.size(); i++) {
			T element = list.get(i);
			action.accept(i, element);
		}
	}
}
