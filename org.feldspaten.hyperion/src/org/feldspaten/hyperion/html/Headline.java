package org.feldspaten.hyperion.html;

public class Headline extends Html {

	private String title = "";
	private int level = 1;

	public Headline(String title) {
		super();
		this.title = title;
	}

	public Headline(String title, int level) {
		super();
		this.title = title;
		this.level = level;
	}

	@Override
	String generateHeader() {
		return "<h" + level + ">" + title + "</h" + level + ">";
	}

}
