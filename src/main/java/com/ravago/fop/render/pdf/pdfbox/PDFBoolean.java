/*
 * Copyright 2010 Jeremias Maerki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: PDFBoolean.java 1126 2010-09-20 13:53:19Z jeremias $ */

package com.ravago.fop.render.pdf.pdfbox;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.fop.pdf.PDFObject;

/**
 * Represents a boolean object.
 */
public class PDFBoolean extends PDFObject {

	private boolean value;

	/**
	 * Main constructor.
	 *
	 * @param value the boolean value
	 */
	public PDFBoolean(boolean value) {
		this.value = value;
	}

	/**
	 * Returns the boolean value
	 *
	 * @return the boolean value
	 */
	public boolean getValue() {
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int output(OutputStream stream) throws IOException {
		CountingOutputStream cout = new CountingOutputStream(stream);
		Writer writer = FopPdfUtils.getBufferedWriter(cout);
		if (hasObjectNumber()) {
			writer.write(getObjectID());
		}

		writer.write(Boolean.toString(getValue()));

		if (hasObjectNumber()) {
			writer.write("\nendobj\n");
		}

		writer.flush();
		return cout.getCount();
	}

}
