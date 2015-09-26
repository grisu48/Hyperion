package org.feldspaten.hyperion.html;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DropdownList extends Html {

	private final List<ListOption> listOptions = new LinkedList<>();
	private String name = "";
	private int selected = 0;

	public DropdownList(String name) {
		super();
		this.name = name;
	}

	public DropdownList(String name,
			final Collection<? extends ListOption> options) {
		super();
		this.name = name;
		this.listOptions.addAll(options);
	}

	public ListOption addOption(final ListOption option) {
		if (option == null)
			return null;
		synchronized (listOptions) {
			listOptions.add(option);
		}
		return option;
	}

	public ListOption addOption(final String text, final String value) {
		return addOption(new ListOption(text, value));
	}

	public int getSelected() {
		return selected;
	}

	@Override
	protected String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<select name=\"");
		buffer.append(name);
		buffer.append("\">");
		synchronized (listOptions) {
			int i = 0;
			for (final ListOption option : listOptions)
				buffer.append(option.generateHtml(i++ == selected));

		}
		buffer.append("</select>");
		return buffer.toString();
	}

	public void setSelected(int i) {
		this.selected = i;
	}

}
