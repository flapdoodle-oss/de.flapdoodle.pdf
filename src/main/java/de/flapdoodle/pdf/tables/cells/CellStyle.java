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
package de.flapdoodle.pdf.tables.cells;

import com.lowagie.text.Font;
import de.flapdoodle.pdf.Optionals;
import org.immutables.value.Value;

import java.awt.*;
import java.util.Optional;

@Value.Immutable(singleton = true)
public interface CellStyle {
	Optional<Color> backgroundColor();

	Optional<Font> font();

	Optional<BorderStyle> borderTop();

	Optional<BorderStyle> borderLeft();

	Optional<BorderStyle> borderRight();

	Optional<BorderStyle> borderBottom();

	Optional<HorizontalAlignment> horizontalAlignment();

	static ImmutableCellStyle empty() {
		return ImmutableCellStyle.of();
	}

	default CellStyle overrideWith(CellStyle specific) {
		if (specific != null) {
			return ImmutableCellStyle.copyOf(this)
				.withBackgroundColor(Optionals.firstOf(specific.backgroundColor(), this.backgroundColor()))
				.withFont(Optionals.firstOf(specific.font(), this.font()))
				.withBorderTop(Optionals.firstOf(specific.borderTop(), this.borderTop()))
				.withBorderLeft(Optionals.firstOf(specific.borderLeft(), this.borderLeft()))
				.withBorderRight(Optionals.firstOf(specific.borderRight(), this.borderRight()))
				.withBorderBottom(Optionals.firstOf(specific.borderBottom(), this.borderBottom()))
				.withHorizontalAlignment(Optionals.firstOf(specific.horizontalAlignment(), this.horizontalAlignment()));
		}
		return this;
	}


	default CellStyle withBorder(BorderStyle border) {
		return ImmutableCellStyle.copyOf(this)
			.withBorderLeft(border)
			.withBorderRight(border)
			.withBorderTop(border)
			.withBorderBottom(border);
	}

	default boolean sameStyleForAllBorders() {
		return borderTop().equals(borderLeft())
			&& borderLeft().equals(borderBottom())
			&& borderBottom().equals(borderRight());
	}

	default Optional<BorderStyle> border() {
		if (sameStyleForAllBorders()) {
			return borderTop();
		}
		return Optional.empty();
	}

}
