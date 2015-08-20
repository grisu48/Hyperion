package org.feldspaten.hyperion.html;

/**
 * HTML hyperlink
 *
 */
public class Href extends Html {

	/**
	 * Target of the link:
	 * <ul>
	 * <li>_blank</li>
	 * <li>_self</li>
	 * <li>_parent</li>
	 * <li>_top</li>
	 * </ul>
	 */
	private String target = "";
	/** Link the hyperlinks points to */
	private String link = "";
	/** Displayed html */
	private String display = "";

	public Href(String link, String text, String target) {
		super();
		this.link = link;
		this.target = target;
		this.display = text;
	}

	public Href(String link, String text) {
		super();
		this.link = link;
		this.display = text;
	}

	public Href(String link) {
		super();
		this.link = link;
		this.display = link;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public void setText(String text) {
		this.display = text;
	}

	@Override
	String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<a href=\"");
		buffer.append(link);
		buffer.append("\"");
		if (target != null && !target.isEmpty()) {
			buffer.append(" target=\"");
			buffer.append(target);
			buffer.append("\"");
		}
		buffer.append('>');
		if (super.subcomponents.isEmpty())
			buffer.append(display);
		return buffer.toString();
	}

	@Override
	String generateFooter() {
		return "</a>";
	}
}
