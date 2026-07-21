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

import org.openpdf.text.Chunk;
import org.openpdf.text.Font;
import org.openpdf.text.Phrase;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class PhraseElement implements ElementSupplier<Phrase> {

	@Value.Default
	protected float leading() { return Float.NaN; }
	protected abstract String text();
	protected abstract Optional<Font> font();

	@Override
	public Phrase create() {
		return new Phrase(leading(), text(), font().orElse(new Font()));
	}

	public static ImmutablePhraseElement.Builder builder() {
		return ImmutablePhraseElement.builder();
	}

	public static ImmutablePhraseElement of(String text) {
		return builder()
			.text(text)
			.build();
	}

	public static ImmutablePhraseElement of(String text, Font font) {
		return builder().text(text).font(font).build();
	}

	public static ImmutablePhraseElement of(String text, Optional<Font> font) {
		return builder().text(text).font(font).build();
	}

	public static void setFont(Phrase phrase, Font font) {
		phrase.setFont(font);
		phrase.getChunks().forEach(chunk -> {
			if (chunk instanceof Chunk) {
				((Chunk) chunk).setFont(font);
			}
		});
	}
}
