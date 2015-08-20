package org.feldspaten.hyperion.html;

public class SubmitButton extends Html {
	private String value = "Submit";
	private String name = "";

	public SubmitButton(String value) {
		super();
		this.value = value;
	}

	public SubmitButton() {
		super();
	}

	public SubmitButton(String value, String name) {
		this.value = value;
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<input type=\"submit\" value=\"" + value + "\"");
		if (name.length() > 0) {
			buffer.append(" name=\"");
			buffer.append(name);
			buffer.append('"');
		}
		buffer.append('>');

		return buffer.toString();
	}
}
