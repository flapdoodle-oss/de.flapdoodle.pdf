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

import org.assertj.core.api.AbstractByteArrayAssert;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.fail;

public class ResourceByteArrayAssert extends AbstractByteArrayAssert<ResourceByteArrayAssert> {
	protected ResourceByteArrayAssert(byte[] actual) {
		super(actual, ResourceByteArrayAssert.class);
	}

	public void matchesResource(Class<?> baseClass,String name) {
		var resourceURL = baseClass.getResource(name);

		try(var resourceStream = baseClass.getResourceAsStream(name)) {
			if (resourceStream == null) {
				fail("resource stream for " + baseClass + " / " + name + " not found" + persistToTempFile(actual, name, resourceURL));
				return;
			}

			byte[] expected = resourceStream.readAllBytes();

			Assertions.assertThat(Arrays.equals(actual, expected))
				.describedAs(() -> "expect binary match" + persistToTempFile(actual, name, resourceURL))
				.isTrue();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String persistToTempFile(byte[] data, String fileName, URL resourceURL) {
		try {
			var tempFile = Files.createTempFile("match", "--"+fileName);
			Files.write(tempFile, data);
			var resourceName = resourceURL != null ? resourceURL.toString()
																							 .replace("/target/test-classes/", "/src/test/resources/")
																							 .replace("file:/", "/") : null;
			return "\n" +
				",you can find the actual value in\n" +
				"   " + tempFile + " \n" +
				"for\n" +
				"   " + resourceName + "\n\n" +
				"diffpdf " + tempFile + " " + resourceName + "\n\n"+
				"cp " + tempFile + " " + resourceName + "\n\n"
				;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public static ResourceByteArrayAssert assertThat(byte[] actual) {
		return new ResourceByteArrayAssert(actual);
	}
}
