/* Copyright (c) 2021 janeluo
 * easy-pdf is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
