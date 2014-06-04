package com.boxparser.parse;

import java.util.List;

import com.boxparser.html.util.Validate;

class DataSourceTokeniser {
	static final char replacementChar = '\uFFFD'; // replaces null character

	private CharacterReader reader; // html input
	private List errors; // errors found while tokenising

	private DataSourceTokeniserState state = DataSourceTokeniserState.Data; // current tokenisation
	// state
	private Token emitPending; // the token we are about to emit on next read
	private boolean isEmitPending = false;
	private StringBuilder charBuffer = new StringBuilder(); // buffers
	// characters to
	// output as one
	// token
	StringBuilder dataBuffer; // buffers data looking for </script>

	Token.Tag tagPending; // tag we are building up
	Token.Doctype doctypePending; // doctype building up
	Token.Comment commentPending; // comment building up
	private Token.StartTag lastStartTag; // the last start tag emitted, to test
	// appropriate end tag
	private boolean selfClosingFlagAcknowledged = true;

	DataSourceTokeniser(CharacterReader reader, List errors) {
		this.reader = reader;
		this.errors = errors;
	}

	Token read() {
		if (!selfClosingFlagAcknowledged) {
			error("Self closing flag not acknowledged");
			selfClosingFlagAcknowledged = true;
		}

		while (!isEmitPending)
			state.read(this, reader);

		// if emit is pending, a non-character token was found: return any chars
		// in buffer, and leave token for next read:
		if (charBuffer.length() > 0) {
			String str = charBuffer.toString();
			charBuffer.delete(0, charBuffer.length());
			return new Token.Character(str);
		} else {
			isEmitPending = false;
			return emitPending;
		}
	}

	void emit(Token token) {
		Validate.isFalse(isEmitPending, "There is an unread token pending!");

		emitPending = token;
		isEmitPending = true;

		if (token.type == Token.TokenType.StartTag) {
			Token.StartTag startTag = (Token.StartTag) token;
			lastStartTag = startTag;
			if (startTag.selfClosing)
				selfClosingFlagAcknowledged = false;
		} else if (token.type == Token.TokenType.EndTag) {
			Token.EndTag endTag = (Token.EndTag) token;
			if (endTag.attributes.size() > 0)
				error("Attributes incorrectly present on end tag");
		}
	}

	void emit(String str) {
		// buffer strings up until last string token found, to emit only one
		// token for a run of character refs etc.
		// does not set isEmitPending; read checks that
		charBuffer.append(str);
	}

	void emit(char c) {
		charBuffer.append(c);
	}

	DataSourceTokeniserState getState() {
		return state;
	}

	void transition(DataSourceTokeniserState state) {
		this.state = state;
	}

	void advanceTransition(DataSourceTokeniserState state) {
		reader.advance();
		this.state = state;
	}

	void acknowledgeSelfClosingFlag() {
		selfClosingFlagAcknowledged = true;
	}

	Token.Tag createTagPending(boolean start) {
		tagPending = start ? new Token.StartTag() : new Token.EndTag();
		return tagPending;
	}

	void emitTagPending() {
		tagPending.finaliseTag();
		emit(tagPending);
	}

	void createCommentPending() {
		commentPending = new Token.Comment();
	}

	void emitCommentPending() {
		emit(commentPending);
	}

	void createDoctypePending() {
		doctypePending = new Token.Doctype();
	}

	void emitDoctypePending() {
		emit(doctypePending);
	}

	void createTempBuffer() {
		dataBuffer = new StringBuilder();
	}

	boolean isAppropriateEndTagToken() {
		return tagPending.tagName.equals(lastStartTag.tagName);
	}

	String appropriateEndTagName() {
		return lastStartTag.tagName;
	}

	void error(DataSourceTokeniserState state) {
		errors.add(new ParseError(reader.pos(),
				"Unexpected character '%s' in input state [%s]", reader
						.current(), state));
	}

	void eofError(DataSourceTokeniserState state) {
		errors.add(new ParseError(reader.pos(),
				"Unexpectedly reached end of file (EOF) in input state [%s]",
				state));
	}

	private void characterReferenceError(String message) {
		errors.add(new ParseError(reader.pos(),
				"Invalid character reference: %s", message));
	}

	private void error(String errorMsg) {
		errors.add(new ParseError(reader.pos(), errorMsg));
	}

	boolean currentNodeInHtmlNS() {
		// todo: implememnt namespaces correctly
		return true;
		// Element currentNode = currentNode();
		// return currentNode != null && currentNode.namespace().equals("HTML");
	}
}
