package org.controlsfx.control.imageview;

import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class SelectableImageView extends Control {

    /* ************************************************************************
    *                                                                         *
    * Constructor                                                             *
    *                                                                         *
    **************************************************************************/

    public SelectableImageView() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
    }

    /* ************************************************************************
    *                                                                         *
    * Style Sheet & Skin Handling                                             *
    *                                                                         *
    **************************************************************************/

    /**
     * The name of the style class used in CSS for instances of this class.
     */
    private static final String DEFAULT_STYLE_CLASS = "selectable-image-view";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAgentStylesheet() {
        return getClass().getResource("selectableimageview.css").toExternalForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new SelectableImageViewSkin(this);
    }

}
