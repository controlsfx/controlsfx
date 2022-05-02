/**
 * Copyright (c) 2013, 2022, ControlsFX
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
package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.controlsfx.control.HyperlinkLabel;

public class HyperlinkLabelSkin extends SkinBase<HyperlinkLabel> {

    /***************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/

    // The strings used to delimit the hyperlinks
    private static final String HYPERLINK_START = "["; //$NON-NLS-1$
    private static final String HYPERLINK_END = "]"; //$NON-NLS-1$

    /***************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/

    private final TextFlow textFlow;
    private final Hyperlink hyperlink;

    private final EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
        @Override public void handle(final ActionEvent event) {
            EventHandler<ActionEvent> onActionHandler = getSkinnable().getOnAction();
            if (onActionHandler != null) {
                onActionHandler.handle(event);
            }
        }
    };
    private final ChangeListener<Boolean> focusChangeListener;

    /***************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    public HyperlinkLabelSkin(HyperlinkLabel control) {
        super(control);

        textFlow = new TextFlow();
        hyperlink = new Hyperlink();
        hyperlink.setPadding(new Insets(0, 0, 0, 0));
        hyperlink.setOnAction(eventHandler);
        hyperlink.focusTraversableProperty().bind(control.focusTraversableProperty());
        focusChangeListener = (o, ov, nv) -> {
            if (nv) {
                hyperlink.requestFocus();
            }
        };
        getSkinnable().focusedProperty().addListener(focusChangeListener);

        getChildren().add(textFlow);
        updateText();

        registerChangeListener(control.textProperty(), e -> updateText()); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        hyperlink.focusTraversableProperty().unbind();
        getSkinnable().focusedProperty().removeListener(focusChangeListener);
        super.dispose();
    }

    /***************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/

    // splits up the string into Text and Hyperlink nodes, and places them
    // into a TextFlow instance
    private void updateText() {
        final String text = getSkinnable().getText();

        if (text == null || text.isEmpty()) {
            textFlow.getChildren().clear();
            return;
        }

        // parse the text and put it into an array list
        final List<Node> nodes = new ArrayList<>();

        int start = 0;
        final int textLength = text.length();
        while (start != -1 && start < textLength) {
            int startPos = text.indexOf(HYPERLINK_START, start);
            int endPos = text.indexOf(HYPERLINK_END, startPos);

            // if the startPos is -1, there are no more hyperlinks...
            if (startPos == -1 || endPos == -1) {
                if (textLength > start) {
                    // ...but there is still text to turn into one last label
                    Label label = new Label(text.substring(start));
                    nodes.add(label);
                    break;
                }
            }

            // firstly, create a label from start to startPos
            Text label = new Text(text.substring(start, startPos));
            nodes.add(label);

            // if endPos is greater than startPos, create a hyperlink
            hyperlink.setText(text.substring(startPos + 1, endPos));
            nodes.add(hyperlink);

            start = endPos + 1;
        }
        textFlow.getChildren().setAll(nodes);
    }
}
