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


import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取 .doc 文件，并转换为 TextPDF 可识别的模板格式
 */
public class DocReader {
    private URL xslUrl = null;
    private boolean autoTitle = false;
    private boolean ignoreBlankPara = false;
    private Map<String, Object> jsonObject;
    private Map<String, String> jsonData;

    /**
     * 如果指定，将在文件中增加 XSL 风格页的引用
     *
     * @param url XSL stylesheet URL
     */
    public void setXSLUrl(URL url) {
        this.xslUrl = url;
    }

    /**
     * 自动识别标题行，默认关闭，可以通过这个函数开启
     *
     * @param autoTitle 是/否
     */
    public void setAutoTitle(boolean autoTitle) {
        this.autoTitle = autoTitle;
    }

    /**
     * 是否忽略空白段落
     *
     * @param ignore
     */
    public void ignoreBlankPara(boolean ignore) {
        this.ignoreBlankPara = ignore;
    }

    private int getTitleIndex(Range range) {
        int index = 0;
        int maxFontSize = 0;
        boolean center = false;

        // 从头 3 段中找标题
        int nParas = Math.min(3, range.numParagraphs());
        for (int i = 0; i < nParas; i++) {
            Paragraph para = range.getParagraph(i);

            // 找到这一段中最大的字体
            int fontSize = 0;
            for (int j = 0; j < para.numCharacterRuns(); j++) {
                CharacterRun run = para.getCharacterRun(j);
                fontSize = Math.max(fontSize, run.getFontSize());
            }

            // 如果字体比之前的都大，则认为是标题
            if (fontSize > maxFontSize) {
                index = i;
                maxFontSize = fontSize;
            } else if (fontSize == maxFontSize) {
                if (!center && para.getJustification() == 1) {
                    index = i;
                    center = true;
                }
            }
        }
        return index;
    }

    private void appendParaAttrs(StringBuilder builder, Paragraph para) {
        switch (para.getJustification()) {
            case 1:
                builder.append(" align=\"center\"");
                break;
            case 2:
                builder.append(" align=\"right\"");
                break;
            case 3:    // left 对齐是默认的，不写入模板中
                break;
            default:
                break;
        }
    }

    private void appendRunAttrs(StringBuilder builder,
                                CharacterRun run, boolean isSpan) {
        StringBuilder style = new StringBuilder();

        if (isSpan) {
            if (run.isBold()) {
                style.append("bold");
            }
            if (run.isItalic()) {
                if (style.length() > 0) {
                    style.append(",");
                }
                style.append("italic");
            }
            if (run.getUnderlineCode() == 1) {
                if (style.length() > 0) {
                    style.append(",");
                }
                style.append("underline");
            }
            if (style.length() > 0) {
                builder.append(" font-style=\"").append(style).append("\"");
            }
        }

        builder.append(" font-size=\"");
        builder.append(run.getFontSize() / 2);
        builder.append("\"");
    }

    private String textEscape(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String escape = Util.escapeXmlChars(text.charAt(i));
            if (escape != null) {
                builder.append(escape);
            } else {
                builder.append(text.charAt(i));
            }
        }
        return builder.toString();
    }

    private void readCharacterRuns(Paragraph para, int paraIndex,
                                   StringBuilder builder, boolean isTitle) {
        StringBuilder allText = null;
        if (isTitle && jsonObject != null) {
            allText = new StringBuilder();
        }
        for (int j = 0; j < para.numCharacterRuns(); j++) {
            CharacterRun run = para.getCharacterRun(j);
            String text = run.text().replaceAll("[\u0000-\u001f]", "");

            System.out.println("run text: " + text + " >i=" + paraIndex);
//			System.out.println("vanished: " + run.isVanished());
//			System.out.println("special: " + run.isSpecialCharacter());

            // 忽略特殊字符
            if (run.isSpecialCharacter()) {
                continue;
            }
            // 忽略级链接
            if (text.matches(" HYPERLINK .+") ||
                    text.matches("HYPERLINK .+") ||
                    text.matches(" PAGEREF .+") ||
                    text.matches(" TOC .+")) {
                continue;
            }

            // \u3000: IDEOGRAPHIC SPACE
            if (text.matches("^[\\s\u3000]+$")) {
                if (run.getUnderlineCode() == 1) {
                    String vid = "vid_" + paraIndex + "_" + j;
                    builder.append("    <value id=\"");
                    builder.append(vid);
                    builder.append("\" minlen=\"");
                    builder.append(text.length());
                    builder.append("\"");
                    appendRunAttrs(builder, run, false);
                    builder.append(" />\n");
                    if (jsonData != null) {
                        jsonData.put(vid, "");
                    }
                } else {
                    builder.append("    <hspace");
                    builder.append(" size=\"");
                    builder.append(text.length());
                    builder.append("\"");
                    appendRunAttrs(builder, run, false);
                    builder.append(" />\n");
                }
            } else if (text.matches("^_+$")) {
                String vid = "vid_" + paraIndex + "_" + j;
                builder.append("    <value id=\"");
                builder.append(vid);
                builder.append("\" minlen=\"");
                builder.append(text.length());
                builder.append("\"");
                appendRunAttrs(builder, run, false);
                builder.append(" />\n");
                if (jsonData != null) {
                    jsonData.put(vid, "");
                }
            } else if (text.length() > 0) {
                builder.append("    <span");
                appendRunAttrs(builder, run, true);
                builder.append(">");
                builder.append(textEscape(text));
                builder.append("</span>\n");

                if (allText != null) {
                    allText.append(text);
                }
            }
        }
        if (isTitle && jsonObject != null) {
            jsonObject.put("title", allText != null ? allText.toString() : "");
        }
    }

    /**
     * 转换 .doc 文件
     *
     * @param docStream .doc 数据流
     * @param xmlStream .xml 输出流，用于保存转换后结果
     */
    public void read(InputStream docStream, OutputStream xmlStream,
                     OutputStream jsonStream)
            throws IOException {
        if (docStream == null || xmlStream == null) {
            System.err.println("Invalid argument");
            return;
        }
        if (jsonStream != null) {
            jsonObject = new HashMap<>();
            jsonData = new HashMap<>();
        }
        HWPFDocument document = new HWPFDocument(docStream);
        Range range = document.getRange();
        StringBuilder builder = new StringBuilder();

        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        if (xslUrl != null) {
            builder.append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(xslUrl.getPath()).append("\"?>\n");
        }
        builder.append("\n<!-- Automatic generated by TextPDF DocReader -->\n");
        builder.append("\n<textpdf>\n");

        xmlStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        builder.setLength(0);

        int titleIndex = 0;
        if (autoTitle) {
            titleIndex = getTitleIndex(range);
        }

        Table table = null;

        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph para = range.getParagraph(i);
            boolean isTitle = false;

            if (para.pageBreakBefore()) {    // 换页符
                builder.append("  <pagebreak />\n");
            }

            if (para.isInTable()) {        // 表格
                if (table == null) {
                    table = range.getTable(para);
                    int maxCells = 0;
                    for (int m = 0; m < table.numRows(); m++) {
                        TableRow row = table.getRow(m);
                        maxCells = Math.max(maxCells, row.numCells());
                    }
                    StringBuilder columns = new StringBuilder();
                    columns.append("1");
                    for (int n = 1; n <= maxCells; n++) {
                        if (n == maxCells) {
                            columns.append(",0");
                        } else {
                            columns.append(",1");
                        }
                    }
                    builder.append("  <table columns=\"").append(columns).append("\">\n");
                }
                String text = para.text().replaceAll("[\u0000-\u001f]", "");
                builder.append("    <cell>");
                builder.append(textEscape(text));
                builder.append("</cell>\n");
                continue;
            } else {
                if (table != null) {
                    builder.append("  </table>\n");
                    table = null;
                }
            }

            if (ignoreBlankPara && para.numCharacterRuns() == 0) {
                continue;
            }
            if (autoTitle && i == titleIndex) {
                builder.append("  <title");
                isTitle = true;
            } else {
                builder.append("  <para");
            }
            appendParaAttrs(builder, para);
            builder.append(">\n");

            readCharacterRuns(para, i, builder, isTitle);

            if (autoTitle && i == titleIndex) {
                builder.append("  </title>\n");
            } else {
                builder.append("  </para>\n");
            }
            xmlStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            builder.setLength(0);
        }
        xmlStream.write("</textpdf>\n".getBytes());

        // 输出 JSON 数据模板
        if (jsonStream != null) {
            jsonObject.put("data", jsonData);
            String jsonString = JSONObject.toJSONString(jsonObject);
            jsonStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        }
    }

}
