package com.boxparser.html.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.boxparser.html.util.Validate;
import com.boxparser.parse.HtmlParser.OutputSettings;

public class Attributes implements Iterable<Attribute>, Cloneable, Serializable {
    private static final long serialVersionUID = 1L; 
	private LinkedHashMap<String, Attribute> attributes = null;

	public String get(String key) {
		Validate.notEmpty(key);

		if (attributes == null)
			return "";

		Attribute attr = attributes.get(key.toLowerCase());
		return attr != null ? attr.getValue() : "";
	}

	public void put(String key, String value) {
		Attribute attr = new Attribute(key, value, '"');
		put(attr);
	}

	public void put(Attribute attribute) {
		Validate.notNull(attribute);
		if (attributes == null)
			attributes = new LinkedHashMap<String, Attribute>(2);
		attributes.put(attribute.getKey(), attribute);
	}

	public void addAll(Attributes incoming) {
		if (incoming.size() == 0)
			return;
		if (attributes == null)
			attributes = new LinkedHashMap<String, Attribute>(incoming.size());
		attributes.putAll(incoming.attributes);
	}

	public Iterator<Attribute> iterator() {
		return asList().iterator();
	}

	public List<Attribute> asList() {
		if (attributes == null)
			return Collections.emptyList();

		List<Attribute> list = new ArrayList<Attribute>(attributes.size());
		for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
			list.add(entry.getValue());
		}
		return Collections.unmodifiableList(list);
	}

	public void remove(String key) {
		Validate.notEmpty(key);
		if (attributes == null)
			return;
		attributes.remove(key.toLowerCase());
	}

	public boolean hasKey(String key) {
		return attributes != null && attributes.containsKey(key.toLowerCase());
	}

	public int size() {
		if (attributes == null)
			return 0;
		return attributes.size();
	}

	@Override
	public Attributes clone() {
		if (attributes == null)
			return new Attributes();

		Attributes clone;
		try {
			clone = (Attributes) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		clone.attributes = new LinkedHashMap<String, Attribute>(attributes
				.size());
		for (Attribute attribute : this)
			clone.attributes.put(attribute.getKey(), attribute.clone());
		return clone;
	}

	/**
	 * to html
	 */
	void html(StringBuilder accum, OutputSettings out) {
		if (attributes == null)
			return;

		for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
			Attribute attribute = entry.getValue();
			accum.append(" ");
			attribute.html(accum, out);
		}
	}

	public String toString() {
		return html();
	}

	public String html() {
		StringBuilder accum = new StringBuilder();
		html(accum, OutputSettings.defaultOutputSettings()); // output settings a
															// bit funky, but
															// this html()
															// seldom used
		return accum.toString();
	}
}
