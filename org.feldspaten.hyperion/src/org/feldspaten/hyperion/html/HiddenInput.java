package org.feldspaten.hyperion.html;

public class HiddenInput extends Html {

	private String name = "";
	private String value = "";

	public HiddenInput(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	String generateHeader() {
		return "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value
				+ "\">";
	}

	@Override
	String generateFooter() {
		return "";
	}

}
