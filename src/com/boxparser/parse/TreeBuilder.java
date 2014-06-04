package com.boxparser.parse;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.boxparser.html.nodes.Document;
import com.boxparser.html.nodes.Element;
import com.boxparser.html.nodes.Node;
import com.boxparser.html.util.StringUtil;
import com.boxparser.html.util.Validate;

public abstract class TreeBuilder {

    CharacterReader               reader;
    DataSourceTokeniser           tokeniser;
    protected LinkedList<Element> stack;
    protected Token               currentToken;
    protected List<ParseError>    errors;

    protected void initialiseParse(String input, List<ParseError> errors) {
        Validate.notNull(input, "String input must not be null");

        reader = new CharacterReader(input);
        tokeniser = new DataSourceTokeniser(reader, errors);
        stack = new LinkedList<Element>();

        this.errors = errors;
    }

    abstract Document parse(String input, List<ParseError> errors);

    protected void runParser() {
        while (true) {
            Token token = tokeniser.read();
            process(token);

            if (token.type == Token.TokenType.EOF) break;
        }
    }

    protected abstract boolean process(Token token);

    protected Node currentElement() {
        return stack.getLast();
    }

    Node pop() {
        return stack.pollLast();
    }

    void push(Element element) {
        stack.add(element);
    }

    public LinkedList<Element> getStack() {
        return stack;
    }

    public boolean isSpecial(Element el) {
        String name = el.nodeName();
        return StringUtil.in(name, "address", "applet", "area", "article", "aside", "base", "basefont", "bgsound",
                             "blockquote", "body", "br", "button", "caption", "center", "col", "colgroup", "command",
                             "dd", "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure",
                             "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head",
                             "header", "hgroup", "hr", "html", "iframe", "img", "input", "isindex", "li", "link",
                             "listing", "marquee", "menu", "meta", "nav", "noembed", "noframes", "noscript", "object",
                             "ol", "p", "param", "plaintext", "pre", "script", "section", "select", "style", "summary",
                             "table", "tbody", "td", "textarea", "tfoot", "th", "thead", "title", "tr", "ul", "wbr",
                             "xmp");
    }

    public boolean inScope(String targetName, String[] extras) {
        return inSpecificScope(targetName, new String[] { "applet", "caption", "html", "table", "td", "th", "marquee",
                "object" }, extras);
    }

    private boolean inSpecificScope(String targetName, String[] baseTypes, String[] extraTypes) {
        return inSpecificScope(new String[] { targetName }, baseTypes, extraTypes);
    }

    private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        Iterator<Element> it = stack.descendingIterator();
        while (it.hasNext()) {
            Element el = it.next();
            String elName = el.nodeName();
            if (StringUtil.in(elName, targetNames)) return true;
            if (StringUtil.in(elName, baseTypes)) return false;
            if (extraTypes != null && StringUtil.in(elName, extraTypes)) return false;
        }
//        Validate.fail("Should not be reachable");
        return false;
    }

    boolean inTableScope(String targetName) {
        return inSpecificScope(targetName, new String[] { "html", "table" }, null);
    }
}
