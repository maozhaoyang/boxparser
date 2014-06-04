package com.boxparser.parse;

import java.util.Iterator;
import java.util.List;

import com.boxparser.html.nodes.Document;
import com.boxparser.html.nodes.Element;
import com.boxparser.html.nodes.Node;
import com.boxparser.html.nodes.TextNode;
import com.boxparser.html.util.StringUtil;

public class DataSourceTreeBuilder extends TreeBuilder {

    private DataSourceTreeBuilderState state;               // the current state
    private DataSourceTreeBuilderState originalState;       // original / marked state
    private Document                   doc = new Document(Tag.valueOf("#root"));

    @Override
    protected boolean process(Token token) {
        this.currentToken = token;
        return this.state.process(token, this);
    }

    Document parse(String input, List<ParseError> errors) {
        state = DataSourceTreeBuilderState.all;
        initialiseParse(input, errors);
        stack.push(doc);

        runParser();
        return doc;
    }

    DataSourceTreeBuilderState state() {
        return state;
    }

    public Document document() {
        return doc;
    }

    void transition(DataSourceTreeBuilderState state) {
        this.state = state;
    }

    void markInsertionMode() {
        originalState = state;
    }

    DataSourceTreeBuilderState originalState() {
        return originalState;
    }

    void error(DataSourceTreeBuilderState state) {
        errors.add(new ParseError(reader.pos(), "Unexpected token [%s] when in state [%s]", currentToken.tokenType(),
                                  state));
    }

    void insert(Token.Doctype doctype) {
        Node node = new TextNode(doctype.toString(), false);
        currentElement().addChildren(node);
    }

    void insert(Token.Character characterToken) {
        Node node = new TextNode(characterToken.getData());
        currentElement().addChildren(node);
    }

    void insert(Token.Comment commentToken) {
        Node node = new TextNode(commentToken.toString());
        insertNode(node);
    }

    private void insertNode(Node node) {
        currentElement().addChildren(node);
    }

    Element insert(Token.StartTag startTag) {
        // handle empty unknown tags
        // when the spec expects an empty tag, will directly hit insertEmpty, so
        // won't generate fake end tag.
        if (startTag.isSelfClosing()) { // && !Tag.isKnownTag(startTag.name())
            Element el = insertEmpty(startTag);
            // process(new Token.EndTag(el.tagName())); // ensure we get out of
            // whatever state we are
            // in
            return el;
        }

        Element el = new Element(Tag.valueOf(startTag.name()), startTag.attributes);
        insert(el);
        return el;
    }

    Element insertEmpty(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name());
        Element el = new Element(tag, startTag.attributes);
        insertNode(el);
        if (startTag.isSelfClosing()) {
            tokeniser.acknowledgeSelfClosingFlag();
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing
            // for output
            tag.setSelfClosing();
        }
        return el;
    }

    void insert(Element el) {
        insertNode(el);
        stack.add(el);
    }

    public boolean inButtonScope(String targetName) {
        return inScope(targetName, new String[] { "button" });
    }

    boolean inScope(String targetName) {
        return inScope(targetName, null);
    }

    void generateImpliedEndTags() {
        generateImpliedEndTags(null);
    }

    void generateImpliedEndTags(String excludeTag) {
        while ((excludeTag != null && !currentElement().nodeName().equals(excludeTag))
               && StringUtil.in(currentElement().nodeName(), "dd", "dt", "li", "option", "optgroup", "p", "rp", "rt"))
            pop();
    }

    void popStackToClose(String elName) {
        Iterator<Element> it = stack.descendingIterator();
        while (it.hasNext()) {
            Element next = it.next();
            if (next.nodeName().equals(elName)) {
                it.remove();
                break;
            } else {
                it.remove();
            }
        }
    }

}
