package com.boxparser.html.nodes.select;

import com.boxparser.html.nodes.Node;

public interface NodeVisitor {

    /**
     * Callback for when a node is first visited.
     * 
     * @param node the node being visited.
     * @param depth the depth of the node, relative to the root node. E.g., the root node has depth 0, and a child node
     * of that will have depth 1.
     * @return true exit iterate
     */
    public boolean head(Node node, int depth);

    /**
     * Callback for when a node is last visited, after all of its descendants have been visited.
     * 
     * @param node the node being visited.
     * @param depth the depth of the node, relative to the root node. E.g., the root node has depth 0, and a child node
     * of that will have depth 1.
     * @return true exit iterate
     */
    public boolean tail(Node node, int depth);

    public final static boolean NEXT = false;
    public final static boolean EXIT = true;

}
