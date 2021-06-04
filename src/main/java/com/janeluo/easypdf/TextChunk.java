/* Copyright (c) 2021 janeluo
 * easy-pdf is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.janeluo.easypdf;

import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextChunk {
    private String contents;
    private final Map<String, String> attrs;
    private boolean isValue;

    public TextChunk() {
        attrs = new HashMap<>();
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String chars) {
        this.contents = chars;
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

    public boolean isValue() {
        return isValue;
    }

    public void setIsValue(boolean isValue) {
        this.isValue = isValue;
    }

    @Override
    public TextChunk clone() {
        TextChunk chunk = new TextChunk();

        Set<String> keys = this.attrs.keySet();
        for (String key : keys) {
            chunk.attrs.put(key, this.attrs.get(key));
        }
        chunk.contents = this.contents;
        chunk.isValue = this.isValue;
        return chunk;
    }

}
