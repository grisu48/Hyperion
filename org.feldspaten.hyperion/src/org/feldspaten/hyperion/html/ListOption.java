package org.feldspaten.hyperion.html;

public class ListOption {

	private String text = "";
	private String value = "";

	public ListOption(String text, String value) {
		super();
		this.text = text;
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	String generateHtml() {
		return generateHtml(false);
	}

	String generateHtml(final boolean selected) {
		return "<option value=\"" + value + "\""
				+ (selected ? " selected" : "") + ">" + text + "</option>";
	}
}
