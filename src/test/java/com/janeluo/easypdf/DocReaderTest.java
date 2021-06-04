package com.janeluo.easypdf;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DocReaderTest
{
	@Test
	public void test() {
		try {
			InputStream doc_stream =
					new FileInputStream("tests/test.doc");
			OutputStream xml_stream =
					new FileOutputStream("tests/test.xml");
			OutputStream json_stream =
					new FileOutputStream("tests/test.json");

			DocReader reader = new DocReader();
			reader.setAutoTitle(true);
			reader.ignoreBlankPara(true);
			reader.read(doc_stream, xml_stream, json_stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
