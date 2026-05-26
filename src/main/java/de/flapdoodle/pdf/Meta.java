package de.flapdoodle.pdf;

import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

@Value.Immutable(singleton = true)
public interface Meta {
	Optional<String> title();
	Optional<String> subject();
	Optional<String> author();
	Optional<ZonedDateTime> creationDate();
	Optional<String> creator();
	Optional<String> producer();

	static ImmutableMeta.Builder builder() {
		return ImmutableMeta.builder();
	}

	static ImmutableMeta empty() {
		return ImmutableMeta.of();
	}
}
