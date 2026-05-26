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
import java.util.List;
import java.util.stream.IntStream;

public class FloatArray {
	private final float[] array;

	private FloatArray(float[] array) {
		this.array = array;
	}

	public int length() {
		return array.length;
	}
	
	public float[] array() {
		return Arrays.copyOf(array,  array.length);
	}

	public List<Float> asList() {
		return IntStream.range(0, array.length).mapToObj(i -> array[i]).toList();
	}

	public static FloatArray from(List<Float> list) {
		float[] array = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return new FloatArray(array);
	}

	public static FloatArray from(float ... array) {
		return new FloatArray(Arrays.copyOf(array, array.length));
	}
}
