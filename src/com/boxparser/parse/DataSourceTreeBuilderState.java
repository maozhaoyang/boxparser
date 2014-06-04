package com.boxparser.parse;

import java.util.LinkedList;

import com.boxparser.html.nodes.Element;
import com.boxparser.html.util.StringUtil;

public enum DataSourceTreeBuilderState {

    all {

        @Override
        public boolean process(Token t, DataSourceTreeBuilder tb) {
            switch (t.type) {
                case StartTag: {
                    Token.StartTag startTag = t.asStartTag();
                    String name = startTag.name();
                    if (name.equals("style")) {
                        handleRawtext(startTag, tb);
                    } else if (name.equals("script")) {
                        // skips some script rules as won't execute them
                        tb.insert(startTag);
                        tb.tokeniser.transition(DataSourceTokeniserState.ScriptData);
                        tb.markInsertionMode();
                        tb.transition(Text);
                    } else if (name.equals("li")) {
                        handNoEmbeddedTag(tb, startTag, name);
                    } else if (StringUtil.in(name, "area", "br", "embed", "img", "keygen", "wbr", "input", "param",
                                             "source", "track", "hr")) {
                        tb.insertEmpty(startTag);
                    } else if (StringUtil.in(name, "meta", "base", "basefont", "bgsound", "command", "link")) {
                        tb.insertEmpty(startTag);
                    } else if (StringUtil.in(name, "dd", "dt")) {
                        handNoEmbeddedTag(tb, startTag, "dd", "dt");
                    } else if (StringUtil.in(name, "tr")) {
                        backwordTagInScope(tb, startTag, "tbody","table");
                    } else if (StringUtil.in(name, "h1", "h2", "h3", "h4", "h5", "h6")) {
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        if (StringUtil.in(tb.currentElement().nodeName(), "h1", "h2", "h3", "h4", "h5", "h6")) {
                            tb.error(this);
                            tb.pop();
                        }
                        tb.insert(startTag);
                    } else if (name.equals("textarea")) {
                        tb.insert(startTag);
                        // todo: If the next token is a U+000A LINE FEED (LF) character token, then ignore that token
                        // and move on to the next one. (Newlines at the start of textarea elements are ignored as an
                        // authoring convenience.)
                        tb.tokeniser.transition(DataSourceTokeniserState.Rcdata);
                        tb.markInsertionMode();
                        tb.transition(Text);
                    } else if (name.equals("iframe")) {
                        handleRawtext(startTag, tb);
                    } else if (name.equals("image")) {
                        // we're not supposed to ask.
                        startTag.name("img");
                        return tb.process(startTag);
                    } else {
                        tb.insert(startTag);
                    }
                    break;
                }

                case EndTag: {
                    Token.EndTag endTag = t.asEndTag();
                    String name = endTag.name();
                    if (!tb.inScope(name)) {
                        // nothing to close
                        tb.error(this);
                        return false;
                    } else {
                        tb.generateImpliedEndTags();
                        if (!tb.currentElement().nodeName().equals(name)) tb.error(this);
                        tb.popStackToClose(name);
                    }
                    break;
                }

                case Character:
                    tb.insert(t.asCharacter());
                    break;

                case Comment:
                    tb.insert(t.asComment());
                    break;

                case Doctype:
                    tb.insert(t.asDoctype());

                case EOF:
                    break;

                default:

            }
            return false;
        }

    },

    Text {

        // in script, style etc. normally treated as data tags
        boolean process(Token t, DataSourceTreeBuilder tb) {
            if (t.isCharacter()) {
                tb.insert(t.asCharacter());
            } else if (t.isEOF()) {
                tb.error(this);
                // if current node is script: already started
                tb.pop();
                tb.transition(tb.originalState());
                return tb.process(t);
            } else if (t.isEndTag()) {
                // if: An end tag whose tag name is "script" -- scripting
                // nesting level, if evaluating scripts
                tb.pop();
                tb.transition(tb.originalState());
            }
            return true;
        }
    };

    abstract boolean process(Token t, DataSourceTreeBuilder tb);

    private static void handleRawtext(Token.StartTag startTag, DataSourceTreeBuilder tb) {
        tb.insert(startTag);
        tb.tokeniser.transition(DataSourceTokeniserState.Rawtext);
        tb.markInsertionMode();
        tb.transition(Text);
    }

    /**
     * @param tb
     * @param startTag
     */
    private static void handNoEmbeddedTag(DataSourceTreeBuilder tb, Token.StartTag startTag, String... inTagNames) {
        // tb.framesetOk(false);
        LinkedList<Element> stack = tb.getStack();
        for (int i = stack.size() - 1; i > 0; i--) {
            Element el = stack.get(i);
            if (StringUtil.in(el.nodeName(), inTagNames)) {
                tb.process(new Token.EndTag(el.tagName()));
                break;
            }
            if (tb.isSpecial(el) && !StringUtil.in(el.nodeName(), "address", "div", "p")) break;
        }
        if (tb.inButtonScope("p")) {
            tb.process(new Token.EndTag("p"));
        }
        tb.insert(startTag);
    }

    private static void backwordTagInScope(DataSourceTreeBuilder tb, Token.StartTag startTag, String... excludeTags) {
        LinkedList<Element> stack = tb.getStack();
        String nodeName = startTag.name();
        for (int i = stack.size() - 1; i > 0; i--) {
            Element el = stack.get(i);
            if (el.nodeName().equals(nodeName)) {
                tb.process(new Token.EndTag(el.tagName()));
                break;
            }
            // 如果在排除标签外
            if (StringUtil.in(el.nodeName(), excludeTags)) {
                break;
            }
        }
        tb.insert(startTag);
    }
}
