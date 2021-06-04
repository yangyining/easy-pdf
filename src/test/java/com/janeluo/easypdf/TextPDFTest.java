package com.janeluo.easypdf;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TextPDFTest {

    @Test
    public void test() {
        try {
            File xmlfile = new File("tests/test.xml");
            File jsonfile = new File("tests/test.json");
            File pdffile = new File("tests/test.pdf");
            TextParser parser = new TextParser(
                    new FileInputStream(xmlfile),
                    new FileInputStream(jsonfile),
                    new FileOutputStream(pdffile));
            parser.genPdf();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
