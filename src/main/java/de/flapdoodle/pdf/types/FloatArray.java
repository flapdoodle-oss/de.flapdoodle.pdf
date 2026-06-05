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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class FloatArray implements Iterable<Float> {
	private final float[] array;

	private FloatArray(float[] array) {
		this.array = array;
	}

	public int length() {
		return array.length;
	}

	public float get(int index) {
		return array[index];
	}
	
	public float[] array() {
		return Arrays.copyOf(array,  array.length);
	}

	public List<Float> asList() {
		return IntStream.range(0, array.length).mapToObj(i -> array[i]).toList();
	}

	public boolean allMatch(FloatPredicate predicate) {
		for (float value : array) {
			if (!predicate.test(value)) {
				return false;
			}
		}
		return true;
	}

	public int indexOf(FloatPredicate predicate) {
		return indexOf(0, predicate);
	}

	public int indexOf(int start, FloatPredicate predicate) {
		for  (int i = start; i < array.length; i++) {
			if (predicate.test(array[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public int lastIndexOf(FloatPredicate predicate) {
		return lastIndexOf(0, predicate);
	}

	public int lastIndexOf(int start, FloatPredicate predicate) {
		int lastMatch = -1;
		for  (int i = start; i < array.length; i++) {
			if (predicate.test(array[i])) {
				lastMatch = i;
			} else {
				return lastMatch;
			}
		}
		return lastMatch;
	}

	public FloatArray part(int start, int end) {
		return new FloatArray(Arrays.copyOfRange(array, start, end));
	}

	public FloatArray part(int start) {
		return part(start, array.length);
	}

	public FloatArray append(FloatArray other) {
		float[] all = new float[array.length + other.array.length];
		System.arraycopy(array, 0, all, 0, array.length);
		System.arraycopy(other.array, 0, all, array.length, other.array.length);
		return new  FloatArray(all);
	}
	
	public float sum() {
		float sum = 0;
		for (float value : array) {
			sum += value;
		}
		return sum;
	}

	public int indexOfSumFitting(float max) {
		int index = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < max) {
				index = i;
				max = max - array[i];
			}
		}
		return index;
	}

	@Override
	public Iterator<Float> iterator() {
		return new Iterator<Float>() {
			private int index = 0;
			@Override
			public boolean hasNext() {
				return index < array.length;
			}
			@Override
			public Float next() {
				return array[index++];
			}
		};
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		FloatArray that = (FloatArray) o;
		return Objects.deepEquals(array, that.array);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(array);
	}

	@Override
	public String toString() {
		return "FloatArray{" +
			"array=" + Arrays.toString(array) +
			'}';
	}

	public static FloatArray from(float ... array) {
		return new FloatArray(Arrays.copyOf(array, array.length));
	}

	public static FloatArray from(List<Float> list) {
		float[] array = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return new FloatArray(array);
	}

}
