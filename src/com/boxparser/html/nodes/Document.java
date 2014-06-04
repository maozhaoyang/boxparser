package com.boxparser.html.nodes;

import com.boxparser.parse.Tag;
import com.boxparser.parse.HtmlParser.OutputSettings;

public class Document extends Element {

    private static final long serialVersionUID = 1L;
    private transient OutputSettings outputSettings = null;
    
    public Document(){
        super();
    }

    public Document(Tag tag){
        super(tag);
    }

    public String outerHtml() {
        String html = null;
        if (childNodes != null) {
            StringBuffer buff = new StringBuffer();
            for (Node node : childNodes) {
                buff.append(node.toString());
            }
            html = buff.toString();
        }
        return html;
    }
    
    public OutputSettings outputSettings() {
        if (outputSettings == null) {
            outputSettings = new OutputSettings();
        }
        return outputSettings;
    }
}
