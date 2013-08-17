package org.controlsfx.dialog;

import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog.DialogAction;

/**
 * A convenience class that implements the {@link Action} and {@link DialogAction} interfaces and provides
 * a simpler API. It is highly recommended to use this class rather than 
 * implement the {@link Action} or the {@link DialogAction} interfaces directly.
 * 
 * <p>To better understand how to use actions, and where they fit within the
 * JavaFX ecosystem, refer to the {@link Action} class documentation.
 * 
 * @see Action
 * @see DialogAction
 */
public abstract class AbstractDialogAction extends AbstractAction implements DialogAction {
    
    private boolean _closing;
    private boolean _default;
    private boolean _cancel;

    public AbstractDialogAction(String text, boolean isClosing, boolean isDefault, boolean isCancel ) {
        super(text);
        _closing = isClosing;
        _default = isDefault;
        _cancel  = isCancel;
    }

    @Override public boolean isClosing() {
        return _closing;
    }

    @Override public boolean isDefault() {
        return _default;
    }

    @Override public boolean isCancel() {
        return _cancel;
    }

}
