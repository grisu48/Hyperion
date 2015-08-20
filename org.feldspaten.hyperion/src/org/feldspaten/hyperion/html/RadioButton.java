package org.feldspaten.hyperion.html;

public class RadioButton extends Html {

	private String name = "";
	private String value = "";
	private boolean checked = false;

	public RadioButton(String name, String value, boolean checked) {
		super();
		this.name = name;
		this.value = value;
		this.checked = checked;
	}

	public RadioButton(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public RadioButton(String name) {
		super();
		this.name = name;
		this.value = "true";
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

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<input type=\"radio\"");
		if (name.length() > 0) {
			buffer.append(" name=\"");
			buffer.append(name);
			buffer.append("\"");
		}
		if (value.length() > 0) {
			buffer.append(" value=\"");
			buffer.append(value);
			buffer.append("\"");
		}
		if (checked)
			buffer.append("checked=\"checked\"");
		buffer.append('>');
		return buffer.toString();
	}

}
