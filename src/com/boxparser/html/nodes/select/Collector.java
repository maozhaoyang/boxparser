package com.boxparser.html.nodes.select;

import java.util.ArrayList;
import java.util.List;

import com.boxparser.html.nodes.Element;
import com.boxparser.html.nodes.Node;

public class Collector {

    private Collector(){
    }

    public static List<Element> collect(Evaluator eval, Element root) {
        List<Element> elements = new ArrayList<Element>();
        new NodeTraversor(new Accumulator(root, elements, eval)).traverse(root);
        return elements;
    }

    public static Element first(Evaluator eval, Element root) {
        List<Element> elements = new ArrayList<Element>(1);
        new NodeTraversor(new Accumulator(root, elements, eval, true)).find(root);
        return elements.isEmpty() ? null : elements.remove(0);
    }

    private static class Accumulator implements NodeVisitor {

        private final Element       root;
        private final List<Element> elements;
        private final Evaluator     eval;
        private final boolean       matchFirst;

        Accumulator(Element root, List<Element> elements, Evaluator eval, boolean matchFirst){
            this.root = root;
            this.elements = elements;
            this.eval = eval;
            this.matchFirst = matchFirst;
        }

        Accumulator(Element root, List<Element> elements, Evaluator eval){
            this(root, elements, eval, false);
        }

        public boolean head(Node node, int depth) {
            if (node instanceof Element) {
                Element el = (Element) node;
                if (eval.matches(root, el)) {
                    elements.add(el);
                    if (matchFirst) {
                        return EXIT;
                    }
                }
            }
            return NEXT;
        }

        public boolean tail(Node node, int depth) {
            return NEXT;
        }
    }
}
