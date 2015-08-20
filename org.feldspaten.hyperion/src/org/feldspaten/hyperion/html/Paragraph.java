package org.feldspaten.hyperion.html;

/**
 * HTML paragraph
 *
 */
public class Paragraph extends Html {

	@Override
	String generateFooter() {
		return "</p>";
	}

	@Override
	String generateHeader() {
		return "<p>";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		try {
			final Paragraph paragraph = (Paragraph) obj;

			if (this.subcomponents.size() != paragraph.subcomponents.size())
				return false;
			return this.subcomponents.equals(paragraph.subcomponents);
		} catch (ClassCastException e) {
			return false;
		}
	}
}
