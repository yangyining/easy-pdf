package com.janeluo.easypdf;

import org.xml.sax.Attributes;

import java.util.*;

public class TextTable {
    private final Map<String, String> attrs;
    private final List<TextChunk> cells;

    public TextTable() {
        attrs = new HashMap<>();
        cells = new ArrayList<>();
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void addAttrs(Attributes attrs) {
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.getQName(i);
            String value = attrs.getValue(i);
            this.attrs.put(name, value);
        }
    }

    public void addAttrs(Map<String, String> attrs) {
        Set<String> keys = attrs.keySet();
        for (String key : keys) {
            this.attrs.put(key, attrs.get(key));
        }
    }

    public void addAttr(String key, String value) {
        if (key != null && value != null) {
            attrs.put(key, value);
        }
    }

    public List<TextChunk> getCells() {
        return cells;
    }

    public void addCell(TextChunk chunk) {
        cells.add(chunk);
    }

    public TextChunk lastCell() {
        int last = cells.size() - 1;
        return cells.get(last);
    }

}
