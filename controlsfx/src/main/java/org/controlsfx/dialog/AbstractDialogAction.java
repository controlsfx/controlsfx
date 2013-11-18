package org.controlsfx.dialog;

import java.util.Arrays;
import java.util.EnumSet;

import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog.ActionTrait;
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
    
    private final EnumSet<ActionTrait> traits;

    /**
     * Creates a dialog action with given text and traits
     * @param text
     * @param traits
     */
    public AbstractDialogAction(String text, ActionTrait... traits) {
        super(text);
        this.traits = EnumSet.copyOf(Arrays.asList(traits));
    }

    /**
     * Creates a dialog action with given text and common set of traits: CLOSING and DEFAULT
     * @param text
     */
    public AbstractDialogAction(String text ) {
        this(text, ActionTrait.CLOSING, ActionTrait.DEFAULT);
    }
    
    
    /** {@inheritDoc} */
    @Override public boolean hasTrait(ActionTrait trait) {
        return traits.contains(trait);
    }

}
