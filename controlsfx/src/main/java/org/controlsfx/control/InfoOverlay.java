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

package org.controlsfx.control;

import impl.org.controlsfx.skin.InfoOverlaySkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.image.ImageView;

/**
 * A simple UI control that allows for an information popup to be displayed over 
 * a node to describe it in further detail. In some ways, it can be thought of 
 * as a always visible tooltip (although by default it is collapsed so only the
 * first line is shown - clicking on it will expand it to show all text).
 * 
 * <p>Shown below is a screenshot of the InfoOverlay control in both its 
 * collapsed and expanded states:
 * 
 * <br>
 * <center>
 * <img src="infoOverlay.png" alt="Screenshot of InfoOverlay">
 * </center>
 */
public class InfoOverlay extends ControlsFXControl {
    
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Constructs a default InfoOverlay control with no node or text. 
     */
    public InfoOverlay() {
        this((Node)null, null);
    }
    
    /**
     * Attempts to construct an InfoOverlay instance using the given string
     * to load an image, and to place the given text string over top of it.
     * 
     * @param imageUrl The image file to attempt to load.
     * @param text The text to display over top of the image.
     */
    public InfoOverlay(String imageUrl, String text) {
        this(new ImageView(imageUrl), text);
    }

    /**
     * Constructs an InfoOverlay instance using the given Node (which can be
     * an arbitrarily complex node / scenegraph, or a simple ImageView, for example),
     * and places the given text string over top of it.
     * 
     * @param content The arbitrarily complex scenegraph over which the text will be displayed.
     * @param text The text to display over top of the node.
     */
    public InfoOverlay(Node content, String text) {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        setContent(content);
        setText(text);
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/
    
    /** {@inheritDoc} */   
    @Override protected Skin<?> createDefaultSkin() {
        return new InfoOverlaySkin(this);
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // --- content
     private ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content"); //$NON-NLS-1$
     
    /**
     * 
     * @return an {@link ObjectProperty} containing the arbitrarily complex
     * scenegraph over which the text will be displayed.
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    /**
     * Sets a new value for the {@link #contentProperty() }.
     * @param content 
     */
    public final void setContent(Node content) {
        contentProperty().set(content);
    }

    /**
     * 
     * @return the arbitrarily complex scenegraph over which the text will be 
     * displayed.
     */
    public final Node getContent() {
        return contentProperty().get();
    }

    
    // --- text
    
    private StringProperty text = new SimpleStringProperty(this, "text"); //$NON-NLS-1$
    
    /**
     * @return A {@link StringProperty} representing the text displayed over top
     * of the {@link #contentProperty() content}.
     */
    public final StringProperty textProperty() {
        return text;
    }

    /**
     * 
     * @return The text displayed over top of the {@link #contentProperty() content}.
     */
    public final String getText() {
        return textProperty().get();
    }

    /**
     * Specifies the text to display over top of the {@link #contentProperty() content}.
     * @param text 
     */
    public final void setText(String text) {
        textProperty().set(text);
    }
    
    
    // --- showOnHover
    private BooleanProperty showOnHover = new SimpleBooleanProperty(this, "showOnHover", true); //$NON-NLS-1$
    
    /**
     * 
     * @return A {@link BooleanProperty} representing whether the overlay on 
     * hover of the content node is showing.
     */
    public final BooleanProperty showOnHoverProperty() {
        return showOnHover;
    }

    /**
     * 
     * @return whether the overlay on hover of the content node is showing.
     */
    public final boolean isShowOnHover() {
        return showOnHoverProperty().get();
    }

    /**
     * Specifies whether to show the overlay on hover of the content node (and 
     * to hide it again when the content is no longer being hovered). By default 
     * this is true. 
     * @param value 
     */
    public final void setShowOnHover(boolean value) {
        showOnHoverProperty().set(value);
    }

    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/
    
    private static final String DEFAULT_STYLE_CLASS = "info-overlay"; //$NON-NLS-1$

    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(InfoOverlay.class, "info-overlay.css");
    }
}
