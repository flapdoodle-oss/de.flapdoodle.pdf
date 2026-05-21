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

import org.jspecify.annotations.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

public record IntRange(int start, int end) implements Iterable<Integer> {
	public static final IntRange EMPTY = new IntRange(1, 0);

	public <T> Stream<T> map(Function<Integer, ? extends T> mapper) {
		return stream().map(mapper);
	}
	public @NonNull Stream<Integer> stream() {
		return Stream.iterate(start, i -> i <= end, i -> i + 1);
	}

	public static IntRange until(int start, int excludedEnd) {
		return new IntRange(start, excludedEnd-1);
	}
	
	@Override
	public @NonNull Iterator<Integer> iterator() {
		return new Iterator<>() {
			private int current = start;

			@Override
			public boolean hasNext() {
				return current <= end;
			}

			@Override
			public Integer next() {
				if (!hasNext()) throw new NoSuchElementException();
				return current++;
			}
		};
	}
}
