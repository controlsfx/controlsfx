/**
 * Copyright (c) 2018 ControlsFX
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

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListActionView;
import org.controlsfx.control.action.ActionUtils;

import static java.util.stream.Collectors.toCollection;
import static javafx.scene.control.SelectionMode.MULTIPLE;

public class ListActionViewSkin<T> extends SkinBase<ListActionView<T>> {

    private static final PseudoClass PSEUDO_CLASS_TOP = PseudoClass.getPseudoClass("top");
    private static final PseudoClass PSEUDO_CLASS_RIGHT = PseudoClass.getPseudoClass("right");
    private static final PseudoClass PSEUDO_CLASS_BOTTOM = PseudoClass.getPseudoClass("bottom");
    private static final PseudoClass PSEUDO_CLASS_LEFT = PseudoClass.getPseudoClass("left");

    private BorderPane borderPane;
    private ListView<T> listView;

    public ListActionViewSkin(ListActionView<T> control) {
        super(control);

        borderPane = new BorderPane();

        this.listView = createListView();
        this.listView.cellFactoryProperty().bind(getSkinnable().cellFactoryProperty());
        Bindings.bindContent(this.listView.getItems(), getSkinnable().getItems());

        control.getActions().addListener((InvalidationListener)  o -> update());
        control.sideProperty().addListener(o -> update());

        update();
        getChildren().add(borderPane);
    }

    private void update() {
        borderPane.getChildren().clear();
        borderPane.setCenter(listView);
        clearPseudoStates();

        if (!getSkinnable().getActions().isEmpty()) {
            Side side = getSkinnable().getSide();
            switch (side) {
                case TOP:
                    borderPane.setTop(createHorizontalButtonBox());
                    pseudoClassStateChanged(PSEUDO_CLASS_TOP, true);
                    break;
                case RIGHT:
                    borderPane.setRight(createVerticalButtonBox());
                    pseudoClassStateChanged(PSEUDO_CLASS_RIGHT, true);
                    break;
                case BOTTOM:
                    borderPane.setBottom(createHorizontalButtonBox());
                    pseudoClassStateChanged(PSEUDO_CLASS_BOTTOM, true);
                    break;
                case LEFT:
                    borderPane.setLeft(createVerticalButtonBox());
                    pseudoClassStateChanged(PSEUDO_CLASS_LEFT, true);
                    break;
            }
        }
    }

    private VBox createVerticalButtonBox() {
        VBox box = new VBox();
        box.getStyleClass().add("buttons");
        box.setFillWidth(true);
        box.getChildren().addAll(createButtonsFromActions());
        return box;
    }

    private HBox createHorizontalButtonBox() {
        HBox box = new HBox();
        box.getStyleClass().add("buttons");
        box.setFillHeight(true);
        box.getChildren().addAll(createButtonsFromActions());
        return box;
    }

    private ObservableList<Node> createButtonsFromActions() {
        return getSkinnable().getActions().stream()
                .peek(listAction -> listAction.initialize(listView))
                .map(this::createActionButton)
                .collect(toCollection(FXCollections::observableArrayList));
    }

    private Button createActionButton(ListActionView.ListAction<T> action) {
        Button button = ActionUtils.createButton(action);
        button.setMaxWidth(Double.MAX_VALUE);
        if (action.getAccelerator() != null) {
            getSkinnable().getScene().getAccelerators().put(action.getAccelerator(), button::fire);
        }
        return button;
    }

    private void clearPseudoStates() {
        pseudoClassStateChanged(PSEUDO_CLASS_TOP, false);
        pseudoClassStateChanged(PSEUDO_CLASS_RIGHT, false);
        pseudoClassStateChanged(PSEUDO_CLASS_BOTTOM, false);
        pseudoClassStateChanged(PSEUDO_CLASS_LEFT, false);
    }

    /**
     * Creates the {@link ListView} instance to be used in the control.
     * This method can be overridden to provide a customized list
     * view control.
     *
     * @return the list view
     */
    protected ListView<T> createListView() {
        ListView<T> view = new ListView<>();
        view.getSelectionModel().setSelectionMode(MULTIPLE);
        return view;
    }
}
