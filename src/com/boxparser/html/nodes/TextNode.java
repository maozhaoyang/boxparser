package com.boxparser.html.nodes;

import com.boxparser.html.util.StringUtil;
import com.boxparser.parse.HtmlParser.OutputSettings;

public class TextNode extends Node {

    private static final String TEXT_KEY = "text";
    String                      text;
    boolean                     escape   = false;

    public TextNode(String text){
        this.text = text;
    }

    public TextNode(String text, boolean escape){
        this.text = text;
        this.escape = escape;
    }

    public String nodeName() {
        return "#text";
    }

    public String text() {
        return normaliseWhitespace(getWholeText());
    }

    public TextNode text(String text) {
        this.text = text;
        if (attributes != null) attributes.put(TEXT_KEY, text);
        return this;
    }

    public String getWholeText() {
        return attributes == null ? text : attributes.get(TEXT_KEY);
    }

    static String normaliseWhitespace(String text) {
        text = StringUtil.normaliseWhitespace(text);
        return text;
    }

    @Override
    void outerHtmlHead(StringBuilder accum, int depth, OutputSettings out) {
        String html = getWholeText();
        if(escape){
            html = Entities.escape(html, out);
        }
        accum.append(html);
    }

    @Override
    void outerHtmlTail(StringBuilder accum, int depth, OutputSettings out) {

    }
    
    /**
     * È¥µô¿Õ°××Ö·û
     */
    public void minimize() {
        if (attributes != null && attributes.get(TEXT_KEY) != null) {
            attributes.put(TEXT_KEY, attributes.get(TEXT_KEY).trim());
        } else if (text != null) {
            text = text.trim();
        }
    }    
    
    static boolean lastCharIsWhitespace(StringBuilder sb) {
        return sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ';
    }
    
    static String stripLeadingWhitespace(String text) {
        return text.replaceFirst("^\\s+", "");
    }
}
