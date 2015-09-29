package org.feldspaten.hyperion.html;

import java.util.LinkedList;
import java.util.List;

/**
 * HTML Ordered list
 *
 */
public class OrderedList extends Html {

	/** List items */
	private List<Html> listItems = new LinkedList<>();

	@Override
	protected String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<ol>");

		for (final Html html : listItems)
			buffer.append("<li>" + html.generate() + "</li>");

		return buffer.toString();
	}

	@Override
	protected String generateFooter() {
		return "</ol>";
	}

	/**
	 * Adds a new list item
	 * 
	 * @param item
	 *            Text of the list item
	 */
	public void addListItem(final String item) {
		addListItem(new Plain(item));
	}

	/**
	 * Adds a new HTML component als list item
	 * 
	 * @param item
	 *            to be added
	 */
	public void addListItem(final Html item) {
		this.listItems.add(item);
	}

}
