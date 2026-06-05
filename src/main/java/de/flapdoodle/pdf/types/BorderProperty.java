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

import de.flapdoodle.pdf.Optionals;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable(singleton = true)
public interface BorderProperty<T> {
	Optional<T> left();
	Optional<T> top();
	Optional<T> right();
	Optional<T> bottom();

	default boolean sameForAllBorders() {
		return top().equals(left())
			&& left().equals(bottom())
			&& bottom().equals(right());
	}

	default Optional<T> value() {
		if (sameForAllBorders()) {
			return top();
		}
		return Optional.empty();
	}

	default BorderProperty<T> overrideWith(BorderProperty<T> padding) {
		return ImmutableBorderProperty.copyOf(this)
			.withLeft(Optionals.firstOf(padding.left(), this.left()))
			.withTop(Optionals.firstOf(padding.top(), this.top()))
			.withRight(Optionals.firstOf(padding.right(), this.right()))
			.withBottom(Optionals.firstOf(padding.bottom(), this.bottom()));
	}

	static <T> ImmutableBorderProperty.Builder<T> builder() {
		return ImmutableBorderProperty.builder();
	}

	static <T> ImmutableBorderProperty<T> of() {
		return ImmutableBorderProperty.of();
	}

	static <T> ImmutableBorderProperty<T> of(T all) {
		return of(all, all, all, all);
	}
	
	static <T> ImmutableBorderProperty<T> of(T left, T top, T right, T bottom) {
		return ImmutableBorderProperty.<T>builder()
			.left(left)
			.top(top)
			.right(right)
			.bottom(bottom)
			.build();
	}

}
