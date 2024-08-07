package com.ravago.fop.render.pdf.pdfbox;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.fop.pdf.PDFDocument;

public class FopPdfUtils {

	public static Writer getBufferedWriter(OutputStream out) {
		try {
			return new BufferedWriter(new OutputStreamWriter(out, PDFDocument.ENCODING));
		} catch (UnsupportedEncodingException var2) {
			throw new Error("JVM doesn't support " + PDFDocument.ENCODING + " encoding!");
		}
	}
}
