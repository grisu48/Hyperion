package org.feldspaten.hyperion.html;

import java.util.LinkedList;
import java.util.List;

public class Table extends Html {

	/**
	 * Single table row
	 *
	 */
	public class TableRow {

		public List<Html> elements = new LinkedList<>();

		private TableRow() {

		}

		public int columns() {
			return elements.size();
		}

		public synchronized TableRow put(int index, Html element) {
			if (index < 0)
				throw new IndexOutOfBoundsException();

			while (index >= elements.size())
				elements.add(new Plain());
			elements.set(index, element);
			return this;
		}

		public synchronized TableRow put(int index, String html) {
			if (index < 0)
				throw new IndexOutOfBoundsException();

			while (index >= elements.size())
				elements.add(new Plain());
			elements.set(index, new Plain(html));
			return this;
		}

		public synchronized TableRow put(String html) {
			elements.add(new Plain(html));
			return this;
		}

		public synchronized TableRow put(Html element) {
			elements.add(element);
			return this;
		}

		public synchronized Html get(int index) {
			try {
				return elements.get(index);
			} catch (IndexOutOfBoundsException e) {
				return new Plain();
			}
		}

		@Override
		public int hashCode() {
			return rows.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			try {
				TableRow row = (TableRow) obj;
				if (row.columns() != this.columns())
					return false;
				final int len = this.columns();
				for (int i = 0; i < len; i++) {
					if (!row.elements.get(i).equals(this.elements.get(i)))
						return false;
				}
				return false;
			} catch (ClassCastException e) {
				return false;
			}
		}

		public String generate() {
			final StringBuffer buffer = new StringBuffer();
			generate(buffer);
			return buffer.toString();
		}

		void generate(final StringBuffer buffer) {
			buffer.append("<tr>");
			for (final Html html : elements) {
				buffer.append("<td>");
				buffer.append(html.generate());
				buffer.append("</td>");
			}
			buffer.append("</tr>");
		}

		public TableRow put(final float value) {
			return this.put(Float.toString(value));
		}

		public TableRow put(final double value) {
			return this.put(Double.toString(value));
		}

		public TableRow put(final int value) {
			return this.put(Integer.toString(value));
		}

		public TableRow put(final long value) {
			return this.put(Long.toString(value));
		}
	}

	private final List<TableRow> rows = new LinkedList<>();

	private boolean printNewLines = false;
	private int borderWidth = 0;
	private int cellPadding = 0;

	@Override
	String generateHeader() {
		final StringBuffer buffer = new StringBuffer();
		synchronized (rows) {
			// final int columns = columnCount();

			buffer.append("<table");
			if (borderWidth > 0) {
				buffer.append(" border=\"");
				buffer.append(borderWidth);
				buffer.append('"');
			}
			if (cellPadding > 0) {
				buffer.append("  cellpadding=\"");
				buffer.append(cellPadding);
				buffer.append('"');
			}
			buffer.append('>');
			if (printNewLines)
				buffer.append('\n');
			for (final TableRow row : rows) {
				row.generate(buffer);
				if (printNewLines)
					buffer.append('\n');
			}
			buffer.append("</table>");
		}
		return buffer.toString();
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public int getCellPadding() {
		return cellPadding;
	}

	public void setCellPadding(int cellPadding) {
		this.cellPadding = cellPadding;
	}

	public boolean isPrintingNewLines() {
		return printNewLines;
	}

	public void setPrintNewLines(boolean printNewLines) {
		this.printNewLines = printNewLines;
	}

	public TableRow addRow() {
		final TableRow row = new TableRow();
		synchronized (rows) {
			rows.add(row);
		}
		return row;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void removeRow(final TableRow row) {
		if (row == null)
			return;
		synchronized (rows) {
			while (rows.contains(row))
				rows.remove(row);
		}
	}

	public int rowCount() {
		synchronized (rows) {
			return rows.size();
		}
	}

	public int columnCount() {
		int result = 0;
		synchronized (rows) {
			for (final TableRow row : rows) {
				int columns = row.columns();
				if (columns > result)
					result = columns;
			}
		}
		return result;
	}

}
