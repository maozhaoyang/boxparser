package com.boxparser.parse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import com.boxparser.html.nodes.Document;
import com.boxparser.html.nodes.Entities;
import com.boxparser.html.nodes.Node;
import com.boxparser.html.util.Validate;

public class HtmlParser {

    public static List<Node> parseNodes(String input) {
        Document doc = parse(input);
        return doc.childNodes();
    }

    public static Document parse(String input) {
        TreeBuilder builder = new DataSourceTreeBuilder();
        List<ParseError> errors = new ArrayList<ParseError>();
        Document doc = builder.parse(input, errors);
        return doc;
    }

    public static String parseHTML(String input) {
        Document doc = parse(input);
        return doc.toString();
    }

    public static class OutputSettings implements Cloneable {

        private Entities.EscapeMode   escapeMode     = Entities.EscapeMode.base;
        private Charset               charset        = Charset.forName("GBK");
        private CharsetEncoder        charsetEncoder = charset.newEncoder();
        private boolean               prettyPrint    = true;
        private int                   indentAmount   = 1;                        ;

        private static OutputSettings defaultSetting = null;

        public static OutputSettings defaultOutputSettings() {
            if (defaultSetting == null) {
                defaultSetting = new OutputSettings();
            }
            return defaultSetting;
        }

        public OutputSettings(){
        }

        /**
         * Get the document's current HTML escape mode: <code>base</code>, which provides a limited set of named HTML
         * entities and escapes other characters as numbered entities for maximum compatibility; or
         * <code>extended</code>, which uses the complete set of HTML named entities.
         * <p>
         * The default escape mode is <code>base</code>.
         * 
         * @return the document's current escape mode
         */
        public Entities.EscapeMode escapeMode() {
            return escapeMode;
        }

        /**
         * Set the document's escape mode
         * 
         * @param escapeMode the new escape mode to use
         * @return the document's output settings, for chaining
         */
        public OutputSettings escapeMode(Entities.EscapeMode escapeMode) {
            this.escapeMode = escapeMode;
            return this;
        }

        /**
         * Get the document's current output charset, which is used to control which characters are escaped when
         * generating HTML (via the <code>html()</code> methods), and which are kept intact.
         * <p>
         * Where possible (when parsing from a URL or File), the document's output charset is automatically set to the
         * input charset. Otherwise, it defaults to UTF-8.
         * 
         * @return the document's current charset.
         */
        public Charset charset() {
            return charset;
        }

        /**
         * Update the document's output charset.
         * 
         * @param charset the new charset to use.
         * @return the document's output settings, for chaining
         */
        public OutputSettings charset(Charset charset) {
            // todo: this should probably update the doc's meta charset
            this.charset = charset;
            charsetEncoder = charset.newEncoder();
            return this;
        }

        /**
         * Update the document's output charset.
         * 
         * @param charset the new charset (by name) to use.
         * @return the document's output settings, for chaining
         */
        public OutputSettings charset(String charset) {
            charset(Charset.forName(charset));
            return this;
        }

        public CharsetEncoder encoder() {
            return charsetEncoder;
        }

        /**
         * Get if pretty printing is enabled. Default is true. If disabled, the HTML output methods will not re-format
         * the output, and the output will generally look like the input.
         * 
         * @return if pretty printing is enabled.
         */
        public boolean prettyPrint() {
            return prettyPrint;
        }

        /**
         * Enable or disable pretty printing.
         * 
         * @param pretty new pretty print setting
         * @return this, for chaining
         */
        public OutputSettings prettyPrint(boolean pretty) {
            prettyPrint = pretty;
            return this;
        }

        /**
         * Get the current tag indent amount, used when pretty printing.
         * 
         * @return the current indent amount
         */
        public int indentAmount() {
            return indentAmount;
        }

        /**
         * Set the indent amount for pretty printing
         * 
         * @param indentAmount number of spaces to use for indenting each level. Must be >= 0.
         * @return this, for chaining
         */
        public OutputSettings indentAmount(int indentAmount) {
            Validate.isTrue(indentAmount >= 0);
            this.indentAmount = indentAmount;
            return this;
        }

        @Override
        public OutputSettings clone() {
            OutputSettings clone;
            try {
                clone = (OutputSettings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            clone.charset(charset.name()); // new charset and charset encoder
            clone.escapeMode = Entities.EscapeMode.valueOf(escapeMode.name());
            // indentAmount, prettyPrint are primitives so object.clone() will handle
            return clone;
        }
    }
}
