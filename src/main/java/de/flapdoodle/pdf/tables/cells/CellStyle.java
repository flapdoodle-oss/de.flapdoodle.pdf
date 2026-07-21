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
import de.flapdoodle.pdf.types.BorderProperty;
import de.flapdoodle.pdf.types.ImmutableBorderProperty;
import org.immutables.value.Value;

import java.awt.*;
import java.util.Optional;

@Value.Immutable(singleton = true)
public interface CellStyle {
	Optional<Color> backgroundColor();

	Optional<Font> font();

	@Value.Default
	default BorderProperty<Float> padding() {
		return ImmutableBorderProperty.of();
	}

	@Value.Default
	default BorderProperty<BorderStyle> bordeStyle() {
		return ImmutableBorderProperty.of();
	}

	Optional<HorizontalAlignment> horizontalAlignment();

	Optional<VerticalAlignment> verticalAlignment();

	static ImmutableCellStyle empty() {
		return ImmutableCellStyle.of();
	}

	static ImmutableCellStyle noBorder() {
		return  ImmutableCellStyle.builder()
			.bordeStyle(BorderProperty.of(BorderStyle.noBorder()))
			.build();
	}

	default CellStyle overrideWith(CellStyle specific) {
		if (specific != null) {
			return ImmutableCellStyle.copyOf(this)
				.withBackgroundColor(Optionals.firstOf(specific.backgroundColor(), this.backgroundColor()))
				.withFont(Optionals.firstOf(specific.font(), this.font()))
				.withPadding(padding().overrideWith(specific.padding()))
				.withBordeStyle(bordeStyle().overrideWith(specific.bordeStyle()))
				.withHorizontalAlignment(Optionals.firstOf(specific.horizontalAlignment(), this.horizontalAlignment()))
				.withVerticalAlignment(Optionals.firstOf(specific.verticalAlignment(), this.verticalAlignment()));
		}
		return this;
	}


	default ImmutableCellStyle withBorder(BorderStyle border) {
		return ImmutableCellStyle.copyOf(this)
			.withBordeStyle(BorderProperty.of(border));
	}

	default ImmutableCellStyle withBorder(BorderProperty<BorderStyle> border) {
		return ImmutableCellStyle.copyOf(this)
			.withBordeStyle(border);
	}
}
