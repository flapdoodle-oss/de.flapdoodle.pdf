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
