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
package de.flapdoodle.pdf.internals;

import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.pdf.ByteBuffer;
import org.openpdf.text.pdf.PdfLiteral;
import org.openpdf.text.pdf.PdfObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StaticPdfFileIdGenerator implements PdfFileIdGenerator {
	@Override
	public PdfObject generate() {
		return createInfoId(createDocumentId());
	}

	/**
	 * @see  PdfEncryption.createInfoId
	 */
	private PdfObject createInfoId(byte[] id)  {
		var infoId = id;
		var buf = new ByteBuffer(90);
		buf.append('[').append('<');
		for (int k=0; k<15; k++){
			buf.appendHex(infoId[k]);
		}
		buf.append('>').append('<');
		infoId = createDocumentId();
		for (int k=0; k<15; k++) {
			buf.appendHex(infoId[k]);
		}
		buf.append('>').append(']');
		return new PdfLiteral(buf.toByteArray());
	}

	/**
	 * @see PdfEncryption.createDocumentId()
	 */
	private byte[] createDocumentId() {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new ExceptionConverter(e);
		}

		var time = 0L;
		var mem = 10L;
		var s = time+"+"+mem+"+0";
		return md5.digest(s.getBytes());
	}
}
