package de.flapdoodle.pdf.elements;

import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import org.immutables.value.Value;

import java.awt.*;
import java.util.Optional;

@Value.Immutable
public abstract class PhraseElement implements ElementSupplier<Phrase> {

	protected abstract String text();
	protected abstract Optional<Font> font();

	@Override
	public Phrase create() {
		return new Phrase(text(), font().orElse(new Font()));
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
}
