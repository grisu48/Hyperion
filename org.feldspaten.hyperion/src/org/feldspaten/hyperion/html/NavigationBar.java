package org.feldspaten.hyperion.html;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NavigationBar extends Html implements
		Collection<NavigationBar.NavbarElement> {

	/** Element of this navigation bar */
	public class NavbarElement {
		/** Actual hyperlink for this element */
		private String link;
		/** Display text */
		private String text;
		/** Hyperlink target or null, if not defined */
		private String target;
		/** If visiable or not */
		private boolean visible;

		protected NavbarElement(String link, String text) {
			super();
			this.link = link;
			this.text = text;
			this.target = null;
			this.visible = true;
		}

		public String getLink() {
			return link;
		}

		public void setLink(String link) {
			this.link = link;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

	}

	/** List of all elements, this navigation bar holds */
	private final List<NavbarElement> elements = new LinkedList<>();

	/** Spaces between the elements */
	private int spaces = 1;

	public void setSpaces(int count) {
		this.spaces = count;
	}

	public int getSpaces() {
		return this.spaces;
	}

	public NavbarElement add(String link) {
		return this.add(link, link);
	}

	public NavbarElement add(final String text, final String link) {
		final NavbarElement element = new NavbarElement(link, text);
		add(element);
		return element;
	}

	public NavbarElement add(final String text, final String link,
			final String target) {
		final NavbarElement element = new NavbarElement(link, text);
		element.setTarget(target);
		add(element);
		return element;
	}

	@Override
	String generateHeader() {
		if (isEmpty())
			return "";

		// Separation spaces
		String spaces = "";
		for (int i = 0; i < this.spaces; i++)
			spaces += " ";

		final StringBuffer result = new StringBuffer();
		result.append("<p>");
		final Href hyperlink = new Href(""); // Object to create hyperlink HTML
		boolean first = true;
		for (final NavbarElement elem : elements) {
			if (!elem.isVisible())
				continue;
			if (first)
				first = false;
			else
				result.append(spaces);

			hyperlink.setLink(elem.getLink());
			hyperlink.setText("[" + elem.getText() + "]");
			if (elem.getTarget() != null)
				hyperlink.setTarget(elem.getTarget());
			result.append(hyperlink.generate());
		}

		result.append("</p>");
		return result.toString();
	}

	@Override
	public boolean add(NavbarElement element) {
		return this.elements.add(element);
	}

	@Override
	public boolean addAll(Collection<? extends NavbarElement> elements) {
		return this.elements.addAll(elements);
	}

	@Override
	public void clear() {
		this.elements.clear();
	}

	@Override
	public boolean contains(Object obj) {
		return this.elements.contains(obj);
	}

	@Override
	public boolean containsAll(Collection<?> objs) {
		return this.elements.containsAll(objs);
	}

	@Override
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	@Override
	public Iterator<NavbarElement> iterator() {
		return this.elements.iterator();
	}

	@Override
	public boolean remove(Object obj) {
		return this.elements.remove(obj);
	}

	@Override
	public boolean removeAll(Collection<?> objs) {
		return this.elements.removeAll(objs);
	}

	@Override
	public boolean retainAll(Collection<?> objs) {
		return this.elements.retainAll(objs);
	}

	@Override
	public int size() {
		return this.elements.size();
	}

	@Override
	public Object[] toArray() {
		return this.elements.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.elements.toArray(a);
	}

}
