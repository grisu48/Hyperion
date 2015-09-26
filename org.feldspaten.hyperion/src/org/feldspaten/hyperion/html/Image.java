package org.feldspaten.hyperion.html;

/**
 * Embedded HTML picture
 * 
 * @author phoenix
 *
 */
public class Image extends Html {

	private String source;
	private String alternativeText = null;
	private int width = 0;
	private int height = 0;

	public Image(String source) {
		super();
		this.source = source;
	}

	public void setAlternativeText(final String text) {
		this.alternativeText = text;
	}

	public void setSize(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	protected String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<img src=\"" + source + "\"");
		if (alternativeText != null && !alternativeText.isEmpty())
			buffer.append(" alt=\"" + alternativeText + "\"");
		if (width > 0)
			buffer.append(" width=\"" + width + "\"");
		if (height > 0)
			buffer.append(" height=\"" + height + "\"");
		buffer.append(">");
		return buffer.toString();
	}

}
