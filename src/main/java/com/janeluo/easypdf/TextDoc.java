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

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 输出文档抽象类
 */
public abstract class TextDoc {
    protected OutputStream outputStream;
    protected Rectangle pageSize = PageSize.A4;
    protected int pageMarginLeft = 50;
    protected int pageMarginRight = 50;
    protected int pageMarginTop = 50;
    protected int pageMarginBottom = 56;
    protected String encoding = "UTF-8";

    public TextDoc(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * 设置页面大小
     *
     * @param pageSize
     */
    public void setPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 设置页面边距
     *
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void setPageMargin(int left, int right, int top, int bottom) {
        pageMarginLeft = left;
        pageMarginRight = right;
        pageMarginTop = top;
        pageMarginBottom = bottom;
    }

    /**
     * 设置输出文件编码
     *
     * @param enc 编码
     */
    public void setEncoding(String enc) {
        this.encoding = enc;
    }

    /**
     * 打开文档
     *
     * @return
     */
    abstract public boolean open();

    /**
     * 关闭文档
     */
    abstract public void close();

    /**
     * 文档是否打开
     *
     * @return 是否打开
     */
    abstract public boolean isOpen();

    /**
     * 文本框
     *
     * @param blockName 文本框类型名称
     * @param chunkList 文本内容集合
     * @throws IOException IO异常
     */
    abstract public void writeBlock(String blockName,
                                    List<TextChunk> chunkList) throws IOException;

    /**
     * 添加新页
     */
    abstract public void newPage();

    /**
     * 水平线
     *
     * @param attrs 属性
     */
    abstract public void addHrule(Attributes attrs);

    /**
     * 添加图片
     *
     * @param attrs 属性
     */
    abstract public void addImage(Attributes attrs);

    /**
     * 添加表格
     *
     * @param table 表格
     * @throws IOException IO异常
     */
    abstract public void writeTable(TextTable table) throws IOException;
}
