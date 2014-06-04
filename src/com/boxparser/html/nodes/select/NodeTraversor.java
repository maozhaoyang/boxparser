package com.boxparser.html.nodes.select;

import com.boxparser.html.nodes.Node;

public class NodeTraversor {

    private NodeVisitor visitor;

    /**
     * Create a new traversor.
     * 
     * @param visitor a class implementing the {@link NodeVisitor} interface, to be called when visiting each node.
     */
    public NodeTraversor(NodeVisitor visitor){
        this.visitor = visitor;
    }

    /**
     * Start a depth-first traverse of the root and all of its descendants.
     * 
     * @param root the root node point to traverse.
     */
    public void traverse(Node root) {
        Node node = root;
        int depth = 0;

        while (node != null) {
            visitor.head(node, depth);
            if (node.childNodes().size() > 0) {
                node = node.childNode(0);
                depth++;
            } else {
                while (node.nextSibling() == null && depth > 0) {
                    visitor.tail(node, depth);
                    node = node.parent();
                    depth--;
                }
                visitor.tail(node, depth);
                if (node == root) break;
                node = node.nextSibling();
            }
        }
    }

    public void find(Node root) {
        Node node = root;
        int depth = 0;

        root: while (node != null) {
            if (visitor.head(node, depth)) {
                break;
            }
            if (node.childNodes().size() > 0) {
                node = node.childNode(0);
                depth++;
            } else {
                while (node.nextSibling() == null && depth > 0) {
                    if (visitor.tail(node, depth)) {
                        break root;
                    }
                    node = node.parent();
                    depth--;
                }
                if (visitor.tail(node, depth)) {
                    break;
                }
                if (node == root) break;
                node = node.nextSibling();
            }
        }
    }
}
