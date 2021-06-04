/* TextPDF - generate PDF dynamically
 *
 * Copyright (c) 2015 Lucky Byte, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.janeluo.easypdf;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * 解析 XML 模板
 * <p>
 * 这个类负责解析 XML 模板，并组合 JSON 数据，然后调用 PDFDoc
 * 类提供的功能生成 PDF 文件。
 * <p>
 * 版本 0.2 增加生成 HTML 的能力，主要的原因是 XSL 用起来太恼火
 */
public class TextParser {
    static final public int DOC_TYPE_PDF = 1;
    static final public int DOC_TYPE_HTML = 2;

   protected InputStream inputStream;
   protected InputStream jsonStream;
   protected OutputStream outStream;
   protected List<String> cssPaths;
   protected List<String> jsPaths;
   protected String outEncoding = null;
   protected String htmlDeclare = null;
   protected String htmlExtra = null;
   protected int typeInput = HTMLDoc.TYPE_INPUT;

    public TextParser(InputStream xmlStream, InputStream inputStream,
                      OutputStream outputStream) {
        this.inputStream = xmlStream;
        this.jsonStream = inputStream;
        this.outStream = outputStream;
        cssPaths = new ArrayList<>();
        jsPaths = new ArrayList<>();
    }

    /**
     * 在输出的 html 文件中添加 css 链接
     *
     * @param cssPaths css连接路径
     */
    public void setCssLinks(List<String> cssPaths) {
        this.cssPaths.addAll(cssPaths);
    }

    /**
     * 在输出的 html 文件中添加 css 链接
     *
     * @param cssPaths css连接路径
     */
    public void setCssLinks(String[] cssPaths) {
        if (cssPaths != null) {
            Collections.addAll(this.cssPaths, cssPaths);
        }
    }

    /**
     * 在输出的 html 文件中增加 js 链接
     *
     * @param jsPaths 连接集合
     */
    public void setJsLinks(List<String> jsPaths) {
        this.jsPaths.addAll(jsPaths);
    }

    /**
     * 在输出的 html 文件中增加 js 链接
     *
     * @param jsLinks 连接集合
     */
    public void setJsLinks(String[] jsLinks) {
        if (jsLinks != null) {
            Collections.addAll(this.jsPaths, jsLinks);
        }
    }

    /**
     * 设置 html 输出的文件编码
     *
     * @param encoding 编码
     */
    public void setOutputEncoding(String encoding) {
        this.outEncoding = encoding;
    }

    /**
     * 设置 html 文件的声明，默认为 <!DOCTYPE html>
     *
     * @param declare 内容
     */
    public void setHtmlDeclare(String declare) {
        this.htmlDeclare = declare;
    }

    /**
     * 增加一段内容到 html 的 body 结尾处
     *
     * @param extra 内容
     */
    public void setHtmlExtra(String extra) {
        this.htmlExtra = extra;
    }

    /**
     * 设置 HTML 输出类型
     *
     * @param type 类型
     */
    public void setHtmlType(int type) {
        this.typeInput = type;
    }

    /**
     * 解析 XML 模板并生成输出文档
     */
    public void gen(int docType) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        SAXParser parser = factory.newSAXParser();
        parser.parse(inputStream, new TextParserDocHandler(this, docType));
    }

    /**
     * 解析 XML 模板并生成 PDF 文档
     */
    public void genPdf() throws Exception {
        gen(DOC_TYPE_PDF);
    }

    /**
     * 解析 XML 模板并生成 HTML 文档
     */
    public void genHtml() throws Exception {
        gen(DOC_TYPE_HTML);
    }
}


