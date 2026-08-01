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
package de.flapdoodle.pdf.elements;

import de.flapdoodle.pdf.checks.Preconditions;
import org.immutables.value.Value;
import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Value.Immutable
public abstract class PhraseElement implements ElementSupplier<Phrase> {

	@Value.Default
	protected float leading() {
		return Float.NaN;
	}

	protected abstract List<Part> parts();

	protected abstract Optional<Font> font();

	@Override
	public Phrase create() {
		Font font = font().orElse(new Font());
		Phrase phrase = new Phrase(leading(), null, font);
		parts().forEach(part -> {
			if (Objects.requireNonNull(part) instanceof Part.Text text) {
				phrase.add(new Chunk(text.value, text.font().orElse(font)));
			} else if (part instanceof Part.Tag tag) {
				phrase.add(new Chunk(tag.value, tag.font().orElse(font))
					.setGenericTag(tag.tag));
			}
		});
		return phrase;
	}

	public static ImmutablePhraseElement.Builder builder() {
		return ImmutablePhraseElement.builder();
	}

	public static ImmutablePhraseElement of(String text) {
		return builder()
			.addParts(new Part.Text(text))
			.build();
	}

	public static ImmutablePhraseElement of(String text, Font font) {
		return builder().addParts(new Part.Text(text)).font(font).build();
	}

	public static ImmutablePhraseElement of(String text, Optional<Font> font) {
		return builder().addParts(new Part.Text(text)).font(font).build();
	}

	public static void setFont(Phrase phrase, Font font) {
		phrase.setFont(font);
		phrase.getChunks().forEach(chunk -> {
			if (chunk instanceof Chunk) {
				((Chunk) chunk).setFont(font);
			}
		});
	}

	public sealed interface Part {
		record Text(String value, Optional<Font> font) implements Part {
			public Text {
				Preconditions.checkNotNull(value, "value must not be null");
				Preconditions.checkNotNull(font, "font must not be null");
			}
			public Text(String value) {
				this(value, Optional.empty());
			}
			public Text(String value, Font font) {
				this(value, Optional.of(font));
			}
		}

		record Tag(String value, String tag, Optional<Font> font) implements Part {
			public Tag {
				Preconditions.checkNotNull(value, "value must not be null");
				Preconditions.checkNotNull(tag, "tag must not be null");
				Preconditions.checkNotNull(font, "font must not be null");
			}
			public Tag(String value, String tag) {
				this(value, tag, Optional.empty());
			}
			public Tag(String value, String tag, Font font) {
				this(value, tag, Optional.of(font));
			}
		}
	}

}
