package org.feldspaten.hyperion.html;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * HTML element - Superclass for all components
 *
 */
public abstract class Html {

	/** Subcomponets of the component */
	protected final List<Html> subcomponents = new LinkedList<>();
	/** If this component is enabled */
	protected boolean enabled = true;

	/** ID of the component, if set */
	protected String id = null;

	/**
	 * Generate the HTML contents for the element
	 * 
	 * @return HTML code of the element
	 */
	public String generate() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(generateHeader());
		for (final Html html : subcomponents) {
			if (html.isEnabled())
				buffer.append(html.generate());
		}
		buffer.append(generateFooter());
		return buffer.toString();
	}

	/**
	 * Generate header of the component. The header is usually the HTML part
	 * before the subcomponents
	 * 
	 * @return HTML of the header
	 */
	protected abstract String generateHeader();

	/**
	 * Generate footer for the component The header is usually the HTML part
	 * after the subcomponents
	 * 
	 * @return HTML of the footer
	 */
	protected String generateFooter() {
		return "";
	}

	/**
	 * @return ID of the component, if assigned. Otherwise null
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set ID of the component
	 * 
	 * @param id
	 *            ID of the HTML component. Null to delete
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Add a subcomponent
	 * 
	 * @param component
	 *            to be added
	 */
	public void addComponent(final Html component) {
		if (component == null)
			return;
		synchronized (subcomponents) {
			subcomponents.add(component);
		}
	}

	/**
	 * Creates and adds a table
	 * 
	 * @return the created table
	 */
	public Table addTable() {
		final Table table = new Table();
		addComponent(table);
		return table;
	}

	/**
	 * Create and add a form
	 * 
	 * @param action
	 *            Action for the created form
	 * @return the created form
	 */
	public Form addForm(final String action) {
		final Form form = new Form(action);
		addComponent(form);
		return form;
	}

	/**
	 * Create and add a form
	 * 
	 * @param action
	 *            Action for the created form
	 * @param method
	 *            Method for the created form
	 * @return the created form
	 */
	public Form addForm(final String action, final String method) {
		final Form form = new Form(action, method);
		addComponent(form);
		return form;
	}

	/**
	 * Removes the given component
	 * 
	 * @param component
	 *            to be removed
	 * @return true if found and removed, false if not found
	 */
	public boolean removeComponent(final Html component) {
		if (component == null)
			return false;
		synchronized (subcomponents) {
			boolean found = false;
			while (subcomponents.contains(component)) {
				subcomponents.remove(component);
				found = true;
			}
			return found;
		}
	}

	/**
	 * Replaces the given component with a replacement
	 * 
	 * @param original
	 *            to be replaced
	 * @param replacement
	 *            with this instance
	 * @return true if the component is found an replaced otherwise false
	 */
	public boolean replaceComponent(final Html original, final Html replacement) {
		if (original == null)
			return false;
		if (replacement == null)
			return removeComponent(original);
		if (original.equals(replacement))
			return false;

		synchronized (subcomponents) {
			if (subcomponents.isEmpty())
				return false;
			boolean found = false;
			while (true) {
				final int index = subcomponents.indexOf(original);
				if (index < 0)
					break;
				found = true;
				subcomponents.set(index, replacement);
			}
			return found;
		}
	}

	@Override
	public String toString() {
		return generate();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		try {
			Html html = (Html) obj;
			return html.generate().equals(this.generate());
		} catch (ClassCastException e) {
			return false;
		}
	}

	public void print(final PrintWriter writer) {
		writer.print(generate());
	}

	public void println(final PrintWriter writer) {
		writer.println(generate());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Headline addHeadline(final String headline) {
		return addHeadline(headline, 1);
	}

	public Headline addHeadline(final String title, final int level) {
		final Headline headline = new Headline(title, level);
		addComponent(headline);
		return headline;
	}

	public Paragraph addParagraph(final String text) {
		final Paragraph paragraph = new Paragraph();
		paragraph.addComponent(new Plain(text));
		addComponent(paragraph);
		return paragraph;
	}

	public HorizontalLine addHorizontalLine() {
		final HorizontalLine line = new HorizontalLine();
		addComponent(line);
		return line;
	}

	/**
	 * Adds the given HTML as {@link Plain} component
	 * 
	 * @param html
	 *            to be added
	 */
	public Plain addPlain(final String html) {
		final Plain plain = new Plain(html);
		addComponent(plain);
		return plain;
	}
}
