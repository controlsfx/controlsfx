package org.controlsfx.control;

import impl.org.controlsfx.skin.SegmentedButtonSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;

public class SegmentedButton extends Control {
    
    private final ObservableList<ToggleButton> buttons;
    
    /**************************************************************************
     * 
     * Constructors
     * 
     *************************************************************************/
    
    /**
     * 
     */
    public SegmentedButton() {
        this(null);
    }
    
    /**
     * 
     * @param buttons
     */
    public SegmentedButton(ObservableList<ToggleButton> buttons) {
        getStyleClass().add("segmented-button");
        this.buttons = buttons == null ? FXCollections.<ToggleButton>observableArrayList() : buttons;
        setFocusTraversable(true);
    }
    
    @Override protected Skin<?> createDefaultSkin() {
        return new SegmentedButtonSkin(this);
    }
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     *************************************************************************/
    
    /**
     * 
     * @return
     */
    public final ObservableList<ToggleButton> getButtons() {
        return buttons;
    }
    
    
    /**************************************************************************
     * 
     * CSS
     * 
     *************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return SegmentedButton.class.getResource("segmentedbutton.css").toExternalForm();
    }
}