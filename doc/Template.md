# 模板说明

TextPDF 模板侧重于表达文档的排版格式，而不是文档的结构。
所有`chapter`, `section`，等等，都是没有语义的，例如 TextPDF 并不能通过它们来生产文档结构图，
而只是为它们提供了不同的默认排版风格而已，因此，你完全可以忽略这些标签，而改用特定的属性达到一样的效果。

TextPDF 的模板模型非常简单，只有两类标签，块标签以及內联标签，每个块标签会输出成 PDF 的一个段落。
內联标签只能嵌套在块标签中使用，来混排一段文字。

內联标签不能在块标签之外使用，否则会被完全的忽略。

下面的示例只是强调某个元素，测试时需要将其包含到 `textpdf`标签中，即：

```xml
<textpdf>
    示例内容放在这里
</textpdf>
```

## 块标签

### page

用于定义页尺寸以及页边距。可以通过在文档中放置多个`page`标签，得到大小不等的页面。

这个标签有一个单边影响就是会立即强制换页。

```xml
<page size="a4" margin="40,40,40,40"/>
```

可以在一个文档中的任何地方插入此标签来得到大小不等的页面。
如果只是为了强制换页，使用`<pagebreak/>`标签。

### title

默认：黑体、18、居中对齐、加粗
```xml
<title>标题</title>
```

### chapter

默认: 宋体、16、左对齐、加粗

```xml
<chapter>节</chapter>
```

### section

默认: 宋体、14、左对齐、加粗

```xml
<section>节</section>
```

### para

默认：宋体，12、左对齐，首航缩进15

```xml
<para>文字</para>
```

### img

插入一个图片。

```xml
<img src="path/to/file.img" />
```

### pagebreak

换页，这个标签不支持任何属性

```xml
<pagebreak />
```

### hrule

水平线条

```xml
<hrule width="1" precent="100" />
```

`width`用于定义线条的厚度，`precent`用于定义线条的宽度，用百分比。

### table

表格

```xml
<table columns="3,4,2" width="90">
    <cell align="center" colspan="2">内容</cell>
</table>
```

表格的`columns`属性有 2 个用途，一是确定列数，二是确定每列的宽度占比，例如上面的`3,4,2`表示有 3 列，第一列占 3/9 宽，第二列占 4/9 宽，第三列占 2/9 宽。`width`属性是 1--100 之间的百分比宽度。


## 內联标签

### span

默认：无，从父节点继承

这是一个內联标签，其必须嵌套在`title`, `section`, `para`中使用。

```xml
<para><span>some</span></para>
```

### break

这是一个內联标签，用于强制换行，不支持任何属性。

```xml
<para>第一行<break />第二行</para>
```

### value

默认：宋体、12、加粗、下划线

这是一个动态内容标签，通过 id 属性来引用 JSON 中的数据。

```xml
<para><value id="name" font-size="22" />，你好！</para>
```

value 是內联标签，并且可以通`font-size`,`font-xxx`来改变其默认排版风格。


## 风格

风格 | 用途 | 说明
---- | ---- | ----
indent | 缩进 | 单位为 pt
font-family | 字体名称 | 可以为`heiti` 和 `songti`
font-size | 字体大小 | 单位为 pt
font-style | 字体风格 | 可以为 bold, italic, underline 三种的组合，以逗号分隔，例如: `bold,italic` 表示粗斜体
space-before | 段前空间 | 单位为 pt
space-after | 段后空间 | 单位为 pt
align | 对齐方式 | left, right, center
sub | 下标 | 字体大小为 8pt，下移 3pt
super | 上标 | 字体大小为 8pt，上移 6pt
