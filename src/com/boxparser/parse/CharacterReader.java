package com.boxparser.parse;

import com.boxparser.html.util.Validate;

public class CharacterReader {

    public static final char EOF  = (char) -1;

    private final String     input;
    private final int        length;
    private int              pos  = 0;
    private int              mark = 0;

    public CharacterReader(String input){
        this(input, true);
    }

    public CharacterReader(String input, boolean normalise){
        Validate.notNull(input);
        if (normalise) {
            input = input.replaceAll("\r\n?", "\n"); // normalise carriage returns
                                                     // to newlines
        }

        this.input = input;
        this.length = input.length();
    }

    public int pos() {
        return pos;
    }

    public boolean isEmpty() {
        return pos >= length;
    }

    public char current() {
        return isEmpty() ? EOF : input.charAt(pos);
    }

    public char consume() {
        char val = isEmpty() ? EOF : input.charAt(pos);
        pos++;
        return val;
    }

    public void unconsume() {
        pos--;
    }

    public void advance() {
        pos++;
    }

    public void mark() {
        mark = pos;
    }

    public void rewindToMark() {
        pos = mark;
    }

    public String consumeAsString() {
        return input.substring(pos, pos++);
    }

    public String consumeTo(char c) {
        int offset = input.indexOf(c, pos);
        if (offset != -1) {
            String consumed = input.substring(pos, offset);
            pos += consumed.length();
            return consumed;
        } else {
            return consumeToEnd();
        }
    }

    public String consumeTo(String seq) {
        int offset = input.indexOf(seq, pos);
        if (offset != -1) {
            String consumed = input.substring(pos, offset);
            pos += consumed.length();
            return consumed;
        } else {
            return consumeToEnd();
        }
    }

    public String consumeToAny(char... seq) {
        int start = pos;

        OUTER: while (!isEmpty()) {
            char c = input.charAt(pos);
            for (char seek : seq) {
                if (seek == c) break OUTER;
            }
            pos++;
        }

        return pos > start ? input.substring(start, pos) : "";
    }

    public String consumeToEnd() {
        String data = input.substring(pos, input.length());
        pos = input.length();
        return data;
    }

    public String consumeLetterSequence() {
        int start = pos;
        while (!isEmpty()) {
            char c = input.charAt(pos);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) pos++;
            else break;
        }

        return input.substring(start, pos);
    }

    public String consumeHexSequence() {
        int start = pos;
        while (!isEmpty()) {
            char c = input.charAt(pos);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) pos++;
            else break;
        }
        return input.substring(start, pos);
    }

    public String consumeDigitSequence() {
        int start = pos;
        while (!isEmpty()) {
            char c = input.charAt(pos);
            if (c >= '0' && c <= '9') pos++;
            else break;
        }
        return input.substring(start, pos);
    }

    public boolean matches(char c) {
        return !isEmpty() && input.charAt(pos) == c;

    }

    public boolean matches(String seq) {
        return input.startsWith(seq, pos);
    }

    public boolean matchesIgnoreCase(String seq) {
        return input.regionMatches(true, pos, seq, 0, seq.length());
    }

    public boolean matchesAny(char... seq) {
        if (isEmpty()) return false;

        char c = input.charAt(pos);
        for (char seek : seq) {
            if (seek == c) return true;
        }
        return false;
    }

    public boolean matchesLetter() {
        if (isEmpty()) return false;
        char c = input.charAt(pos);
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    public boolean matchesDigit() {
        if (isEmpty()) return false;
        char c = input.charAt(pos);
        return (c >= '0' && c <= '9');
    }

    public boolean matchConsume(String seq) {
        if (matches(seq)) {
            pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    public boolean matchConsumeIgnoreCase(String seq) {
        if (matchesIgnoreCase(seq)) {
            pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    public boolean containsIgnoreCase(String seq) {
        // used to check presence of </title>, </style>. only finds consistent
        // case.
        String loScan = seq.toLowerCase();
        String hiScan = seq.toUpperCase();
        return (input.indexOf(loScan, pos) > -1) || (input.indexOf(hiScan, pos) > -1);
    }

    @Override
    public String toString() {
        return input.substring(pos);
    }

    /**
     * zhaoyang.add
     */
    @Deprecated
    public void setPos(int pos) {
        this.pos = pos;
    }

    /**
     * zhaoyang.add
     */
    public boolean matchesWhitespace() {
        if (isEmpty()) return false;
        char c = input.charAt(pos);
        return Character.isWhitespace(c);
    }
    
    /**
     * zhaoyang.add
     */    
    public String consumeWhitespaceSequence() {
        int start = pos;
        while (!isEmpty()) {
            char c = input.charAt(pos);
            if (Character.isWhitespace(c)) pos++;
            else break;
        }
        return input.substring(start, pos);
    }    
}
