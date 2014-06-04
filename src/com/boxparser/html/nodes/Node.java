package com.boxparser.html.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boxparser.html.nodes.select.NodeTraversor;
import com.boxparser.html.nodes.select.NodeVisitor;
import com.boxparser.html.util.Validate;
import com.boxparser.parse.HtmlParser.OutputSettings;

public abstract class Node implements Cloneable, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    Node       parentNode;
    List<Node> childNodes;
    Attributes attributes;
    int        siblingIndex;
    Map        memory;      // 用来在内存中保留一些数据，跟Dom树没有关系

    protected Node(Attributes attributes){
        Validate.notNull(attributes);

        childNodes = new ArrayList<Node>(4);
        this.attributes = attributes;
    }

    /**
     * Default constructor. Doesn't setup base uri, children, or attributes; use with caution.
     */
    protected Node(){
        childNodes = Collections.emptyList();
        attributes = null;
    }

    public abstract String nodeName();

    public List<Node> childNodes() {
        return Collections.unmodifiableList(childNodes);
    }

    public Node[] childNodesAsArray() {
        return childNodes.toArray(new Node[childNodes().size()]);
    }

    public void addChildren(Node... children) {
        // most used. short circuit addChildren(int), which hits reindex
        // children and array copy
        for (Node child : children) {
            reparentChild(child);
            childNodes.add(child);
            child.setSiblingIndex(childNodes.size() - 1);
        }
    }
    public void addChildren(List<Node> children) {
        // most used. short circuit addChildren(int), which hits reindex
        // children and array copy
        for (Node child : children) {
            reparentChild(child);
            childNodes.add(child);
            child.setSiblingIndex(childNodes.size() - 1);
        }
    }

    protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        for (int i = children.length - 1; i >= 0; i--) {
            Node in = children[i];
            reparentChild(in);
            childNodes.add(index, in);
        }
        reindexChildren();
    }

    public void insertBefore(Node... children) {
        Validate.noNullElements(children);
        int index = this.siblingIndex();
        for (int i = children.length - 1; i >= 0; i--) {
            this.parent().addChildren(index, children);
        }
    }

    private void reparentChild(Node child) {
        if (child.parentNode != null) child.parentNode.removeChild(child);
        child.setParentNode(this);
    }

    private void reindexChildren() {
        for (int i = 0; i < childNodes.size(); i++) {
            childNodes.get(i).setSiblingIndex(i);
        }
    }

    protected void removeChild(Node out) {
        Validate.isTrue(out.parentNode == this);
        int index = out.siblingIndex();
        childNodes.remove(index);
        reindexChildren();
        out.parentNode = null;
    }

    public void remove() {
        //mao add 2012.11.1
        if (parentNode != null) {
            Validate.notNull(parentNode);
            parentNode.removeChild(this);
        }
    }

    public Node before(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex(), node);
        return this;
    }

    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);
        return attributes.hasKey(attributeKey);
    }

    @Override
    public Node clone() {
        return doClone(null); // splits for orphan
    }

    protected Node doClone(Node parent) {
        Node clone;
        try {
            clone = (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        clone.parentNode = parent; // can be null, to create an orphan split
        clone.siblingIndex = parent == null ? 0 : siblingIndex;
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.childNodes = new ArrayList<Node>(childNodes.size());
        clone.memory = memory != null ? new HashMap(memory) : null;
        for (Node child : childNodes)
            clone.childNodes.add(child.doClone(clone)); // clone() creates orphans, doClone() keeps parent

        return clone;
    }

    protected void setParentNode(Node parentNode) {
        if (this.parentNode != null) this.parentNode.removeChild(this);
        this.parentNode = parentNode;
    }

    public int siblingIndex() {
        return siblingIndex;
    }

    protected void setSiblingIndex(int siblingIndex) {
        this.siblingIndex = siblingIndex;
    }

    public String attr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributes.hasKey(attributeKey)) return attributes.get(attributeKey);

        return "";
    }

    public String escapeAttr(String attributeKey) {
        String val = attr(attributeKey);
        return val != null ? Entities.unescape(val, true) : "";
    }

    public Attributes attributes() {
        return attributes;
    }

    public Node attr(String attributeKey, String attributeValue) {
        attributes.put(attributeKey, attributeValue);
        return this;
    }

    public Node escapeAttr(String attributeKey, String attributeValue) {
        attributes.put(attributeKey, Entities.escape(attributeValue));
        return this;
    }

    public Node attr(String attributeKey, String attributeValue, char beforeChar) {
        attributes.put(attributeKey, attributeValue);
        return this;
    }

    public Node parent() {
        return parentNode;
    }

    /**
     * to HTML
     */

    public Node childNode(int index) {
        return childNodes.get(index);
    }

    public Node nextSibling() {
        if (parentNode == null) return null; // root

        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        if (siblings.size() > index + 1) return siblings.get(index + 1);
        else return null;
    }

    public String toString() {
        return outerHtml();
    }

    public String outerHtml() {
        StringBuilder accum = new StringBuilder(128);
        outerHtml(accum);
        return accum.toString();
    }
    public String innerHtml(){
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

    protected void outerHtml(StringBuilder accum) {
        new NodeTraversor(new OuterHtmlVisitor(accum, OutputSettings.defaultOutputSettings())).traverse(this);
    }

    abstract void outerHtmlHead(StringBuilder accum, int depth, OutputSettings out);

    abstract void outerHtmlTail(StringBuilder accum, int depth, OutputSettings out);

    private static class OuterHtmlVisitor implements NodeVisitor {

        private StringBuilder  accum;
        private OutputSettings out;

        OuterHtmlVisitor(StringBuilder accum, OutputSettings out){
            this.accum = accum;
            this.out = out;
        }

        public boolean head(Node node, int depth) {
            node.outerHtmlHead(accum, depth, out);
            return NEXT;
        }

        public boolean tail(Node node, int depth) {
            if (!node.nodeName().equals("#text")) // saves a void hit.
            node.outerHtmlTail(accum, depth, out);
            return NEXT;
        }
    }

    public Node removeAttr(String attributeKey) {
        Validate.notNull(attributeKey);
        attributes.remove(attributeKey);
        return this;
    }

    protected void replaceChild(Node out, Node in) {
        Validate.isTrue(out.parentNode == this);
        Validate.notNull(in);
        if (in.parentNode != null) in.parentNode.removeChild(in);

        Integer index = out.siblingIndex();
        childNodes.set(index, in);
        in.parentNode = this;
        in.setSiblingIndex(index);
        out.parentNode = null;
    }

    public void replace(Node replaceNode) {
        if (this.parentNode != null) {
            this.parentNode.replaceChild(this, replaceNode);
        }
    }

    public Node memory(String key, Object value) {
        if (memory == null) {
            memory = new HashMap();
        }
        memory.put(key, value);
        return this;
    }

    public Object memory(String key) {
        return memory != null ? memory.get(key) : null;
    }

}
