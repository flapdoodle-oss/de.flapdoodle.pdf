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
package de.flapdoodle.pdf;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DocumentFactoryAssert extends AbstractAssert<DocumentFactoryAssert, DocumentFactory> {
	protected DocumentFactoryAssert(DocumentFactory documentFactory) {
		super(documentFactory, DocumentFactoryAssert.class);
	}

	public static DocumentFactoryAssert assertThat(DocumentFactory documentFactory) {
		return new DocumentFactoryAssert(documentFactory);
	}

	public ResourceByteArrayAssert expectRendering() {
		return expectRendering(null);
	}

	public ResourceByteArrayAssert expectRendering(String writeToFileName) {
		try (ByteArrayOutputStream it = new ByteArrayOutputStream()) {
			actual.render(it);

			var content = it.toByteArray();

			if (writeToFileName != null) {
				try (FileOutputStream fos = new FileOutputStream(writeToFileName)) {
					fos.write(content);
				}
			}

			return ResourceByteArrayAssert.assertThat(content);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public AbstractThrowableAssert<?, ? extends Throwable> renderingThrows() {
		return Assertions.assertThatThrownBy(() -> {
			try (ByteArrayOutputStream it = new ByteArrayOutputStream()) {
				actual.render(it);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
