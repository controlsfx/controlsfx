/**
 * Copyright (c) 2014, 2015 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.dialog;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;
import impl.org.controlsfx.ImplUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BooleanSupplier;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Window;

import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.ValidationSupport;

/**
 * <p>The API for creating multi-page Wizards, based on JavaFX {@link Dialog} API.<br> 
 * Wizard can be setup in following few steps:</p>
 * <ul>
 *    <li>Design wizard pages by inheriting them from {@link WizardPane}</li>
 *    <li>Define wizard flow by implementing {@link Wizard.Flow}</li>
 *    <li>Create and instance of the Wizard and assign flow to it</li>
 *    <li>Execute the wizard using showAndWait method</li>
 *    <li>Values can be extracted from settings map by calling getSettings 
 * </ul>    
 * <p>For simple, linear wizards, the {@link LinearFlow} can be used. 
 * It is a flow based on a collection of wizard pages. Here is the example:</p>
 * 
 * <pre>{@code // Create pages. Here for simplicity we just create and instance of WizardPane.
 * WizardPane page1 = new WizardPane(); 
 * WizardPane page2 = new WizardPane(); 
 * WizardPane page2 = new WizardPane(); 
 * 
 * // create wizard
 * Wizard wizard = new Wizard();
 * 
 * // create and assign the flow
 * wizard.setFlow(new LinearFlow(page1, page2, page3));
 *     
 * // show wizard and wait for response
 * wizard.showAndWait().ifPresent(result -> {
 *     if (result == ButtonType.FINISH) {
 *         System.out.println("Wizard finished, settings: " + wizard.getSettings());
 *     }
 * });}</pre>
 * 
 * <p>For more complex wizard flows we suggest to create a custom ones, describing page traversal logic. 
 * Here is a simplified example: </p>
 * 
 * <pre>{@code Wizard.Flow branchingFlow = new Wizard.Flow() {
 *     public Optional<WizardPane> advance(WizardPane currentPage) {
 *         return Optional.of(getNext(currentPage));
 *     }
 *
 *     public boolean canAdvance(WizardPane currentPage) {
 *         return currentPage != page3;
 *     }
 *          
 *     private WizardPane getNext(WizardPane currentPage) {
 *         if ( currentPage == null ) {
 *             return page1;
 *         } else if ( currentPage == page1) {
 *             // skipNextPage() does not exist - this just represents that you
 *             // can add a conditional statement here to change the page.
 *             return page1.skipNextPage()? page3: page2;
 *         } else {
 *             return page3;
 *         }
 *     }
 * };}</pre>
 */
public class Wizard {
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private Dialog<ButtonType> dialog;
    
    private final ObservableMap<String, Object> settings = FXCollections.observableHashMap();
    
    private final Stack<WizardPane> pageHistory = new Stack<>(); 
    
    private Optional<WizardPane> currentPage = Optional.empty();

    private final BooleanProperty invalidProperty = new SimpleBooleanProperty(false);

    // Read settings activated by default for backward compatibility
    private final BooleanProperty readSettingsProperty = new SimpleBooleanProperty(true);
    
    private final ButtonType BUTTON_PREVIOUS = new ButtonType(localize(asKey("wizard.previous.button")), ButtonData.BACK_PREVIOUS); //$NON-NLS-1$
    private final EventHandler<ActionEvent> BUTTON_PREVIOUS_ACTION_HANDLER = actionEvent -> {
        actionEvent.consume();
        currentPage = Optional.ofNullable( pageHistory.isEmpty()? null: pageHistory.pop() );
        updatePage(dialog,false);
    };
    
    private final ButtonType BUTTON_NEXT = new ButtonType(localize(asKey("wizard.next.button")), ButtonData.NEXT_FORWARD); //$NON-NLS-1$
    private final EventHandler<ActionEvent> BUTTON_NEXT_ACTION_HANDLER = actionEvent -> {
        actionEvent.consume();
        currentPage.ifPresent(page->pageHistory.push(page));
        currentPage = getFlow().advance(currentPage.orElse(null));
        updatePage(dialog,true);
    };
    
    private final StringProperty titleProperty = new SimpleStringProperty(); 
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates an instance of the wizard without an owner.
     */
    public Wizard() {
        this(null);
    }
    
    /**
     * Creates an instance of the wizard with the given owner.
     * @param owner The object from which the owner window is deduced (typically
     *      this is a Node, but it may also be a Scene or a Stage). 
     */
    public Wizard(Object owner) {
        this(owner, ""); //$NON-NLS-1$
    }
    
    /**
     * Creates an instance of the wizard with the given owner and title.
     * 
     * @param owner The object from which the owner window is deduced (typically
     *      this is a Node, but it may also be a Scene or a Stage). 
     * @param title The wizard title.
     */
    public Wizard(Object owner, String title) {
    	
        invalidProperty.addListener( (o, ov, nv) -> validateActionState());
        
        dialog = new Dialog<>();
        dialog.titleProperty().bind(this.titleProperty);
        setTitle(title);
        
        Window window = null;
        if ( owner instanceof Window) { 
        	window = (Window)owner;
        } else if ( owner instanceof Node ) {
        	window = ((Node)owner).getScene().getWindow();
        }
        
        dialog.initOwner(window);
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
//    /**
//     * Shows the wizard but does not wait for a user response (in other words,
//     * this brings up a non-blocking dialog). Users of this API must either
//     * poll the {@link #resultProperty() result property}, or else add a listener
//     * to the result property to be informed of when it is set.
//     */
//    public final void show() {
//        dialog.show();
//    }
    
    /**
     * Shows the wizard and waits for the user response (in other words, brings 
     * up a blocking dialog, with the returned value the users input).
     * 
     * @return An {@link Optional} that contains the result.
     */
    public final Optional<ButtonType> showAndWait() {
        return dialog.showAndWait();
    }

    /**
     * @return {@link Dialog#resultProperty()} of the {@link Dialog} representing this {@link Wizard}.
     */
    public final ObjectProperty<ButtonType> resultProperty() {
            return dialog.resultProperty();
    }
    
    /**
     * The settings map is the place where all data from pages is kept once the 
     * user moves on from the page, assuming there is a {@link ValueExtractor} 
     * that is capable of extracting a value out of the various fields on the page. 
     */
    public final ObservableMap<String, Object> getSettings() {
        return settings;
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- title
    
    /**
     * Return the titleProperty of the wizard.
     */
    public final StringProperty titleProperty() {
        return titleProperty;
    }  
    
    /**
     * Return the title of the wizard.
     */
    public final String getTitle() {
    	return titleProperty.get();
    }
    
    /**
     * Change the Title of the wizard.
     * @param title
     */
    public final void setTitle( String title ) {
    	titleProperty.set(title);
    }
    
    // --- flow
    /**
     * The {@link Flow} property represents the flow of pages in the wizard. 
     */
    private ObjectProperty<Flow> flow = new SimpleObjectProperty<Flow>(new LinearFlow()) {
        @Override protected void invalidated() {
            updatePage(dialog,false);
        }
        
        @Override public void set(Flow flow) {
        	super.set(flow);
        	pageHistory.clear();
        	if ( flow != null ) {
        		currentPage = flow.advance(currentPage.orElse(null));
        		updatePage(dialog,true);
        	}
        };
    };
    
    public final ObjectProperty<Flow> flowProperty() {
        return flow;
    }
    
    /**
     * Returns the currently set {@link Flow}, which represents the flow of 
     * pages in the wizard. 
     */
    public final Flow getFlow() {
        return flow.get();
    }
    
    /**
     * Sets the {@link Flow}, which represents the flow of pages in the wizard. 
     */
    public final void setFlow(Flow flow) {
        this.flow.set(flow);
    }
    
    
    // --- Properties
    private static final Object USER_DATA_KEY = new Object();
    
    // A map containing a set of properties for this Wizard
    private ObservableMap<Object, Object> properties;

    /**
      * Returns an observable map of properties on this Wizard for use primarily
      * by application developers - not to be confused with the 
      * {@link #getSettings()} map that represents the values entered by the user
      * into the wizard.
      *
      * @return an observable map of properties on this Wizard for use primarily
      * by application developers
     */
     public final ObservableMap<Object, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableMap(new HashMap<>());
        }
        return properties;
    }
    
    /**
     * Tests if this Wizard has properties.
     * @return true if this Wizard has properties.
     */
     public boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

     
    // --- UserData
    /**
     * Convenience method for setting a single Object property that can be
     * retrieved at a later date. This is functionally equivalent to calling
     * the getProperties().put(Object key, Object value) method. This can later
     * be retrieved by calling {@link #getUserData()}.
     *
     * @param value The value to be stored - this can later be retrieved by calling
     *          {@link #getUserData()}.
     */
    public void setUserData(Object value) {
        getProperties().put(USER_DATA_KEY, value);
    }

    /**
     * Returns a previously set Object property, or null if no such property
     * has been set using the {@link #setUserData(Object)} method.
     *
     * @return The Object that was previously set, or null if no property
     *          has been set or if null was set.
     */
    public Object getUserData() {
        return getProperties().get(USER_DATA_KEY);
    }
    
    /**
     * Sets the value of the property {@code invalid}.
     * 
     * @param invalid The new validation state
     *  {@link #invalidProperty() }
     */
    public final void setInvalid(boolean invalid) {
        invalidProperty.set(invalid);
    }
    
    /**
     * Gets the value of the property {@code invalid}.
     * 
     * @return The validation state
     * @see #invalidProperty() 
     */
    public final boolean isInvalid() {
        return invalidProperty.get();
    }
    
    /**
     * Property for overriding the individual validation state of this {@link Wizard}.
     * Setting {@code invalid} to true will disable the next/finish Button and the user
     * will not be able to advance to the next page of the {@link Wizard}. Setting
     * {@code invalid} to false will enable the next/finish Button. <br>
     * <br>
     * For example you can use the {@link ValidationSupport#invalidProperty()} of a
     * page and bind it to the {@code invalid} property: <br>
     * {@code
     * wizard.invalidProperty().bind(page.validationSupport.invalidProperty());
     * }
     * 
     * @return The validation state property
     */
    public final BooleanProperty invalidProperty() {
        return invalidProperty;
    }
    
    /**
     * Sets the value of the property {@code readSettings}.
     * 
     * @param readSettings The new read-settings state
     * @see #readSettingsProperty()
     */
    public final void setReadSettings(boolean readSettings) {
        readSettingsProperty.set(readSettings);
    }
    
    /**
     * Gets the value of the property {@code readSettings}.
     * 
     * @return The read-settings state
     * @see #readSettingsProperty()
     */
    public final boolean isReadSettings() {
        return readSettingsProperty.get();
    }
    
    /**
    * Property for overriding the individual read-settings state of this {@link Wizard}.
    * Setting {@code readSettings} to true will enable the value extraction for this
    * {@link Wizard}. Setting {@code readSettings} to false will disable the value
    * extraction for this {@link Wizard}.
    *
    * @return The readSettings state property
    */
    public final BooleanProperty readSettingsProperty() {
        return readSettingsProperty;
    }
    
    
    
    /**************************************************************************
     * 
     * Private implementation
     * 
     **************************************************************************/
    
    private void updatePage(Dialog<ButtonType> dialog, boolean advancing) {
        Flow flow = getFlow();
        if (flow == null) {
            return;
        }
        
        Optional<WizardPane> prevPage = Optional.ofNullable( pageHistory.isEmpty()? null: pageHistory.peek()); 
        prevPage.ifPresent( page -> {
	        // if we are going forward in the wizard, we read in the settings 
	        // from the page and store them in the settings map.
	        // If we are going backwards, we do nothing
                // This is only performed if readSettings is true.
	        if (advancing && isReadSettings()) {
	        	readSettings(page);
	        }
	        
	        // give the previous wizard page a chance to update the pages list
	        // based on the settings it has received
	        page.onExitingPage(this);
        });
        
        currentPage.ifPresent(currentPage -> {
            // put in default actions
            List<ButtonType> buttons = currentPage.getButtonTypes();
            if (! buttons.contains(BUTTON_PREVIOUS)) {
                buttons.add(BUTTON_PREVIOUS);
                Button button = (Button)currentPage.lookupButton(BUTTON_PREVIOUS);
                button.addEventFilter(ActionEvent.ACTION, BUTTON_PREVIOUS_ACTION_HANDLER);
            }
            if (! buttons.contains(BUTTON_NEXT)) {
                buttons.add(BUTTON_NEXT);
                Button button = (Button)currentPage.lookupButton(BUTTON_NEXT);
                button.addEventFilter(ActionEvent.ACTION, BUTTON_NEXT_ACTION_HANDLER);
            }
            if (! buttons.contains(ButtonType.FINISH)) buttons.add(ButtonType.FINISH);
            if (! buttons.contains(ButtonType.CANCEL)) buttons.add(ButtonType.CANCEL);
                
            // then give user a chance to modify the default actions
            currentPage.onEnteringPage(this);
            
            // Remove from DecorationPane which has been created by e.g. validation
            if (currentPage.getParent() != null && currentPage.getParent() instanceof Pane) {
                Pane parentOfCurrentPage = (Pane) currentPage.getParent();
                parentOfCurrentPage.getChildren().remove(currentPage);
            }
            
            // Get current position and size
            double previousX = dialog.getX();
            double previousY = dialog.getY();
            double previousWidth = dialog.getWidth();
            double previousHeight = dialog.getHeight();
            // and then switch to the new pane
            dialog.setDialogPane(currentPage);
            // Resize Wizard to new page
            Window wizard = currentPage.getScene().getWindow();
            wizard.sizeToScene();
            // Center resized Wizard to previous position
            
            
            if (!Double.isNaN(previousX) && !Double.isNaN(previousY)) {
                double newWidth = dialog.getWidth();
                double newHeight = dialog.getHeight();
                int newX = (int) (previousX + (previousWidth / 2.0) - (newWidth / 2.0));
                int newY = (int) (previousY + (previousHeight / 2.0) - (newHeight / 2.0));

                ObservableList<Screen> screens = Screen.getScreensForRectangle(previousX, previousY, 1, 1);
                Screen screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
                Rectangle2D scrBounds = screen.getBounds();
                int minX = (int)Math.round(scrBounds.getMinX());
                int maxX = (int)Math.round(scrBounds.getMaxX());
                int minY = (int)Math.round(scrBounds.getMinY());
                int maxY = (int)Math.round(scrBounds.getMaxY());
                if(newX + newWidth > maxX) {
                    newX = maxX - (int)Math.round(newWidth);
                }
                if(newY + newHeight > maxY) {
                    newY = maxY - (int)Math.round(newHeight);
                }                
                if(newX < minX) {
                    newX = minX;
                }
                if(newY < minY) {
                    newY = minY;
                }

                dialog.setX(newX);
                dialog.setY(newY);
            }
        });
        
        validateActionState();
    }
    
    private void validateActionState() {
        final List<ButtonType> currentPaneButtons = dialog.getDialogPane().getButtonTypes();
        
        if (getFlow().canAdvance(currentPage.orElse(null))) {
            currentPaneButtons.remove(ButtonType.FINISH);
        } else {
            currentPaneButtons.remove(BUTTON_NEXT);
        }

        validateButton( BUTTON_PREVIOUS, () -> pageHistory.isEmpty());
        validateButton( BUTTON_NEXT,     () -> invalidProperty.get());
        validateButton( ButtonType.FINISH,     () -> invalidProperty.get());

    }
    
    // Functional design allows to delay condition evaluation until it is actually needed 
    private void validateButton( ButtonType buttonType, BooleanSupplier condition) {
    	Button btn = (Button)dialog.getDialogPane().lookupButton(buttonType);
        if ( btn != null ) {
            Node focusOwner = (btn.getScene() != null) ? btn.getScene().getFocusOwner() : null;
            btn.setDisable(condition.getAsBoolean());
            if(focusOwner != null) {
                focusOwner.requestFocus();
            }
        }
    }
    
    private int settingCounter;
    private void readSettings(WizardPane page) {
        // for now we cannot know the structure of the page, so we just drill down
        // through the entire scenegraph (from page.content down) until we get
        // to the leaf nodes. We stop only if we find a node that is a
        // ValueContainer (either by implementing the interface), or being 
        // listed in the internal valueContainers map.
        
        settingCounter = 0;
        checkNode(page.getContent());
    }
    
    private boolean checkNode(Node n) {
        boolean success = readSetting(n);
        
        if (success) {
            // we've added the setting to the settings map and we should stop drilling deeper
            return true;
        } else {
            /**
             * go into children of this node (if possible) and see if we can get
             * a value from them (recursively) We use reflection to fix
             * https://bitbucket.org/controlsfx/controlsfx/issue/412 .
             */
            List<Node> children = ImplUtils.getChildren(n, true);
            
            // we're doing a depth-first search, where we stop drilling down
            // once we hit a successful read
            boolean childSuccess = false;
            for (Node child : children) {
                childSuccess |= checkNode(child);
            }
            return childSuccess;
        }
    }
    
    private boolean readSetting(Node n) {
        if (n == null) {
            return false;
        }
        
        Object setting = ValueExtractor.getValue(n);
        
        if (setting != null) {
            // save it into the settings map.
            // if the node has an id set, we will use that as the setting name
            String settingName = n.getId();
            
            // but if the id is not set, we will use a generic naming scheme
            if (settingName == null || settingName.isEmpty()) {
                settingName = "page_" /*+ previousPageIndex*/ + ".setting_" + settingCounter;  //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            getSettings().put(settingName, setting);
            
            settingCounter++;
        }
        
        return setting != null;
    }
    
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
    
    
    /**
     * Represents the page flow of the wizard. It defines only methods required 
     * to move forward in the wizard logic, as backward movement is automatically 
     * handled by wizard itself, using internal page history.   
     */
    public interface Flow {
    	
    	/** 
    	 * Advances the wizard to the next page if possible.
    	 * 
    	 * @param currentPage The current wizard page
    	 * @return {@link Optional} value containing the next wizard page. 
    	 */
    	Optional<WizardPane> advance(WizardPane currentPage);
    	
    	/**
    	 * Check if advancing to the next page is possible
    	 * 
    	 * @param currentPage The current wizard page
    	 * @return true if it is possible to advance to the next page, false otherwise.
    	 */
    	boolean canAdvance(WizardPane currentPage);
    }
    
    
    /**
     * LinearFlow is an implementation of the {@link Wizard.Flow} interface,
     * designed to support the most common type of wizard flow - namely, a linear 
     * wizard page flow (i.e. through all pages in the order that they are specified).
     * Therefore, this {@link Flow} implementation simply traverses a collections of
     * {@link WizardPane WizardPanes}.
     * 
     * <p>For example of how to use this API, please refer to the {@link Wizard} 
     * documentation</p>
     * 
     * @see Wizard
     * @see WizardPane 
     */
    public static class LinearFlow implements Wizard.Flow {

        private final List<WizardPane> pages;

        /**
         * Creates a new LinearFlow instance that will allow for stepping through
         * the given collection of {@link WizardPane} instances.
         */
        public LinearFlow( Collection<WizardPane> pages ) {
            this.pages = new ArrayList<>(pages);
        }

        /**
         * Creates a new LinearFlow instance that will allow for stepping through
         * the given varargs array of {@link WizardPane} instances.
         */
        public LinearFlow( WizardPane... pages ) {
            this( Arrays.asList(pages));
        }

        /** {@inheritDoc} */
        @Override public Optional<WizardPane> advance(WizardPane currentPage) {
            int pageIndex = pages.indexOf(currentPage);
            return Optional.ofNullable( pages.get(++pageIndex) );
        }

        /** {@inheritDoc} */
        @Override public boolean canAdvance(WizardPane currentPage) {
            int pageIndex = pages.indexOf(currentPage);
            return pages.size()-1 > pageIndex; 
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Methods for the sake of unit tests
     * 
     **************************************************************************/
    
    /**
     * @return The {@link Dialog} representing this {@link Wizard}. <br>
     *         This is actually for {@link Dialog} reading-purposes, e.g.
     *         unit testing the {@link DialogPane} content.
     */
    Dialog<ButtonType> getDialog() {
        return dialog;
    }
    
}
