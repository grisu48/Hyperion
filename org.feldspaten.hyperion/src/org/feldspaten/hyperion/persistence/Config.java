package org.feldspaten.hyperion.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Configuration file. A configuration file stores properties in the following
 * format:<br>
 * <b>NAME</b>=<b>VALUE</b> or <br>
 * <b>NAME</b> = <b>VALUE</b><br>
 * Lines in the configuration file that start with a <i>#</i> or <i>;</i> are
 * comments and therefore ignored
 * 
 */
public class Config {

	/**
	 * Values from the file The values are stored as a two-dimensional hashmap.
	 * The first dimension specifies the section, the secon dimension the name
	 * of the configuration values
	 */
	private final HashMap<String, HashMap<String, String>> values = new HashMap<>();

	/** Actual pathname for this instance */
	// private final String path;

	/** File instance for the config file */
	private final File file;

	/** Modification time the last time the value was read */
	private static long modificationTime = 0L;

	/**
	 * Creates new {@link Config} instance with the given configuration file
	 * 
	 * @param path
	 *            Pathname to the config file
	 */
	public Config(final String path) {
		super();
		if (path.isEmpty())
			throw new IllegalArgumentException("Empty config file");
		this.file = new File(path);
		readFile();
	}

	private boolean isFresh() {
		return file.lastModified() == modificationTime;
	}

	private static String normalizeName(final String name) {
		return name.trim().toLowerCase();
	}

	/**
	 * Reads the configuration file. Stores the values in the {@link HashMap}
	 * {@link Config#values}
	 */
	private void readFile() {
		values.clear();
		// Read file
		try {
			final Scanner scanner = new Scanner(file);
			HashMap<String, String> currentSection = new HashMap<String, String>();
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().trim();
				if (line.isEmpty() || line.startsWith("#")
						|| line.startsWith(";"))
					continue;

				if (line.startsWith("[") && line.endsWith("]")) {
					if (line.equals("[]"))
						continue;
					String name = line.substring(1);
					name = name.substring(0, line.length() - 2);
					name = normalizeName(name);
					currentSection = new HashMap<String, String>();
					values.put(name, currentSection);
				} else {
					final String[] split = line.split("=", 2);
					if (split.length < 2)
						continue;

					final String name = normalizeName(split[0]);
					final String value = split[1].trim();

					if (name.isEmpty())
						continue;
					if (currentSection == null)
						continue;
					currentSection.put(name, value);
				}
			}
			scanner.close();

			modificationTime = file.lastModified();
		} catch (FileNotFoundException e) {
			// Empty file
			values.clear();
		}
	}

	public synchronized String getString(String name, String section,
			String defaultValue) {
		if (!isFresh())
			readFile();

		section = normalizeName(section);
		name = normalizeName(name);
		final HashMap<String, String> values = this.values.get(section);
		if (values == null)
			return defaultValue;
		String result = values.get(name);
		if (result == null)
			return defaultValue;
		else
			return result;
	}

	public String getString(String name, String section) {
		return getString(name, section, "");
	}

	public int getInt(String name, String section) {
		return getInt(name, section, 0);
	}

	public int getInt(String name, String section, int defaultValue) {
		String value = getString(name, section, Integer.toString(defaultValue));
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public synchronized void setInt(String name, String section, int value) {
		setString(name, section, Integer.toString(value));
	}

	public synchronized void setString(final String name, String section,
			final String value) {
		// Read file, set values except the given value
		final List<String> lines = new LinkedList<String>();

		boolean inserted = false; // Indicating if the values is inserted in
		// the file. If not found, then we need to
		// add it at the end of the file

		// Read file
		try {
			// Important: Do no use readFile() but this method, because so it is
			// a in-situ replacement

			final Scanner scanner = new Scanner(file);
			String currentSection = "";
			while (scanner.hasNextLine()) {
				final String raw = scanner.nextLine();
				final String line = raw.trim();
				if (line.isEmpty() || line.startsWith("#")
						|| line.startsWith(";")) {
					lines.add(raw);
					continue;
				}

				if (line.startsWith("[") && line.endsWith("]")) {
					if (line.equals("[]"))
						continue;
					currentSection = line.substring(1);
					currentSection = line.substring(0, line.length() - 2);
					currentSection = normalizeName(currentSection);
					lines.add(raw);
				} else {
					final String[] split = line.split("=", 2);
					if (split.length < 2) {
						lines.add(raw);
						continue;
					}

					String currentName = normalizeName(split[0]);
					if (currentSection.equalsIgnoreCase(section)
							&& currentName.equalsIgnoreCase(name)) {
						if (!value.isEmpty()) {
							lines.add(name + " = " + value);
							inserted = true;
						}
					} else {
						lines.add(raw);
					}
				}
			}
			scanner.close();

		} catch (FileNotFoundException e) {
			// Empty file. So we have not inserted nothing for sure
			inserted = false;
		}

		// Add settings line, if not yet inserted
		if (!inserted)
			lines.add(name + " = " + value);

		// Write to file
		try {
			final FileOutputStream output = new FileOutputStream(file, false);
			try {
				final PrintWriter writer = new PrintWriter(output);

				// Make sure we don't add too much newlines
				boolean isFirst = true;
				for (final String line : lines) {
					if (isFirst)
						isFirst = false;
					else
						writer.println();
					writer.print(line);
				}

				writer.flush();
				writer.close();
			} finally {
				output.close();
			}
		} catch (IOException e) {
			System.err.println("Error writing to config file: "
					+ e.getMessage());
		}

		// Ok, re-read file to update all values
		readFile();
	}

	public long getLong(final String name, String section,
			final long defaultValue) {
		String value = getString(name, section, Long.toString(defaultValue));
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public long getLong(final String name, String section) {
		return getLong(name, section, 0L);
	}

	public boolean getBoolean(String name, String section) {
		return getBoolean(name, section, false);
	}

	public boolean getBoolean(String name, String section, boolean defaultValue) {
		String value = getString(name, section);
		if (value == null)
			return defaultValue;
		value = value.trim();
		if (value.isEmpty())
			return defaultValue;

		final String[] trueValues = new String[] { "true", "on", "yes", "y",
				"1" };
		final String[] falseValues = new String[] { "false", "off", "no", "n",
				"0" };

		for (final String v : trueValues)
			if (value.equalsIgnoreCase(v))
				return true;
		for (final String v : falseValues)
			if (value.equalsIgnoreCase(v))
				return false;

		return defaultValue;
	}

	/**
	 * Re-Reads the file
	 */
	public synchronized void read() {
		readFile();
	}

	public void setInteger(String name, String section, int value) {
		setString(name, section, Integer.toString(value));
	}

	public void setLong(String name, String section, long value) {
		setString(name, section, Long.toString(value));
	}

	public void setBoolean(String name, String section, boolean value) {
		setString(name, section, (value ? "true" : "false"));
	}

}
