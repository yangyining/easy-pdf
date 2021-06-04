/* Copyright (c) 2021 janeluo
 * easy-pdf is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.janeluo.easypdf;

import com.janeluo.easypdf.draw.CustomLineSeparator;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.splitting.ISplitCharacters;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF 操作类
 * <p>
 * 这个类封装 PDF 文档相关的操作，通过 iText 实现。
 * 如果需要水印、印章等特殊效果，请参考 PDFProcess 类。
 */
public class PDFDoc extends TextDoc {

    public final static int FONT_FAMILY_HEI = 1;
    public final static int FONT_FAMILY_SONG = 2;


    private final Map<String, BlockType> blockTypes = new HashMap<String, BlockType>() {{
        put("title", BlockType.BLOCK_TITLE);
        put("chapter", BlockType.BLOCK_CHAPTER);
        put("section", BlockType.BLOCK_SECTION);
        put("para", BlockType.BLOCK_PARA);
    }};

    private final List<PDFBlockDefault> pdfBlockDefaults;

    private PdfDocument pdfDocument;
    private Document document;
    private final Map<String, Image> images;

    private final ISplitCharacters splitCharacters = (glyphLine, i) -> true;

    public PDFDoc(OutputStream outputStream) {
        super(outputStream);

        pdfBlockDefaults = new ArrayList<>();
        images = new HashMap<>();

        // 默认的块属性，应用程序可以通过 setBlockDefault() 来修改这些属性
        pdfBlockDefaults.add(new PDFBlockDefault(BlockType.BLOCK_TITLE,
                FONT_FAMILY_HEI, 18, FontStyle.BOLD,
                TextAlignment.CENTER, 0.0f, 0.0f, 16.0f));
        pdfBlockDefaults.add(new PDFBlockDefault(BlockType.BLOCK_CHAPTER,
                FONT_FAMILY_SONG, 16, FontStyle.BOLD,
                TextAlignment.LEFT, 0.0f, 14.0f, 0.0f));
        pdfBlockDefaults.add(new PDFBlockDefault(BlockType.BLOCK_SECTION,
                FONT_FAMILY_SONG, 14, FontStyle.BOLD,
                TextAlignment.LEFT, 0.0f, 12.0f, 0.0f));
        pdfBlockDefaults.add(new PDFBlockDefault(BlockType.BLOCK_PARA,
                FONT_FAMILY_SONG, 12, FontStyle.NONE,
                TextAlignment.LEFT, 22.0f, 6.0f, 0.0f));
    }

    private void addMetaInfo() {
        PdfDocumentInfo info = pdfDocument.getDocumentInfo();
//        info.setTitle("TextPdf 合同");
//        info.setSubject("本合同带有防伪标识，请登录系统核查");
//        info.setAuthor("Lucky Byte, Inc.(诺百)");
//        info.setKeywords("TextPdf, PDF, Lucky Byte Inc., 诺百");
//        info.setCreator("TextPdf 版本 " + Version.VERSION +
//                " - http://git.oschina.net/lucky-byte/textpdf");
    }


    @Override
    public boolean open() {
        try {
            final PdfWriter writer = new PdfWriter(outputStream);
            pdfDocument = new PdfDocument(writer);
            this.document = new Document(pdfDocument);
            document.setMargins(pageMarginTop, pageMarginRight, pageMarginBottom, pageMarginLeft);
            // writer.setFullCompression();	// 需求 PDF 1.5
            writer.setCompressionLevel(9);
            addMetaInfo();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        pdfDocument.close();
    }

    @Override
    public boolean isOpen() {
        if (pdfDocument == null) {
            return false;
        }
        return !pdfDocument.isClosed();
    }

    @Override
    public void setPageSize(PageSize pageSize) {
        super.setPageSize(pageSize);
        if (isOpen()) {
            pdfDocument.addNewPage(pageSize);
        }
    }

    @Override
    public void setPageMargin(int left, int right, int top, int bottom) {
        super.setPageMargin(left, right, top, bottom);
        if (isOpen()) {
            document.setMargins(pageMarginTop, pageMarginRight, pageMarginBottom, pageMarginLeft);
        }
    }

    /**
     * 设置块默认属性
     * 这个函数一次性设置所有的块默认属性，如果需要单独设置某一个属性，
     * 请使用下面的 setBlockDefaultXXX() 函数。
     *
     * @param blockType  块类型
     * @param fontFamily 字体家族
     * @param fontSize   字体大小
     * @param fontStyle  字体风格
     * @param alignment  对齐方式
     * @param indent     首行缩进距离
     */
    public void setBlockDefault(BlockType blockType, int fontFamily,
                                int fontSize, FontStyle fontStyle, TextAlignment alignment, float indent,
                                float lineSpaceBefore, float lineSpaceAfter) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.fontFamily = fontFamily;
                block.fontSize = fontSize;
                block.fontStyle = fontStyle;
                block.alignment = alignment;
                block.indent = indent;
                block.lineSpaceBefore = lineSpaceBefore;
                block.lineSpaceAfter = lineSpaceAfter;
                break;
            }
        }
    }

    /**
     * 设置块的默认字体家族
     *
     * @param blockType  类型
     * @param fontFamily 字体
     */
    public void setBlockDefaultFontFamily(BlockType blockType, int fontFamily) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.fontFamily = fontFamily;
                break;
            }
        }
    }

    /**
     * 设置块的默认字体大小
     *
     * @param blockType 类型
     * @param fontSize  字体大小
     */
    public void setBlockDefaultFontSize(BlockType blockType, int fontSize) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.fontSize = fontSize;
                break;
            }
        }
    }

    /**
     * 设置块的默认字体风格
     *
     * @param blockType 类型
     * @param fontStyle 字体样式
     */
    public void setBlockDefaultFontStyle(BlockType blockType, FontStyle fontStyle) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.fontStyle = fontStyle;
                break;
            }
        }
    }

    /**
     * 设置块的默认对齐方式
     *
     * @param blockType 类型
     * @param alignment 对齐方式
     */
    public void setBlockDefaultAlignment(BlockType blockType, TextAlignment alignment) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.alignment = alignment;
                break;
            }
        }
    }

    /**
     * 设置块的默认首行缩进距离
     *
     * @param blockType 类型
     * @param indent    首航缩进大小
     */
    public void setBlockDefaultIndent(BlockType blockType, float indent) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.indent = indent;
                break;
            }
        }
    }

    /**
     * 设置段前空间
     *
     * @param blockType       类型
     * @param lineSpaceBefore 行上面空行大小
     */
    public void setBlockDefaultLineSpaceBefore(BlockType blockType, float lineSpaceBefore) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.lineSpaceBefore = lineSpaceBefore;
                break;
            }
        }
    }

    /**
     * 设置段后空间
     *
     * @param blockType      类型
     * @param lineSpaceAfter 下面空行大小
     */
    public void setBlockDefaultLineSpaceAfter(BlockType blockType, float lineSpaceAfter) {
        for (PDFBlockDefault block : pdfBlockDefaults) {
            if (block.blockType == blockType) {
                block.lineSpaceAfter = lineSpaceAfter;
                break;
            }
        }
    }

    /**
     * 根据 TextChunk 中字体相关的属性来设置 Chunk 的字体，字体包括：
     * 家族(黑体或宋体)、大小、修饰(粗体、斜体、下划线等等)。
     *
     * @param textChunk    TextChunk 对象，保存了字体的属性
     * @param chunk        PDF Chunk 对象
     * @param blockDefault
     * @throws IOException
     */
    private void setChunkFont(TextChunk textChunk, Text chunk,
                              PDFBlockDefault blockDefault) throws IOException {
        Map<String, String> attrs = textChunk.getAttrs();

        int fontFamily = blockDefault.fontFamily;
        int fontSize = blockDefault.fontSize;
        FontStyle font_style = blockDefault.fontStyle;
        PdfFont pdfFont = null;

        String value = attrs.get("font-family");
        if (value != null) {
            if ("heiti".equalsIgnoreCase(value) ||
                    "hei".equalsIgnoreCase(value)) {
                fontFamily = FONT_FAMILY_HEI;
            } else if ("songti".equalsIgnoreCase(value) ||
                    "song".equalsIgnoreCase(value)) {
                fontFamily = FONT_FAMILY_SONG;
            } else {
                System.err.println("Font family '" + value + "' unknown!");
            }
        }

        value = attrs.get("font-size");
        if (value != null) {
            try {
                fontSize = Integer.parseInt(value);
            } catch (Exception ex) {
                System.err.println("Font size '" + value + "' invalid.");
            }
        }

        value = attrs.get("font-style");
        if (value != null) {

            String[] styles = value.split(",");
            for (String style : styles) {
                String label = style.trim();
                if ("bold".equalsIgnoreCase(label)) {
                    chunk.setBold();
                } else if ("italic".equalsIgnoreCase(label)) {
                    chunk.setItalic();
                } else if ("underline".equalsIgnoreCase(label)) {
                    chunk.setUnderline();
                }
            }
        } else {
            switch (font_style) {
                case BOLD:
                    chunk.setBold();
                    break;
                case ITALIC:
                    chunk.setItalic();
                    break;
                case UNDERLINE:
                    chunk.setUnderline();
                    break;
                case NONE:
                default:
                    break;

            }
        }

        switch (fontFamily) {
            case FONT_FAMILY_HEI:
                pdfFont = PdfFontFactory.createFont("font/SIMHEI.TTF",
                        PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                break;
            case FONT_FAMILY_SONG:
                pdfFont = PdfFontFactory.createFont("font/SIMSUN.TTC,0",
                        PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                break;
            default:
                break;
        }
        chunk.setFontSize(fontSize);
        chunk.setFont(pdfFont);
    }

    /**
     * 根据 TextChunk 的属性生成 PDF Text 对象
     *
     * @param text_chunk   TextChunk 对象
     * @param blockDefault 块默认类型
     * @return PDF Text
     */
    private Text formatChunk(TextChunk text_chunk,
                             PDFBlockDefault blockDefault) {
        Text chunk = new Text("");
        Map<String, String> attrs = text_chunk.getAttrs();

        String value = attrs.get("super");
        if ("true".equalsIgnoreCase(value)) {
            chunk.setTextRise(6.0f);
            if (!attrs.containsKey("font-size")) {
                attrs.put("font-size", "8");
            }
        }
        value = attrs.get("sub");
        if ("true".equalsIgnoreCase(value)) {
            chunk.setTextRise(-3.0f);
            if (!attrs.containsKey("font-size")) {
                attrs.put("font-size", "8");
            }
        }

        String contents = text_chunk.getContents();

        value = text_chunk.getAttrs().get("minlen");
        if (value != null && value.length() > 0) {
            if (contents.length() == 0) {
                chunk.setUnderline(1.0f, -4.0f);
            }
            try {
                int minlen = Integer.parseInt(value);
                int currlen = 0;
                for (int i = 0; i < contents.length(); i++) {
                    char ch = contents.charAt(i);
                    if (ch < 127) {
                        currlen += 1;
                    } else {
                        currlen += 2;
                    }
                }
                if (currlen < minlen) {
                    StringBuilder builder = new StringBuilder(contents);
                    for (; currlen < minlen; currlen++) {
                        builder.append(' ');
                    }
                    contents = builder.toString();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("minlen need a integer value.");
            }
        }
        chunk.setText(contents);
//        chunk.append(contents);
        try {
            setChunkFont(text_chunk, chunk, blockDefault);
        } catch (IOException ignored) {

        }
        return chunk;
    }

    private void formatParagraph(Paragraph para, List<TextChunk> chunk_list) {
        // 用第一个节点来设置块的段落属性
        TextChunk text_chunk = chunk_list.get(0);
        if (text_chunk != null) {
            Map<String, String> attrs = text_chunk.getAttrs();

            // 设置段落对齐方式
            String value = attrs.get("align");
            if (value != null) {
                if ("left".equalsIgnoreCase(value)) {
                    para.setTextAlignment(TextAlignment.LEFT);
                } else if ("center".equalsIgnoreCase(value)) {
                    para.setTextAlignment(TextAlignment.CENTER);
                } else if ("right".equalsIgnoreCase(value)) {
                    para.setTextAlignment(TextAlignment.RIGHT);
                } else {
                    System.err.println("Block alignment type '"
                            + value + "' unknown.");
                }
            }
            // 设置段落缩进
            value = attrs.get("indent");
            if (value != null) {
                try {
                    float indent = Float.parseFloat(value);
                    para.setFirstLineIndent(indent);
                } catch (Exception ex) {
                    System.err.println(
                            "Indent attribute must has a float value");
                }
            }
            // 设置段落前空间
            value = attrs.get("space-before");
            if (value != null) {
                try {
                    float space = Float.parseFloat(value);
                    para.setMarginTop(space);
                } catch (Exception ex) {
                    System.err.println(
                            "space-before attribute must has a float value");
                }
            }
            // 设置段落后空间
            value = attrs.get("space-after");
            if (value != null) {
                try {
                    float space = Float.parseFloat(value);
                    para.setMarginBottom(space);
                } catch (Exception ex) {
                    System.err.println(
                            "space-after attribute must has a float value");
                }
            }
        }
    }

    /**
     * 添加一段文字到 PDF 文档
     *
     * @param blockType       块类型
     * @param chunkList       chunks 列表
     * @param pdfBlockDefault
     */
    private void addParagraph(BlockType blockType, List<TextChunk> chunkList, PDFBlockDefault pdfBlockDefault) {
        Paragraph para = new Paragraph();

        for (TextChunk textChunk : chunkList) {
            Text chunk = formatChunk(textChunk, pdfBlockDefault);
            chunk.setSplitCharacters(splitCharacters);
            para.add(chunk);
        }
        para.setMarginTop(pdfBlockDefault.lineSpaceBefore);
        para.setMarginBottom(pdfBlockDefault.lineSpaceAfter);
        para.setTextAlignment(pdfBlockDefault.alignment);
        para.setFirstLineIndent(pdfBlockDefault.indent);

        formatParagraph(para, chunkList);
        document.add(para);
    }

    /**
     * 添加一块内容到 PDF 文档，块可以为 Title、Section、等等，
     * 参考类前面的数组定义
     *
     * @param blockName 块类型名，例如 title, section 等等
     * @param chunkList 本块的内容，一个块包含多个 chunk，它们通过列表保存
     */
    @Override
    public void writeBlock(String blockName, List<TextChunk> chunkList) {
        if (blockName == null ||
                chunkList == null || chunkList.size() == 0) {
            return;
        }
        if (pdfDocument == null || pdfDocument.isClosed()) {
            System.err.println("Document unopen yet, please open it first.");
            return;
        }


        // 将块名称映射到内部的整数表示
        final String lowerBlockName = blockName.toLowerCase();
        if (!blockTypes.containsKey(lowerBlockName)) {
            System.err.println("Block type '" + blockName + "' unknown!");
            return;
        }
        BlockType blockType = blockTypes.get(lowerBlockName);

        for (PDFBlockDefault pdfBlockDefault : pdfBlockDefaults) {
            if (pdfBlockDefault.blockType == blockType) {
                addParagraph(blockType, chunkList, pdfBlockDefault);
                break;
            }
        }
    }

    /**
     * 换页
     */
    @Override
    public void newPage() {
        if (pdfDocument == null || pdfDocument.isClosed()) {
            System.err.println("Document unopen yet, please open it first.");
            return;
        }
        pdfDocument.addNewPage();
    }

    @Override
    public void addHrule(Attributes attrs) {
        try {
            int width = 1;
            int percent = 100;
            String value = attrs.getValue("width");
            if (value != null) {
                try {
                    width = Integer.parseInt(value);
                } catch (Exception ignored) {
                }
            }
            value = attrs.getValue("percent");
            if (value != null) {
                try {
                    percent = Integer.parseInt(value);
                } catch (Exception ignored) {
                }
            }
            document.add(new AreaBreak());
            document.add(new LineSeparator(new CustomLineSeparator(width, percent)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 添加一个图片
     *
     * @param attrs 属性
     */
    @Override
    public void addImage(Attributes attrs) {
        try {
            String src = attrs.getValue("src");
            if (src == null) {
                System.err.println("img missing src attribute.");
                return;
            }
            Image img = images.get(src);
            if (img == null) {
                img = new Image(ImageDataFactory.create(src));
                images.put(src, img);
            }

            document.add(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Table createTable(Map<String, String> attrs) {
        float width = 100;
        int[] columns = null;
        Table table;

        String value = attrs.get("columns");
        if (value != null) {
            try {
                String[] array = value.split(",");
                columns = new int[array.length];
                for (int i = 0; i < array.length; i++) {
                    columns[i] = Integer.parseInt(array[i]);
                }
            } catch (Exception ex) {
                System.err.println("column must has a integer value");
            }
        }
        if (columns == null) {
            table = new Table(UnitValue.createPercentArray(1));
        } else {
            table = new Table(UnitValue.createPercentArray(columns.length));

        }

        value = attrs.get("width");
        if (value != null) {
            try {
                width = Float.parseFloat(value);
            } catch (Exception ex) {
                System.err.println("width must has a float value");
            }
        }
        table.setWidth(UnitValue.createPercentValue(width));
        table.setFixedLayout();


        return table;
    }

    private Cell createTableCell(TextChunk textChunk,
                                 PDFBlockDefault blockDefault) {
        Text chunk = formatChunk(textChunk, blockDefault);
        Paragraph phrase = new Paragraph();
        phrase.add(chunk);

        Map<String, String> attrs = textChunk.getAttrs();
        String value = attrs.get("colspan");
        int colspan = 1;
        if (value != null) {
            try {
                colspan = Integer.parseInt(value);
            } catch (Exception ex) {
                System.err.println("colspan must has a integer value");
            }
        }
        Cell cell = new Cell(1, colspan);
        cell.add(phrase);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setPadding(5);

        value = attrs.get("align");
        if (value != null) {
            if ("left".equalsIgnoreCase(value)) {
                cell.setTextAlignment(TextAlignment.LEFT);
            } else if ("center".equalsIgnoreCase(value)) {
                cell.setTextAlignment(TextAlignment.CENTER);
            } else if ("right".equalsIgnoreCase(value)) {
                cell.setTextAlignment(TextAlignment.RIGHT);
            }
        }
        return cell;
    }

    @Override
    public void writeTable(TextTable table) {
        if (!isOpen() || table == null) {
            return;
        }
        PDFBlockDefault blockDefault = null;
        for (PDFBlockDefault def : this.pdfBlockDefaults) {
            if (def.blockType == BlockType.BLOCK_PARA) {
                blockDefault = def;
                break;
            }
        }
        Table pdfTable = createTable(table.getAttrs());

        for (TextChunk textChunk : table.getCells()) {
            pdfTable.addCell(createTableCell(textChunk, blockDefault));
        }
        document.add(pdfTable);
    }

}


/**
 * 一个类用于保存块默认属性
 */
class PDFBlockDefault {
    protected BlockType blockType;
    protected int fontFamily;
    protected int fontSize;
    protected FontStyle fontStyle;
    protected TextAlignment alignment;
    protected float indent;
    protected float lineSpaceBefore;
    protected float lineSpaceAfter;

    public PDFBlockDefault(BlockType blockType, int family,
                           int size, FontStyle style, TextAlignment alignment, float indent,
                           float lineSpaceBefore, float lineSpaceAfter) {
        this.blockType = blockType;
        this.fontFamily = family;
        this.fontSize = size;
        this.fontStyle = style;
        this.alignment = alignment;
        this.indent = indent;
        this.lineSpaceBefore = lineSpaceBefore;
        this.lineSpaceAfter = lineSpaceAfter;
    }


}

/**
 * 块类型
 */
enum BlockType {


    /**
     * 标题
     */
    BLOCK_TITLE(1),
    /**
     *
     */
    BLOCK_CHAPTER(2),
    /**
     *
     */
    BLOCK_SECTION(3),
    /**
     *
     */
    BLOCK_PARA(3),
    ;


    BlockType(int type) {
        this.type = type;
    }

    private final int type;

    public int getType() {
        return type;
    }
}

enum FontStyle {

    /**
     * 空
     */
    NONE(0),
    /**
     * 加粗
     */
    BOLD(1),
    /**
     * 下划线
     */
    UNDERLINE(2),
    ITALIC(4),
    ;

    FontStyle(int type) {
        this.type = type;
    }

    private final int type;

    public int getType() {
        return type;
    }

}
