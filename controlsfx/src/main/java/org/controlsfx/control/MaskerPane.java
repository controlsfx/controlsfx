/**
 * Copyright (c) 2014, 2015, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
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

import impl.org.controlsfx.skin.MaskerPaneSkin;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;


/**
 * <p>MaskerPane is designed to be placed alongside other controls in a {@link StackPane},
 * in order to visually mask these controls, preventing them from being accessed
 * for a short period of time. This comes in handy whenever waiting on asynchronous
 * code to finish, and you do not want the user to be able to modify the state
 * of the UI while waiting.</p>
 *
 * <p>To use this control, it is necessary to place it as the last child in a {@link StackPane},
 * with the other children being masked by this MaskerPane when visible. Simply use
 * {@link #setVisible(boolean)} to toggle between visible states.</p>
 */
public class MaskerPane extends ControlsFXControl {

    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Construct a new {@link MaskerPane}
     */
    public MaskerPane() { getStyleClass().add("masker-pane"); } //$NON-NLS-1$



    /**************************************************************************
     *
     * Properties
     *
     **************************************************************************/

    // -- Background Color

    // -- Progress
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1.0); //$NON-NLS-1$
    public final DoubleProperty progressProperty() { return progress; }
    public final double getProgress() { return progress.get(); }
    public final void setProgress(double progress) { this.progress.set(progress); }

    // -- Progress Node
    private final ObjectProperty<Node> progressNode = new SimpleObjectProperty<Node>() {
        {
            ProgressIndicator node = new ProgressIndicator();
            node.progressProperty().bind(progress);
            setValue(node);
        }

        @Override public String getName() { return "progressNode"; } //$NON-NLS-1$
        @Override public Object getBean() { return MaskerPane.this; }
    };
    public final ObjectProperty<Node> progressNodeProperty() { return progressNode; }
    public final Node getProgressNode() { return progressNode.get();}
    public final void setProgressNode(Node progressNode) { this.progressNode.set(progressNode); }

    // -- Progress Visibility
    private final BooleanProperty progressVisible = new SimpleBooleanProperty(this, "progressVisible", true); //$NON-NLS-1$
    public final BooleanProperty progressVisibleProperty() { return progressVisible; }
    public final boolean getProgressVisible() { return progressVisible.get(); }
    public final void setProgressVisible(boolean progressVisible) { this.progressVisible.set(progressVisible); }

    // -- Text
    private final StringProperty text = new SimpleStringProperty(this, "text", "Please Wait..."); //$NON-NLS-1$
    public final StringProperty textProperty() { return text; }
    public final String getText() { return text.get(); }
    public final void setText(String text) { this.text.set(text); }



    /**************************************************************************
     *
     * Interface implementation
     *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() { return new MaskerPaneSkin(this); }

    /** {@inheritDoc} */
    @Override  public String getUserAgentStylesheet() { return getUserAgentStylesheet(MaskerPane.class, "maskerpane.css"); } //$NON-NLS-1$
}