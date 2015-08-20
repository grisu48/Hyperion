package org.feldspaten.hyperion.html;

public class ResetButton extends Html {
	private String value = "Reset";

	public ResetButton(String value) {
		super();
		this.value = value;
	}

	public ResetButton() {
		super();
	}

	@Override
	String generateHeader() {
		return "<input type=\"reset\" value=\"" + value + "\">";
	}
}
