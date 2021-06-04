# API 示例

在源码中提供了单元测试案例，可以作为参考。

## 转换 XML 为 PDF

通过`TextParser`对象来完成转换，下面是一段示例：

```java
TextParser parser = new TextParser(
          new FileInputStream("tests/test.xml"),
          new FileInputStream("tests/test.json"),
          new FileOutputStream("tests/test.pdf"));
parser.genPDF();
```

`TextParser`原型为：

```java
public TextParser(InputStream xml_stream,
                  InputStream json_stream,
                  OutputStream out_stream);
```

其中`xml_stream`是输入 XML 模板流对象，`json_stream`是输入 JSON 数据流对象，输出 PDF 文件写入`out_stream` 中。

建立`TextParser`对象后，直接调用`genPDF()`即可。

## 转换 XML 为 HTML

和上面转换为 PDF 类似，只不过是调用`genHTML()`而不是调用`genPDF()`。

在输出为 HTML 时，可以设置一些额外的参数，包括：

* CSS 链接；
* JS 链接；
* HTML 声明；
* 额外 HTML 代码；

请参考`TextParser`对象提供的 public 接口，这些接口都是自说明的。

## 转换 DOC 为 XML

这是通过`DocReader`对象完成，下面是一段示例：

```java
InputStream doc_stream = new FileInputStream("tests/test.doc");
OutputStream xml_stream = new FileOutputStream("tests/test.xml");
OutputStream json_stream = new FileOutputStream("tests/test.json");

DocReader reader = new DocReader();
reader.setAutoTitle(true);
reader.ignoreBlankPara(true);
reader.read(doc_stream, xml_stream, json_stream);
```
