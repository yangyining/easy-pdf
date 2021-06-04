package com.janeluo.easypdf;

import com.alibaba.fastjson.JSONObject;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输出 HTML 文档
 */
public class HTMLDoc extends TextDoc {
    static public final int TYPE_INPUT = 1;
    static public final int TYPE_COMBO = 2;

    private boolean isOpen = false;
    private JSONObject jsonObject;
    private List<String> cssPaths;
    private List<String> jsPaths;
    private String declare = null;
    private String extra = null;
    private int type = TYPE_INPUT;

    private String htmlOpen = ""
            + "<!DOCTYPE html>\n"
            + "<html>\n"
            + "  <head>\n"
            + "    <title>__TITLE__</title>\n"
            + "    <meta name=\"author\" content=\"Lucky Byte, Inc.\"/>\n"
            + "    <meta name=\"generator\" content=\"TextPDF\" />\n"
            + "    <meta name=\"description\" content=\"TextPDF HTML Editor\" />\n"
            + "    <meta name=\"keywords\" content=\"TextPDF,PDF,Template\" />\n"
            + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=__ENCODING__\">\n"
            + "    __CSS_URL__\n"
            + "    __JS_URL__\n"
            + "  </head>\n"
            + "  <body>\n";

    private String htmlClose = "  </body>\n</html>\n";


    public HTMLDoc(OutputStream out_stream) {
        super(out_stream);
    }

    public void setJSONObject(JSONObject json_object) {
        this.jsonObject = json_object;
    }

    public void setLinkPaths(List<String> css_paths, List<String> js_paths) {
        this.cssPaths = css_paths;
        this.jsPaths = js_paths;
    }

    public void setDeclare(String declare) {
        this.declare = declare;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setType(int type) {
        this.type = type;
    }

    private boolean writeStream(String string) {
        try {
            outputStream.write(string.getBytes(encoding));
            return true;
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unsupported encoding.");
            return false;
        } catch (IOException e) {
            System.err.println("Write to html stream failed.");
            return false;
        }
    }

    private void substituteDeclare() {
        if (declare != null) {
            htmlOpen = htmlOpen.replace("<!DOCTYPE html>", declare);
        }
    }

    private void substituteTitle() {
        if (jsonObject != null) {
            if (jsonObject.containsKey("title")) {
                Object value = jsonObject.get("title");
                if (value instanceof String) {
                    htmlOpen = htmlOpen.replace("__TITLE__",
                            Util.escapeHtmlString((String) value));
                }
            }
        }
        htmlOpen = htmlOpen.replace("__TITLE__", "");
    }

    private void substituteCSSLinks() {
        if (cssPaths != null) {
            StringBuilder builder = new StringBuilder();
            for (String path : cssPaths) {
                builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                builder.append(Util.escapeHtmlString(path));
                builder.append("\"/>\n");
            }
            htmlOpen = htmlOpen.replace("__CSS_URL__",
                    builder.toString().trim());
        } else {
            htmlOpen = htmlOpen.replace("__CSS_URL__", "");
        }
    }

    private void substituteJSLinks() {
        if (jsPaths != null) {
            StringBuilder builder = new StringBuilder();
            for (String path : jsPaths) {
                builder.append("    <script src=\"");
                builder.append(Util.escapeHtmlString(path));
                builder.append("\"></script>\n");
            }
            htmlOpen = htmlOpen.replace("__JS_URL__",
                    builder.toString().trim());
        } else {
            htmlOpen = htmlOpen.replace("__JS_URL__", "");
        }
    }

    @Override
    public boolean open() {
        if (outputStream == null) {
            return false;
        }

        substituteDeclare();
        substituteTitle();
        htmlOpen = htmlOpen.replace("__ENCODING__", encoding);
        substituteCSSLinks();
        substituteJSLinks();

        isOpen = true;
        return writeStream(htmlOpen);
    }

    @Override
    public void close() {
        if (isOpen && outputStream != null) {
            if (extra != null) {
                writeStream(extra);
            }
            writeStream(htmlClose);
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    private final Map<String, String> block_labels = new HashMap<String, String>() {
        {
            put("title", "h1");
            put("chapter", "h2");
            put("section", "h3");
            put("para", "p");
        }
    };

    private String getHtmlLabel(String blockName) {

        return block_labels.get(blockName.toLowerCase());
    }

    private Map<String, String> getHtmlAttrs(TextChunk chunk,
                                             boolean blockElement) {
        Map<String, String> chunkAttrs = chunk.getAttrs();
        Map<String, String> htmlAttrs = new HashMap<>();
        StringBuilder styleString = new StringBuilder();

        for (String key : chunkAttrs.keySet()) {
            if ("font-style".equalsIgnoreCase(key)) {
                String[] styles = chunkAttrs.get(key).split(",");
                for (String style : styles) {
                    String styleName = style.trim();
                    if ("bold".equalsIgnoreCase(styleName)) {
                        styleString.append("font-weight: bold; ");
                    } else if ("italic".equalsIgnoreCase(styleName)) {
                        styleString.append("font-style: italic; ");
                    } else if ("underline".equalsIgnoreCase(styleName)) {
                        styleString.append("font-decoration: underline; ");
                    }
                }
            } else if (blockElement && "indent".equalsIgnoreCase(key)) {
                styleString.append("text-indent: ").append(chunkAttrs.get(key)).append("px; ");
            } else if (blockElement && "align".equalsIgnoreCase(key)) {
                styleString.append("text-align: ").append(chunkAttrs.get(key)).append("; ");
            }
        }
        if (styleString.toString().length() > 0) {
            htmlAttrs.put("style", styleString.toString());
        }
        return htmlAttrs;
    }

    private String htmlCharEscape(String contents) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < contents.length(); i++) {
            char ch = contents.charAt(i);
            if (ch == '\n') {
                builder.append("<br/>");
            } else {
                String escape = Util.escapeHtmlChars(ch);
                if (escape != null) {
                    builder.append(escape);
                } else {
                    builder.append(ch);
                }
            }
        }
        return builder.toString();
    }

    private void writeValue(TextChunk chunk) {
        Map<String, String> attrs = chunk.getAttrs();
        String id = attrs.get("id");
        String minlen = attrs.get("minlen");

        switch (type) {
            case TYPE_INPUT:
                writeStream("<input type=\"text\"");
                if (id != null && id.length() > 0) {
                    writeStream(" id=\"" + id + "\" name=\"" + id + "\"");
                }
                if (minlen != null && minlen.length() > 0) {
                    writeStream(" size=\"" + minlen + "\"");
                }
                writeStream(" />");
                break;
            case TYPE_COMBO:
                writeStream("<input type=\"text\"");
                if (minlen != null && minlen.length() > 0) {
                    writeStream(" size=\"" + minlen + "\"");
                }
                writeStream(" readonly=\"readonly\"");
                writeStream(" />");

                writeStream("<select");
                if (id != null && id.length() > 0) {
                    writeStream(" id=\"" + id + "\" name=\"" + id + "\"");
                }
                writeStream(">\n");
                writeStream("<option value =\"1\">必输</option>\n");
                writeStream("<option value =\"0\">可选</option>\n");
                writeStream("</select>\n");
                break;
            default:
                break;
        }
    }

    @Override
    public void writeBlock(String blockName, List<TextChunk> chunkList)
            throws IOException {
        if (outputStream == null || chunkList.size() == 0) {
            return;
        }

        String label = getHtmlLabel(blockName);
        if (label == null) {
            System.err.println("unable map block name '"
                    + blockName + "'to html label.");
            return;
        }

        for (int i = 0; i < chunkList.size(); i++) {
            TextChunk chunk = chunkList.get(i);
            if (chunk.isValue()) {
                writeValue(chunk);
                continue;
            }
            if (i == 0) {
                writeStream("    <" + label +
                        " class=\"" + blockName + "\"");
            } else {
                writeStream("<span");
            }
            Map<String, String> htmlAttrs = getHtmlAttrs(chunk, i == 0);
            for (String key : htmlAttrs.keySet()) {
                writeStream(" " + key + "=\"" + htmlAttrs.get(key) + "\"");
            }
            writeStream(">");
            writeStream(htmlCharEscape(chunk.getContents()));
            if (i > 0) {
                writeStream("</span>");
            }
        }
        writeStream("</" + label + ">\n");
    }

    @Override
    public void newPage() {
        writeStream("    <hr/>\n");
    }

    @Override
    public void addHrule(Attributes attrs) {
        writeStream("    <hr/>\n");
    }

    @Override
    public void addImage(Attributes attrs) {
        String value = attrs.getValue("src");
        if (value == null) {
            System.err.println("img missing src attribute.");
            return;
        }
        writeStream("<img src=\"" + Util.escapeHtmlString(value) + "\"/>");
    }

    @Override
    public void writeTable(TextTable table) {
        if (!isOpen() || table == null) {
            return;
        }
        Map<String, String> attrs = table.getAttrs();
        int[] columns = null;

        String value = attrs.get("columns");
        if (value != null) {
            try {
                String[] array = value.split(",");
                columns = new int[array.length];
                int total = 0;
                for (int i = 0; i < array.length; i++) {
                    columns[i] = Integer.parseInt(array[i]);
                    total += columns[i];
                }
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i] * 100 / total;
                }
            } catch (Exception ex) {
                System.err.println("column must has a integer value");
            }
        }
        if (columns == null) {
            return;
        }

        writeStream("    <table border=\"2\" width=\"100%\">\n");
        for (int i = 0; i < table.getCells().size(); i++) {
            int colno = i % columns.length;
            if (colno == 0) {
                if (i > 0) {
                    writeStream("      </tr>\n");
                }
                writeStream("      <tr>\n");
            }
            if (columns[colno] > 0) {
                TextChunk textChunk = table.getCells().get(i);
                writeStream("        <td width=\"" + columns[colno] + "%\">" +
                        textChunk.getContents() + "</td>\n");
            }
        }
        writeStream("      </tr>\n");
        writeStream("    </table>\n");
    }

}
