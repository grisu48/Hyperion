package org.feldspaten.hyperion.html;

public class TextField extends Html {

	private String name = "";
	private String value = "";
	private boolean readonly = false;

	public TextField(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public TextField(String name) {
		super();
		this.name = name;
		this.value = "";
	}

	public TextField(String name, String value, boolean readonly) {
		this.name = name;
		this.value = value;
		this.readonly = readonly;
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

	public boolean isReadOnly() {
		return readonly;
	}

	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
	}

	@Override
	String generateHeader() {
		final StringBuffer buffer = new StringBuffer();

		buffer.append("<input type=\"text\"");
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
		if (readonly)
			buffer.append(" readonly");
		buffer.append('>');

		return buffer.toString();
	}

	@Override
	public String generate() {
		return generateHeader();
	}

	@Override
	String generateFooter() {
		return "";
	}

}
