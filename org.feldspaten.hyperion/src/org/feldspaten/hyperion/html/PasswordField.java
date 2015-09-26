package org.feldspaten.hyperion.html;

public class PasswordField extends Html {

	private String name = "";
	private String value = "";

	public PasswordField(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public PasswordField(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	protected String generateHeader() {
		final StringBuffer buffer = new StringBuffer();

		buffer.append("<input type=\"password\"");
		if (!name.isEmpty()) {
			buffer.append(" name=\"");
			buffer.append(name);
			buffer.append("\"");
		}
		if (value != null && !value.isEmpty()) {
			buffer.append(" value=\"");
			buffer.append(value);
			buffer.append("\"");
		}
		buffer.append('>');

		return buffer.toString();
	}

	@Override
	public String generate() {
		return generateHeader();
	}

	@Override
	protected String generateFooter() {
		return "";
	}

}
