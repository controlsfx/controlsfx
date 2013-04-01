package org.controlsfx.dialogs;

import static org.controlsfx.dialogs.Dialogs.DialogResponse.CLOSED;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.controlsfx.dialogs.Dialogs.DialogOptions;
import org.controlsfx.dialogs.Dialogs.DialogResponse;
import org.controlsfx.dialogs.Dialogs.DialogType;

import javafx.stage.Stage;

public final class DialogBuilder<T> {

	private final Stage owner;
	private String title;
	private String message;
	private String masthead;
	private DialogOptions options;
	private String details;
	private boolean openDetailsInNewWindow = false;
	private T inputInitialValue = null;
	private List<T> inputChoices = null;
	
	protected DialogBuilder( final Stage owner ) {
		this.owner = owner;
	}
	
	public DialogBuilder<T> title( final String title ) {
		this.title = title;
		return this;
	}

	public DialogBuilder<T> message( final String message ) {
		this.message = message;
		return this;
	}

	public DialogBuilder<T> masthead( final String masthead ) {
		this.masthead = masthead;
		return this;
	}

	public DialogBuilder<T> options( final DialogOptions options ) {
		this.options = options;
		return this;
	}
	
	public DialogBuilder<T> details( final String details ) {
		this.details = details;
		return this;
	}
	
	public DialogBuilder<T> openDetailsInNewWindow( final boolean openDetailsInNewWindow ) {
		this.openDetailsInNewWindow = openDetailsInNewWindow;
		return this;
	}

	public DialogBuilder<T> details( final Throwable throwable ) {
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        details(sw.toString());
		return this;
	}
	
	public DialogBuilder<T> inputInitialValue( final T initialValue ) {
		this.inputInitialValue = initialValue;
		return this;
	}
	
	public DialogBuilder<T> inputChoices( final List<T> choices ) {
		this.inputChoices = choices;
		return this;
	}
	
	public void showInformationDialog() {
		 showSimpleContentDialog( DialogType.INFORMATION );
	}
	
	public DialogResponse showConfirmDialog() {
		return showSimpleContentDialog( DialogType.CONFIRMATION );
	}

	public DialogResponse showWarningDialog() {
		return showSimpleContentDialog( DialogType.WARNING );
	}

	public DialogResponse showErrorDialog() {
		return showSimpleContentDialog( DialogType.ERROR );
	}

	//TODO: Has to be generalized to have details for any type of dialog
	public DialogResponse showMoreDetailsDialog() {
		DialogTemplate<Void> template = new DialogTemplate<>(owner, title, masthead, null);
		template.setMoreDetailsContent(message, details, openDetailsInNewWindow);
		return showDialog(template);
	}
	
	   
    public T showInputDialog() {
        DialogTemplate<T> template = new DialogTemplate<>(owner, title, masthead, null);
        template.setInputContent(message, inputInitialValue, inputChoices);
        template.getDialog().centerOnScreen();
        template.show();
        return template.getInputResponse();
    }
	
	
	 /***************************************************************************
     * Private API
     **************************************************************************/
	
	private DialogTemplate<T> getDialogTemplate( final DialogType dlgType ) {
		String actualTitle = title == null? dlgType.getDefaultTitle(): title;
		String actualMasthead = masthead == null? dlgType.getDefaultMasthead(): masthead;
		DialogOptions actualOptions = options == null? dlgType.getDefaultOptions(): options; 
	    return new DialogTemplate<T>(owner, actualTitle, actualMasthead, actualOptions );
	}
	
    private DialogResponse showSimpleContentDialog( final DialogType dlgType ){
    	DialogTemplate<T> template = getDialogTemplate(dlgType);
        template.setSimpleContent(message, dlgType);
        return showDialog(template);
    }
	
    private static <T> DialogResponse showDialog(DialogTemplate<T> template) {
        try {
            template.getDialog().centerOnScreen();
            template.show();
            return template.getResponse();
        } catch (Throwable e) {
            return CLOSED;
        }
    }
    
	
}
