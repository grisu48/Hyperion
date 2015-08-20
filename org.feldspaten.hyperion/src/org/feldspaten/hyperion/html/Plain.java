package org.feldspaten.hyperion.html;

/**
 * Plain HTML
 *
 */
public class Plain extends Html {

	private final String html;

	public Plain(String html) {
		super();
		this.html = html;
	}

	public Plain() {
		this.html = "";
	}

	@Override
	String generateFooter() {
		return "";
	}

	@Override
	public String generate() {
		return html;
	}

	@Override
	String generateHeader() {
		return html;
	}
}
