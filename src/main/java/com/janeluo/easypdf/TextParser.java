/* Copyright (c) 2021 janeluo
 * easy-pdf is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.janeluo.easypdf;

import com.janeluo.easypdf.enums.DocType;
import lombok.Data;

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
@Data
public class TextParser {

    /**
     * 模板输入流
     */
    protected InputStream templateStream;
    /**
     * xml输入流
     */
    protected InputStream xmlStream;
    /**
     * json数据输入流
     */
    protected InputStream jsonStream;
    /**
     * 导出文件输出流
     */
    protected OutputStream outStream;
    protected List<String> cssPaths;
    protected List<String> jsPaths;
    protected String outputEncoding = null;
    protected String htmlDeclare = null;
    protected String htmlExtra = null;
    protected int typeInput = HTMLDoc.TYPE_INPUT;

    public TextParser(InputStream xmlStream, InputStream inputStream,
                      OutputStream outputStream) {
        this.xmlStream = xmlStream;
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
     * 解析 XML 模板并生成输出文档
     */
    public void gen(DocType docType) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        SAXParser parser = factory.newSAXParser();
        parser.parse(xmlStream, new TextParserDocHandler(this, docType));
    }

    /**
     * 解析 XML 模板并生成 PDF 文档
     */
    public void genPdf() throws Exception {
        gen(DocType.DPF);
    }

    /**
     * 解析 XML 模板并生成 HTML 文档
     */
    public void genHtml() throws Exception {
        gen(DocType.HTML);
    }
}


