package com.boxparser.html.nodes.select;

import java.util.Arrays;
import java.util.List;

import com.boxparser.html.nodes.Element;
import com.boxparser.html.util.StringUtil;

public abstract class Evaluator {

    public Evaluator(){
    }

    public abstract boolean matches(Element root, Element element);

    public static final class Attribute extends Evaluator {

        private String key;

        public Attribute(String key){
            this.key = key;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key);
        }

        @Override
        public String toString() {
            return String.format("[%s]", key);
        }

    }

    public static final class Id extends Evaluator {

        private String id;

        public Id(String id){
            this.id = id;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return (id.equals(element.id()));
        }

        @Override
        public String toString() {
            return String.format("#%s", id);
        }

    }

    public static final class MultiAttribute extends Evaluator {

        private String[] keys;

        public MultiAttribute(String... keys){
            this.keys = keys;
        }

        @Override
        public boolean matches(Element root, Element element) {
            List<com.boxparser.html.nodes.Attribute> values = element.attributes().asList();
            for (com.boxparser.html.nodes.Attribute attribute : values) {
                if (StringUtil.in(attribute.getKey(), keys)) return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("[^%s]", Arrays.toString(keys));
        }

    }

    public static final class TagName extends Evaluator {

        private String key;
        private String value;
        private String tagName;

        public TagName(String tagName, String attrKey, String attrValue){
            this.key = attrKey;
            this.value = attrValue;
            this.tagName = tagName;
        }

        @Override
        public boolean matches(Element root, Element element) {
            List<com.boxparser.html.nodes.Attribute> values = element.attributes().asList();

            if (!StringUtil.isBlank(key)
                && !StringUtil.isBlank(value)) {
                for (com.boxparser.html.nodes.Attribute attribute : values) {
                    if (tagName.equals(element.tagName().trim().toLowerCase())
                        && key.equals(attribute.getKey().toLowerCase()) && value.equals(attribute.getValue())) return true;
                }
            } else if (!StringUtil.isBlank(key)) {
                for (com.boxparser.html.nodes.Attribute attribute : values) {
                    if (tagName.equals(element.tagName().trim().toLowerCase())
                        && key.equals(attribute.getKey().toLowerCase())) return true;
                }
            } else {
                if (tagName.equals(element.tagName().trim().toLowerCase())) return true;
            }

            return false;
        }

        @Override
        public String toString() {
            return String.format("[^%s]", key);
        }

    }

    public static final class AttributeStarting extends Evaluator {

        private String keyPrefix;

        public AttributeStarting(String keyPrefix){
            this.keyPrefix = keyPrefix;
        }

        @Override
        public boolean matches(Element root, Element element) {
            List<com.boxparser.html.nodes.Attribute> values = element.attributes().asList();
            for (com.boxparser.html.nodes.Attribute attribute : values) {
                if (attribute.getKey().startsWith(keyPrefix)) return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("[^%s]", keyPrefix);
        }

    }

    public static final class Class extends Evaluator {

        private String className;

        public Class(String className){
            this.className = className;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return (element.hasClass(className));
        }

        @Override
        public String toString() {
            return String.format(".%s", className);
        }

    }

}
