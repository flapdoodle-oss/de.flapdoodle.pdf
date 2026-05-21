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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Range {
	public static final Range EMPTY = new Range(0, 0, true);

	private final int start;
	private final int end;
	private final boolean isEmpty;
	private Range(int start, int end, boolean isEmpty) {
		this.start = start;
		this.end = end;
		this.isEmpty = isEmpty;
	}

	public Range(int start, int end) {
		this(start, end, false);

		if (start > end) throw new IllegalArgumentException("start($start) > end(${end})");
	}

	public int start() {
		return start;
	}

	public int end() {
		return end;
	}

	public boolean isEmpty() {
		return isEmpty;
	}
	public int count() {
		return isEmpty ? 0 : (end - start) + 1;
	}

	public IntRange asRange() {
		return isEmpty ? IntRange.EMPTY : new IntRange(start, end);
	}

	boolean contains(Range other) {
		return isEmpty ? false : start <= other.start && end >= other.end;
	}

	boolean contains(int value) {
		return isEmpty ? false : start <= value && value <= end;
	}


//	fun mapToFloat(supplier: (Int) -> Float): FloatArray {
//		return if (isEmpty) {
//			FloatArray(0)
//		} else
//			FloatArray((end - start) + 1) { supplier(it + start) }
//	}

	public float[] mapToFloat(Function<Integer, Float> mapper) {
		float[] result;
		if (isEmpty) {
			result = new float[0];
		} else {
			result = new float[((end - start) + 1)];
			for (int i = start; i <= end; i++) {
				result[i-start] = mapper.apply(i);
			}
		}
		return result;
	}

	public List<Float> mapToFloatList(Function<Integer, Float> mapper) {
		List<Float> result;
		if (isEmpty) {
			result = List.of();
		} else {
			result = new ArrayList<>(((end - start) + 1));
			for (int i = start; i <= end; i++) {
				result.add(mapper.apply(i));
			}
		}
		return result;
	}

	public void forEach(Consumer<Integer> action) {
		for (int x = start; x <= end; x++) {
			action.accept(x);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Range range = (Range) o;
		return start == range.start && end == range.end && isEmpty == range.isEmpty;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end, isEmpty);
	}

	public static Range at(int start) {
		return new Range(start, start);
	}

}
