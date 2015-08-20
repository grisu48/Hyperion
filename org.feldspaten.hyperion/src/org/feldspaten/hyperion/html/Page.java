package org.feldspaten.hyperion.html;

import java.util.LinkedList;
import java.util.List;

public class Page extends Html {

	private String title = "";
	/** If > 0, autorefresh is enabled with the given interval */
	private int autoRefreshDelay = 0;

	/** Meta fields */
	private List<String> metas = new LinkedList<>();

	@Override
	String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<!DOCTYPE html>");
		buffer.append("\n");
		buffer.append("<html><head>");
		buffer.append("\n");
		if (!title.isEmpty()) {
			buffer.append("<title>");
			buffer.append(title);
			buffer.append("</title>");
		}
		buffer.append("\n");
		if (isAutoRefreshEnabled()) {
			buffer.append("<meta http-equiv=\"refresh\" content=\""
					+ autoRefreshDelay + "\">");
		}
		// Add additional metas
		for (final String meta : metas) {
			buffer.append("<meta " + meta + " />");
		}

		buffer.append("</head>");
		buffer.append("<body>");

		return buffer.toString();
	}

	@Override
	String generateFooter() {
		return "</body>";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getAutoRefreshDelay() {
		return autoRefreshDelay;
	}

	public void setAutoRefreshDelay(int autoRefreshDelay) {
		this.autoRefreshDelay = autoRefreshDelay;
	}

	public boolean isAutoRefreshEnabled() {
		return this.autoRefreshDelay > 0;
	}

	/**
	 * Add a raw meta field The meta tag is added automatically, so you don't
	 * need it here
	 * 
	 * @param meta
	 *            to be added
	 */
	public void addMeta(final String meta) {
		if (meta == null || meta.trim().isEmpty())
			return;
		this.metas.add(meta);
	}

}
