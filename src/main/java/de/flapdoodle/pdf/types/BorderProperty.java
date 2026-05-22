package de.flapdoodle.pdf.types;

import de.flapdoodle.pdf.Optionals;
import de.flapdoodle.pdf.tables.cells.BorderStyle;
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
