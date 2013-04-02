package org.controlsfx.dialogs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class Dialog<T> {

	private final Stage owner;
	private String title;
	private String message;
	private String masthead;
	private Options options;
	private String details;
	private boolean openDetailsInNewWindow = false;
	private T inputInitialValue = null;
	private List<T> inputChoices = null;

	/**
	 * TODO delete me - this is just for testing!!
	 */
	public static void setMacOS(boolean b) {
		DialogTemplate.setMacOS(b);
	}

	public static void setWindows(boolean b) {
		DialogTemplate.setWindows(b);
	}

	// NOT PUBLIC API
	static enum Type {
		// TODO maybe introduce a MORE_DETAILS type, rather than use the ERROR
		// type?
		ERROR(Options.OK, "error.image") {
			@Override
			public String getDefaultTitle() {
				return "Error";
			}

			@Override
			public String getDefaultMasthead() {
				return "Error";
			}
		},
		INFORMATION(Options.OK, "info.image") {
			@Override
			public String getDefaultTitle() {
				return "Message";
			}

			@Override
			public String getDefaultMasthead() {
				return "Message";
			}
		},
		WARNING(Options.OK, "warning.image") {
			@Override
			public String getDefaultTitle() {
				return "Warning";
			}

			@Override
			public String getDefaultMasthead() {
				return "Warning";
			}
		},
		CONFIRMATION(Options.YES_NO_CANCEL, "confirm.image") {
			@Override
			public String getDefaultTitle() {
				return "Select an option";
			}

			@Override
			public String getDefaultMasthead() {
				return "Select an option";
			}
		},
		INPUT(Options.OK_CANCEL, "confirm.image") {
			@Override
			public String getDefaultTitle() {
				return "Select an option";
			}

			@Override
			public String getDefaultMasthead() {
				return "Select an option";
			}
		};

		private final Options defaultOptions;
		private final String imageResource;
		private Image image;

		Type(Options defaultOptions, String imageResource) {
			this.defaultOptions = defaultOptions;
			this.imageResource = imageResource;
		}

		public Image getImage() {
			if (image == null) {
				image = DialogResources.getImage(imageResource);
			}
			return image;
		}

		public abstract String getDefaultMasthead();

		public abstract String getDefaultTitle();

		public Options getDefaultOptions() {
			return defaultOptions;
		}
	}

	/**
	 * An enumeration used to specify the response provided by the user when
	 * interacting with a dialog.
	 */
	public static enum Response {
		YES, NO, CANCEL, OK, CLOSED
	}

	/**
	 * An enumeration used to specify which buttons to show to the user in a
	 * Dialog.
	 */
	public static enum Options {
		YES_NO, YES_NO_CANCEL, OK, OK_CANCEL;
	}

	public static <T> Dialog<T> build(final Stage owner) {
		return new Dialog<T>(owner);
	}

	private Dialog(final Stage owner) {
		this.owner = owner;
	}

	public Dialog<T> title(final String title) {
		this.title = title;
		return this;
	}

	public Dialog<T> message(final String message) {
		this.message = message;
		return this;
	}

	public Dialog<T> masthead(final String masthead) {
		this.masthead = masthead;
		return this;
	}

	public Dialog<T> options(final Options options) {
		this.options = options;
		return this;
	}

	public Dialog<T> details(final String details) {
		this.details = details;
		return this;
	}

	public Dialog<T> openDetailsInNewWindow(final boolean openDetailsInNewWindow) {
		this.openDetailsInNewWindow = openDetailsInNewWindow;
		return this;
	}

	public Dialog<T> details(final Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		details(sw.toString());
		return this;
	}

	public Dialog<T> inputInitialValue(final T initialValue) {
		this.inputInitialValue = initialValue;
		return this;
	}

	public Dialog<T> inputChoices(final List<T> choices) {
		this.inputChoices = choices;
		return this;
	}

	public Dialog<T> inputChoices(
	        @SuppressWarnings("unchecked") final T... choices) {
		return inputChoices(Arrays.asList(choices));
	}

	public void showInformationDialog() {
		showSimpleContentDialog(Type.INFORMATION);
	}

	public Response showConfirmDialog() {
		return showSimpleContentDialog(Type.CONFIRMATION);
	}

	public Response showWarningDialog() {
		return showSimpleContentDialog(Type.WARNING);
	}

	public Response showErrorDialog() {
		return showSimpleContentDialog(Type.ERROR);
	}

	// TODO: Has to be generalized to have details for any type of dialog
	public Response showMoreDetailsDialog() {
		DialogTemplate<Void> template = new DialogTemplate<>(owner, title,
		        masthead, null);
		template.setMoreDetailsContent(message, details, openDetailsInNewWindow);
		return showDialog(template);
	}

	public T showInputDialog() {
		DialogTemplate<T> template = new DialogTemplate<>(owner, title,
		        masthead, null);
		template.setInputContent(message, inputInitialValue, inputChoices);
		template.getDialog().centerOnScreen();
		template.show();
		return template.getInputResponse();
	}

	/***************************************************************************
	 * Private API
	 **************************************************************************/

	private DialogTemplate<T> getDialogTemplate(final Type dlgType) {
		String actualTitle = title == null ? dlgType.getDefaultTitle() : title;
		String actualMasthead = masthead == null ? dlgType.getDefaultMasthead()
		        : masthead;
		Options actualOptions = options == null ? dlgType.getDefaultOptions()
		        : options;
		return new DialogTemplate<T>(owner, actualTitle, actualMasthead,
		        actualOptions);
	}

	private Response showSimpleContentDialog(final Type dlgType) {
		DialogTemplate<T> template = getDialogTemplate(dlgType);
		template.setSimpleContent(message, dlgType);
		return showDialog(template);
	}

	private static <T> Response showDialog(DialogTemplate<T> template) {
		try {
			template.getDialog().centerOnScreen();
			template.show();
			return template.getResponse();
		} catch (Throwable e) {
			return Response.CLOSED;
		}
	}

}
