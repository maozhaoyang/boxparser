package com.boxparser.html.nodes;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.boxparser.html.nodes.select.Collector;
import com.boxparser.html.nodes.select.Evaluator;
import com.boxparser.html.util.StringUtil;
import com.boxparser.html.util.Validate;
import com.boxparser.parse.Tag;
import com.boxparser.parse.HtmlParser.OutputSettings;

public class Element extends Node {

    private static final long serialVersionUID = 1L;
    private Tag         tag;
    private Set<String> classNames;

    @Override
    public String nodeName() {
        return tag.getName();
    }

    /**
     * Get the name of the tag for this element. E.g. {@code div}
     * 
     * @return the tag name
     */
    public String tagName() {
        return tag.getName();
    }

    public Element(Tag tag, Attributes attributes){
        super(attributes);

        Validate.notNull(tag);
        this.tag = tag;
    }

    /**
     * Create a new Element from a tag and a base URI.
     * 
     * @param tag element tag
     * @param baseUri the base URI of this element. It is acceptable for the base URI to be an empty string, but not
     * null.
     * @see Tag#valueOf(String)
     */
    public Element(Tag tag){
        this(tag, new Attributes());
    }

    public Element(){
        this(null, new Attributes());
    }

    public Element appendChild(Node child) {
        Validate.notNull(child);

        addChildren(child);
        return this;
    }

    public Element children(List<Node> children) {
        Validate.notNull(children);

        empty();
        for (Node child : children) {
            appendChild(child);
        }

        return this;
    }

    @Override
    public final Element parent() {
        return (Element) parentNode;
    }

    @Override
    public Element before(Node node) {
        return (Element) super.before(node);
    }

    public Element attr(String attributeKey, String attributeValue) {
        super.attr(attributeKey, attributeValue);
        return this;
    }

    @Override
    void outerHtmlHead(StringBuilder accum, int depth, OutputSettings out) {
        accum.append("<").append(tagName());
        attributes.html(accum, out);

        if (childNodes.isEmpty() && tag.isSelfClosing()) accum.append("/>");
        else accum.append(">");
    }

    @Override
    void outerHtmlTail(StringBuilder accum, int depth, OutputSettings out) {
        if (!(childNodes.isEmpty() && tag.isSelfClosing())) {
            accum.append("</").append(tagName()).append(">");
        }
    }

    public List<Element> getElementsByAttribute(String key) {
        Validate.notEmpty(key);
        key = key.trim().toLowerCase();

        return Collector.collect(new Evaluator.Attribute(key), this);
    }
    
    public List<Element> getElementsByTagName(String tagName,String attrKey,String attrValue) {
        Validate.notEmpty(tagName);
        tagName = tagName.trim().toLowerCase();

        return Collector.collect(new Evaluator.TagName(tagName,attrKey,attrValue), this);
    }

    public List<Element> getElementsByAttributePrefix(String key) {
        Validate.notEmpty(key);
        return Collector.collect(new Evaluator.AttributeStarting(key), this);
    }

    public List<Element> getElementsByAttributes(String... keys) {
        Validate.noNullElements(keys);
        return Collector.collect(new Evaluator.MultiAttribute(keys), this);
    }
    
    public List<Element> getElementsByEvaluator(Evaluator evaluator) {
        Validate.notNull(evaluator);
        return Collector.collect(evaluator, this);
    }    

    public List<Element> getElementsByClass(String className) {
        Validate.notEmpty(className);
        return Collector.collect(new Evaluator.Class(className), this);
    }

    public Element getElementByAttribute(String key) {
        Validate.notEmpty(key);

        Element element = Collector.first(new Evaluator.Attribute(key), this);
        return element;
    }

    public String id() {
        String id = attr("id");
        return id == null ? "" : id;
    }

    public Element getElementById(String id) {
        Validate.notEmpty(id);

        Element elm = Collector.first(new Evaluator.Id(id), this);
        return elm;
    }

    public String value() {
        if (tagName().equals("textarea")) return text();
        else return attr("value");
    }

    public String escapeValue() {
        String val = value();
        if (val != null) {
            val = Entities.unescape(val, true);
        }
        return val;
    }

    public String text() {
        StringBuilder sb = new StringBuilder();
        text(sb);
        return sb.toString().trim();
    }

    private void text(StringBuilder accum) {
        appendWhitespaceIfBr(this, accum);

        for (Node child : childNodes) {
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                appendNormalisedText(accum, textNode);
            } else if (child instanceof Element) {
                Element element = (Element) child;
                if (accum.length() > 0 && element.isBlock() && !TextNode.lastCharIsWhitespace(accum)) accum.append(" ");
                element.text(accum);
            }
        }
    }

    public Element text(String text) {
        Validate.notNull(text);

        empty();
        TextNode textNode = new TextNode(text);
        appendChild(textNode);

        return this;
    }

    private static void appendWhitespaceIfBr(Element element, StringBuilder accum) {
        if (element.tag.getName().equals("br") && !TextNode.lastCharIsWhitespace(accum)) accum.append(" ");
    }

    public boolean isBlock() {
        return tag.isBlock();
    }

    private void appendNormalisedText(StringBuilder accum, TextNode textNode) {
        String text = textNode.getWholeText();

        if (!preserveWhitespace()) {
            text = TextNode.normaliseWhitespace(text);
            if (TextNode.lastCharIsWhitespace(accum)) text = TextNode.stripLeadingWhitespace(text);
        }
        accum.append(text);
    }

    boolean preserveWhitespace() {
        return tag.preserveWhitespace() || parent() != null && parent().preserveWhitespace();
    }

    public Element empty() {
        childNodes.clear();
        return this;
    }

    public boolean hasClass(String className) {
        Set<String> classNames = classNames();
        for (String name : classNames) {
            if (className.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public String className() {
        return attr("class");
    }

    public Set<String> classNames() {
        if (classNames == null) {
            String[] names = className().split("\\s+");
            classNames = new LinkedHashSet<String>(Arrays.asList(names));
        }
        return classNames;
    }

    public Element removeClass(String className) {
        Validate.notNull(className);

        Set<String> classes = classNames();
        classes.remove(className);
        classNames(classes);

        return this;
    }

    public Element classNames(Set<String> classNames) {
        Validate.notNull(classNames);
        attributes.put("class", StringUtil.join(classNames, " "));
        return this;
    }
}
