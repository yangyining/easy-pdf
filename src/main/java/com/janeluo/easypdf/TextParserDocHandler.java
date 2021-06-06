/* Copyright (c) 2021 janeluo
 * easy-pdf is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.janeluo.easypdf;


import com.alibaba.fastjson.JSONObject;
import com.itextpdf.kernel.geom.PageSize;
import com.janeluo.easypdf.enums.DocType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 * 解析 XML 模板，并生成 PDF 文件
 *
 * @author janeluo
 */
@Slf4j
public class TextParserDocHandler extends DefaultHandler {
    public static final String[] BLOCK_ELEMENTS = {
            "title", "chapter", "section", "para",
            "pagebreak", "table"
    };


    // 页面大小常数定义
    private final Object[][] pageSizeMap = {
            {"a0", PageSize.A0}, {"a1", PageSize.A1},
            {"a2", PageSize.A2}, {"a3", PageSize.A3},
            {"a4", PageSize.A4}, {"a5", PageSize.A5},
            {"a6", PageSize.A6}, {"a7", PageSize.A7},
            {"a8", PageSize.A8}, {"a9", PageSize.A9},
            {"a10", PageSize.A10},

            {"b0", PageSize.B0}, {"b1", PageSize.B1},
            {"b2", PageSize.B2}, {"b3", PageSize.B3},
            {"b4", PageSize.B4}, {"b5", PageSize.B5},
            {"b6", PageSize.B6}, {"b7", PageSize.B7},
            {"b8", PageSize.B8}, {"b9", PageSize.B9},
            {"b10", PageSize.B10},
    };

    private final TextParser parser;
    private final TextDoc textDoc;
    private final List<TextChunk> chunkList;
    private final Stack<TextChunk> chunkStack;
    private final StringBuilder contentsBuilder;
    private JSONObject jsonData;
    private TextTable table = null;

    public TextParserDocHandler(TextParser parser, DocType docType) throws IOException {
        chunkList = new ArrayList<>();
        chunkStack = new Stack<>();
        contentsBuilder = new StringBuilder();

        this.parser = parser;

        switch (docType) {
            case DPF:
                textDoc = new PDFDoc(parser.templateStream, parser.outStream);
                break;

            case HTML:
                textDoc = new HTMLDoc(parser.outStream);
                HTMLDoc textDoc = (HTMLDoc) this.textDoc;
                textDoc.setLinkPaths(parser.cssPaths, parser.jsPaths);
                if (parser.htmlDeclare != null) {
                    textDoc.setDeclare(parser.htmlDeclare);
                }
                if (parser.htmlExtra != null) {
                    textDoc.setExtra(parser.htmlExtra);
                }
                break;
            default:
                log.error("Document type unsupported.");
                throw new IOException("Document type unsupported.");
        }

        if (parser.outputEncoding != null) {
            textDoc.setEncoding(parser.outputEncoding);
        }
    }

    /**
     * 文档开始解析时回调
     */
    @Override
    public void startDocument() throws SAXException {
        if (log.isDebugEnabled()) {
            log.debug("解析文件开始");
        }
        try {
            if (parser.jsonStream != null) {
                final String jsonStr = IOUtils.toString(parser.jsonStream, StandardCharsets.UTF_8);

                JSONObject jsonObject = JSONObject.parseObject(jsonStr);

                if (textDoc instanceof PDFDoc) {
                    if (!jsonObject.containsKey("data")) {
                        if (log.isErrorEnabled()) {
                            log.error("JSON source missing 'data' key, please check!");
                        } else {
                            System.err.println(
                                    "JSON source missing 'data' key, please check!");
                        }
                    } else {
                        Object value = jsonObject.get("data");
                        if (!(value instanceof JSONObject)) {
                            if (log.isErrorEnabled()) {
                                log.error("JSON 'data' must be a object.");
                            } else {
                                System.err.println("JSON 'data' must be a object.");
                            }
                        } else {
                            jsonData = (JSONObject) value;
                        }
                    }
                } else if (textDoc instanceof HTMLDoc) {
                    ((HTMLDoc) textDoc).setJSONObject(jsonObject);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SAXException("Failed to parse JSON stream");
        }
    }

    /**
     * 文档解析结束时回调
     */
    @Override
    public void endDocument() {
    }


    private void setupPage(Attributes attrs) {
        // 页面大小
        String value = attrs.getValue("size");
        if (value != null) {
            for (Object[] item : pageSizeMap) {
                if (value.equalsIgnoreCase((String) item[0])) {
                    textDoc.setPageSize((PageSize) item[1]);
                    break;
                }
            }
        }

        // 页面边距
        value = attrs.getValue("margin");
        if (value != null) {
            String[] array = value.split(",");
            if (array.length < 4) {

                if (log.isErrorEnabled()) {
                    log.error("Page margin format error.");
                } else {
                    System.err.println("Page margin format error.");
                }
            } else {
                try {
                    textDoc.setPageMargin(
                            Integer.parseInt(array[0].trim()),
                            Integer.parseInt(array[1].trim()),
                            Integer.parseInt(array[2].trim()),
                            Integer.parseInt(array[3].trim()));
                } catch (Exception ex) {
                    if (log.isErrorEnabled()) {
                        log.error("Page margin format error.");
                    } else {
                        System.err.println("Page margin format error.");
                    }
                }
            }
        }
    }

    /**
     * 元素开始时回调
     */
    @Override
    public void startElement(String namespaceUri,
                             String localName, String qName, Attributes attrs)
            throws SAXException {
        log.info(">>>>>> start qName:[{}]", qName);
        TextChunk prevChunk = null;

        if ("textpdf".equalsIgnoreCase(qName)) {
            if (textDoc.isOpen()) {
                throw new SAXException("'textpdf' must be root element.");
            }
            if (!textDoc.open()) {
                throw new SAXException("Open document failed.");
            }
            return;
        }

        if (!textDoc.isOpen()) {
            throw new SAXException("Document unopen yet. "
                    + "check your xml root element is 'textpdf'");
        }

        // Block 元素不可嵌套
        for (String label : BLOCK_ELEMENTS) {
            if (label.equalsIgnoreCase(qName)) {
                chunkList.clear();
                break;
            }
        }

        if ("table".equalsIgnoreCase(qName)) {
            table = new TextTable();
            table.addAttrs(attrs);
            return;
        }
        if (table != null) {
            if (!"cell".equalsIgnoreCase(qName)) {
                throw new SAXException(qName + " is not child of table");
            }
            TextChunk chunk = new TextChunk();
            chunk.addAttrs(attrs);
            table.addCell(chunk);
            contentsBuilder.setLength(0);
            return;
        }

        if ("page".equalsIgnoreCase(qName)) {
            setupPage(attrs);
            textDoc.newPage();
            return;
        }
        if ("hrule".equalsIgnoreCase(qName)) {
            textDoc.addHrule(attrs);
            return;
        }
        if ("img".equalsIgnoreCase(qName)) {
            textDoc.addImage(attrs);
            return;
        }

        try {
            prevChunk = chunkStack.peek();
            String contents = contentsBuilder.toString();
            if (contents.length() > 0) {
                prevChunk.setContents(contents);
                contentsBuilder.setLength(0);
                chunkList.add(prevChunk.clone());
            }
        } catch (EmptyStackException ignored) {
        }

        TextChunk chunk = new TextChunk();
        if (prevChunk != null) {
            chunk.addAttrs(prevChunk.getAttrs());
        }
        chunk.addAttrs(attrs);

        if ("value".equalsIgnoreCase(qName)) {
            chunk.setIsValue(true);

            String id = attrs.getValue("id");
            if (id == null) {

                if (log.isErrorEnabled()) {
                    log.error("Value element missing 'id' attribute.");
                } else {
                    System.err.println("Value element missing 'id' attribute.");
                }
            } else {
                if (textDoc instanceof PDFDoc) {
                    if (jsonData != null) {
                        if (!jsonData.containsKey(id)) {
                            if (log.isErrorEnabled()) {
                                log.error("JSON data key '" + id
                                        + "' not found!");
                            } else {
                                System.err.println("JSON data key '" + id
                                        + "' not found!");
                            }

                        } else {
                            Object value = jsonData.get(id);
                            if (!(value instanceof String)) {
                                if (log.isErrorEnabled()) {
                                    log.error("JSON  data key '" + id
                                            + "' must has a string value.");
                                } else {
                                    System.err.println("JSON  data key '" + id
                                            + "' must has a string value.");
                                }
                            } else {
                                contentsBuilder.append(value);
                                if (attrs.getValue("font-style") == null) {
                                    chunk.addAttr("font-style", "bold,underline");
                                }
                            }
                        }
                    }
                }
            }
        } else if ("hspace".equalsIgnoreCase(qName)) {
            String value = attrs.getValue("size");
            if (value == null || value.length() == 0) {
                if (log.isErrorEnabled()) {
                    log.error("hspace need a size attribute.");
                } else {
                    System.err.println("hspace need a size attribute.");
                }
            } else {
                try {
                    int size = Integer.parseInt(value);
                    for (int i = 0; i < size; i++) {
                        contentsBuilder.append(' ');
                    }
                } catch (Exception ex) {
                    if (log.isErrorEnabled()) {
                        log.error("size attribute need a integer value", ex);
                    } else {
                        System.err.println("size attribute need a integer value");
                    }
                }
            }
        }
        chunkStack.push(chunk);
    }

    /**
     * 标签字符串处理
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        String contents = new String(ch, start, length);
        contentsBuilder.append(
                contents.replaceAll("\\s*\n+\\s*", "").trim());
    }

    /**
     * 元素结束时回调
     */
    @Override
    public void endElement(String namespaceUri,
                           String localName, String qName) throws SAXException {
        log.info(">>>>>> end qName:[{}]", qName);
        if ("textpdf".equalsIgnoreCase(qName)) {
            textDoc.close();
            return;
        }
        if ("pagebreak".equalsIgnoreCase(qName)) {
            textDoc.newPage();
            return;
        }
        if ("break".equalsIgnoreCase(qName)) {
            contentsBuilder.append("\n");
            return;
        }

        if ("cell".equalsIgnoreCase(qName)) {
            TextChunk chunk = table.lastCell();
            chunk.setContents(contentsBuilder.toString());
        }
        if ("table".equalsIgnoreCase(qName)) {
            if (table.getCells().size() > 0) {
                try {
                    textDoc.writeTable(table);
                } catch (IOException e) {
                    throw new SAXException(e);
                }
            }
            contentsBuilder.setLength(0);
            table = null;
            return;
        }

        TextChunk chunk = null;
        try {
            chunk = chunkStack.pop();
        } catch (Exception ignored) {
        }

        if (chunk == null) {
            return;
        }
        String contents = contentsBuilder.toString();
        if (contents.length() > 0 ||
                "value".equalsIgnoreCase(qName) ||
                "hspace".equalsIgnoreCase(qName)) {
            chunk.setContents(contents);
            contentsBuilder.setLength(0);
            chunkList.add(chunk.clone());
        }

        for (String label : BLOCK_ELEMENTS) {
            // 空段落，需要增加一个空 TextChunk 对象去模拟空段落
            if (chunkList.size() == 0 && "para".equalsIgnoreCase(label)) {
                chunk.setContents(" ");
                chunkList.add(chunk.clone());
            }

            if (chunkList.size() > 0) {
                if (label.equalsIgnoreCase(qName)) {
                    try {
                        textDoc.writeBlock(qName, chunkList);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new SAXException("Write to PDF failed.");
                    } finally {
                        chunkList.clear();
                    }
                    break;
                }
            }
        }
    }

}
