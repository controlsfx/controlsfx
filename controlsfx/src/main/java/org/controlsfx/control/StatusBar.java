/**
 * Copyright (c) 2014, 2016 ControlsFX
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

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

import impl.org.controlsfx.skin.StatusBarSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Skin;

/**
 * The StatusBar control is normally placed at the bottom of a window. It is
 * used to display various types of application status information. This can be
 * a text message, the progress of a task, or any other kind of status (e.g. red
 * / green / yellow lights). By default the status bar contains a label for
 * displaying plain text and a progress bar (see {@link ProgressBar}) for long
 * running tasks. Additional controls / nodes can be placed on the left and
 * right sides (see {@link #getLeftItems()} and {@link #getRightItems()}).
 * 
 * <h3>Screenshots</h3> 
 * The picture below shows the default appearance of the StatusBar control: 
 * <center><img src="statusbar.png" alt="Screenshot of StatusBar"></center> 
 * 
 * <br>
 * The following picture shows the status bar reporting progress of a task:
 * <center><img src="statusbar-progress.png" alt="Screenshot of StatusBar 
 * reporting progress of a task"></center> 
 * 
 * <br>
 * The last picture shows the status bar reporting progress, along with a couple 
 * of extra items added to the left and right areas of the bar: 
 * <center><img src="statusbar-items.png" alt="Screenshot of StatusBar
 * reporting progress, along with a couple of extra items"></center>
 * 
 * <h3>Code Sample</h3>
 * 
 * <pre>
 * StatusBar statusBar = new StatusBar();
 * statusBar.getLeftItems().add(new Button(&quot;Info&quot;));
 * statusBar.setProgress(.5);
 * </pre>
 */
public class StatusBar extends ControlsFXControl {

    /**
     * Constructs a new status bar control.
     */
    public StatusBar() {
        getStyleClass().add("status-bar"); //$NON-NLS-1$
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new StatusBarSkin(this);
    }

    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(StatusBar.class, "statusbar.css");
    }
    
    private final StringProperty text = new SimpleStringProperty(this, "text", //$NON-NLS-1$
            localize(asKey("statusbar.ok"))); //$NON-NLS-1$

    /**
     * The property used for storing the text message shown by the status bar.
     * 
     * @return the text message property
     */
    public final StringProperty textProperty() {
        return text;
    }

    /**
     * Sets the value of the {@link #textProperty()}.
     * 
     * @param text the text shown by the label control inside the status bar
     */
    public final void setText(String text) {
        textProperty().set(text);
    }

    /**
     * Returns the value of the {@link #textProperty()}. 
     * 
     * @return the text currently shown by the status bar
     */
    public final String getText() {
        return textProperty().get();
    }

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(
            this, "graphic"); //$NON-NLS-1$

    /**
     * The property used to store a graphic node that can be displayed by the 
     * status label inside the status bar control.
     * 
     * @return the property used for storing a graphic node
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    /**
     * Returns the value of the {@link #graphicProperty()}.
     * 
     * @return the graphic node shown by the label inside the status bar
     */
    public final Node getGraphic() {
        return graphicProperty().get();
    }

    /**
     * Sets the value of {@link #graphicProperty()}.
     * 
     * @param node the graphic node shown by the label inside the status bar
     */
    public final void setGraphic(Node node) {
        graphicProperty().set(node);
    }

    private final StringProperty styleTextProperty = new SimpleStringProperty();
    /**
     * A string representation of the CSS style associated with this Text.{@link Node#setStyle(java.lang.String)
     * }
     *
     * @param style
     */
    public void setStyleText(String style) {
        styleTextProperty.set(style);
    }

    /**
     * A string representation of the CSS style associated with this Text. {@link Node#getStyle()
     * }
     *
     * @return the style applied on the text.
     */
    public String getStyleText() {
        return styleTextProperty.get();
    }

    /**
     * Return the BooleanProperty associated with the style applied to the text.
     *
     * @return the BooleanProperty associated with the style applied to the text.
     */
    public final StringProperty styleTextProperty() {
        return styleTextProperty;
    }
    
    private final ObservableList<Node> leftItems = FXCollections
            .observableArrayList();

    /**
     * Returns the list of items / nodes that will be shown to the left of the status label.
     * 
     * @return the items on the left-hand side of the status bar
     */
    public final ObservableList<Node> getLeftItems() {
        return leftItems;
    }

    private final ObservableList<Node> rightItems = FXCollections
            .observableArrayList();

    /**
     * Returns the list of items / nodes that will be shown to the right of the status label.
     * 
     * @return the items on the left-hand side of the status bar
     */
    public final ObservableList<Node> getRightItems() {
        return rightItems;
    }

    private final DoubleProperty progress = new SimpleDoubleProperty(this,
            "progress"); //$NON-NLS-1$

    /**
     * The property used to store the progress, a value between 0 and 1. A negative
     * value causes the progress bar to show an indeterminate state.
     * 
     * @return the property used to store the progress of a task
     */
    public final DoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Sets the value of the {@link #progressProperty()}.
     * 
     * @param progress the new progress value
     */
    public final void setProgress(double progress) {
        progressProperty().set(progress);
    }

    /**
     * Returns the value of {@link #progressProperty()}.
     * 
     * @return the current progress value
     */
    public final double getProgress() {
        return progressProperty().get();
    }
}
