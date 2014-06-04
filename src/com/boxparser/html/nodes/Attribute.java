package com.boxparser.html.nodes;

import java.io.Serializable;
import java.util.Map;

import com.boxparser.html.util.Validate;
import com.boxparser.parse.HtmlParser.OutputSettings;

public class Attribute implements Map.Entry<String, String>, Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
    private String key;
	private String value;
	private char beforeChar;

    public Attribute(String key, String value) {
        this(key, value, (char)0);
    }

	public Attribute(String key, String value, char beforeChar) {
		Validate.notEmpty(key);
		Validate.notNull(value);
		this.key = key.trim();
		this.value = value;
		this.beforeChar = beforeChar;
	}
	
	public String getKey() {
		// TODO Auto-generated method stub
		return key;
	}

	public void setKey(String key) {
		Validate.notEmpty(key);
		this.key = key.trim().toLowerCase();
	}

	public String getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	public String setValue(String value) {
		Validate.notNull(value);
		String old = this.value;
		this.value = value;
		return old;
	}

	@Override
	public Attribute clone() {
		try {
			return (Attribute) super.clone(); // only fields are immutable
			// strings key and value, so no
			// more deep copy reqd
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	protected void html(StringBuilder accum, OutputSettings out) {
        boolean appendChar = beforeChar != 0;
        accum.append(key).append("=");
        if (appendChar) accum.append(beforeChar);
        accum.append(value);
        if (appendChar) accum.append(beforeChar);
        
//		accum.append(key).append("=\"").append(value)
//				.append("\"");
	}
}
