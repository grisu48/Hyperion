package org.feldspaten.hyperion.html;

public class Form extends Html {

	private String action = "";
	private String method = "POST";

	public Form(String action) {
		super();
		this.action = action;
	}

	public Form(String action, String method) {
		super();
		this.action = action;
		this.method = method;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	String generateHeader() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<FORM");
		if (!action.isEmpty()) {
			buffer.append(" action=\"");
			buffer.append(action);
			buffer.append('"');
		}
		if (!method.isEmpty()) {
			buffer.append(" method=\"");
			buffer.append(method);
			buffer.append('"');
		}
		buffer.append('>');
		return buffer.toString();
	}

	@Override
	String generateFooter() {
		return "</FORM>";
	}

	public SubmitButton addSubmitButton(String text) {
		final SubmitButton button = new SubmitButton(text);
		this.addComponent(button);
		return button;
	}

	public ResetButton addResetButton(String text) {
		final ResetButton button = new ResetButton(text);
		this.addComponent(button);
		return button;
	}

}
