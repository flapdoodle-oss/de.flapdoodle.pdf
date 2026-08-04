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

import de.flapdoodle.commons.checks.Preconditions;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

public sealed abstract class IntRange implements Iterable<Integer> {
	protected final int start;
	protected final int end;

	protected IntRange(int start, int end) {
		this.start = start;
		this.end = end;
	}
	public int start() {
		return start;
	}

	public int end() {
		return end;
	}

	public abstract boolean isEmpty();

	public abstract int size();

	protected abstract boolean hasNext(int current);

	public boolean contains(int value) {
		return !isEmpty() && start <= value && hasNext(value);
	}

	public abstract IntStream stream();

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<>() {
			private int current = start;

			@Override
			public boolean hasNext() {
				return IntRange.this.hasNext(current);
			}

			@Override
			public Integer next() {
				if (!hasNext()) throw new NoSuchElementException();
				return current++;
			}
		};
	}

	public float[] mapToFloat(Function<Integer, Float> mapper) {
		float[] mapped = new float[size()];
		for (int i = 0; i < size(); i++) {
			mapped[i] = mapper.apply(start() + i);
		}
		return mapped;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		IntRange that = (IntRange) o;
		return start == that.start && end == that.end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	public static final class Open extends IntRange {
		public static final Open EMPTY = new Open();

		// empty
		private Open() {
			super(0, 0);
		}

		private Open(int start, int end) {
			super(start, end);
			Preconditions.checkArgument(start < end, "%s must be > %s", start, end);
		}

		@Override
		public boolean isEmpty() {
			return !(start < end);
		}

		@Override
		public int size() {
			return isEmpty() ? 0 : end - start;
		}

		@Override
		protected boolean hasNext(int current) {
			return current < end;
		}

		public boolean contains(Open other) {
			return !isEmpty() && !other.isEmpty() && start <= other.start && end >= other.end;
		}

		@Override
		public IntStream stream() {
			return isEmpty() ? IntStream.empty() : IntStream.range(start, end);
		}

		public Closed asClosed() {
			return isEmpty()
				? Closed.EMPTY
				: Closed.of(this.start, this.end - 1);
		}

		@Override
		public String toString() {
			return "Open{" +
				"start=" + start +
				", end=" + end +
				'}';
		}
		public static Open until(int start, int end) {
			return new Open(start, end);
		}
	}

	public static final class Closed extends IntRange {

		public static final Closed EMPTY = new Closed();

		private Closed() {
			super(0, -1);
		}

		private Closed(int start, int end) {
			super(start, end);
			Preconditions.checkArgument(start <= end, "%s must be less than or equal to %s", start, end);
		}

		@Override
		public boolean isEmpty() {
			return !(start <= end);
		}

		@Override
		public int size() {
			return isEmpty() ? 0 : end - start + 1;
		}

		@Override
		protected boolean hasNext(int current) {
			return current <= end;
		}

		public boolean contains(Closed other) {
			return !isEmpty() && !other.isEmpty() && start <= other.start && end >= other.end;
		}

		@Override
		public IntStream stream() {
			return isEmpty() ? IntStream.empty() : IntStream.rangeClosed(start, end);
		}

		public Open asOpen() {
			return isEmpty()
				? Open.EMPTY
				: Open.until(this.start, this.end + 1);
		}

		@Override
		public String toString() {
			return "Closed{" +
				"start=" + start +
				", end=" + end +
				'}';
		}
		
		public static Closed of(int start, int end) {
			return new Closed(start, end);
		}
	}

	public static IntRange.Closed to(int start, int end) {
		return new IntRange.Closed(start, end);
	}

	public static IntRange.Closed at(int start) {
		return to(start, start);
	}


	public static IntRange.Open until(int start, int end) {
		return new IntRange.Open(start, end);
	}

}
