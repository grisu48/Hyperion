package org.feldspaten.hyperion.html;

/**
 * Simple a number of spaces
 *
 */
public class Spaces extends Html {

	/** Number of spaces */
	private int count = 1;

	public Spaces() {
		this(1);
	}

	public Spaces(int count) {
		super();
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	String generateHeader() {
		if (count == 0)
			return "";
		else if (count == 1)
			return " ";
		else {
			final StringBuffer buffer = new StringBuffer();

			for (int i = 0; i < count; i++)
				buffer.append(' ');
			return buffer.toString();
		}
	}

}
