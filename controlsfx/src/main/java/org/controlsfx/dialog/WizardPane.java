package org.controlsfx.dialog;

import javafx.scene.control.DialogPane;

/**
 * WizardPane is the base class for all wizard pages. The API is essentially
 * the {@link DialogPane}, with the addition of convenience methods related
 * to {@link #onEnteringPage(Wizard) entering} and 
 * {@link #onExitingPage(Wizard) exiting} the page.
 */
public class WizardPane extends DialogPane {

    /**
     * Creates an instance of wizard pane.
     */
    public WizardPane() {
            getStyleClass().add("wizard-pane");
    }

    /**
     * Called on entering a page. This is a good place to read values from wizard settings 
     * and assign them to controls on the page
     * @param wizard which page will be used on
     */
    public void onEnteringPage(Wizard wizard) {
        // no-op
    }
    
    /**
     * Called on existing the page. 
     * This is a good place to read values from page controls and store them in wizard settings
     * @param wizard which page was used on
     */
    public void onExitingPage(Wizard wizard) {
        // no-op
    }
}
