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
package impl.org.controlsfx.tableview2.filter.popupfilter;

import impl.org.controlsfx.tableview2.filter.parser.aggregate.AggregatorsParser;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.filter.popupfilter.PopupFilter;
import org.controlsfx.control.tableview2.filter.popupfilter.PopupStringFilter;
import org.controlsfx.control.textfield.TextFields;

import java.util.Collection;
import java.util.List;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;
import static java.util.stream.Collectors.toList;

public class PopupFilterSkin<S, T> implements Skin<PopupFilter<S, T>> {

    private static final PseudoClass PSEUDO_CLASS_ERROR = PseudoClass.getPseudoClass("error");

    private final PopupFilter<S, T> popupFilter;
    private final TextField filterTextField;
    private final VBox container;
    private final HBox filterContainer;

    public PopupFilterSkin(PopupFilter<S, T> popupFilter) {
        this.popupFilter = popupFilter;
        filterTextField = TextFields.createClearableTextField();
        filterTextField.textProperty().bindBidirectional(popupFilter.textProperty());
        // TODO: Fixed in JDK 9 - https://bugs.openjdk.java.net/browse/JDK-8090230
        filterTextField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                popupFilter.hide();
                e.consume();
            }
        });
        updateFilterTextField();

        FlowPane availableFilters = new FlowPane(5, 5);
        availableFilters.getChildren().addAll(getOperations(popupFilter.getOperations()));

        container = new VBox();
        filterContainer = new HBox(filterTextField);
        HBox.setHgrow(filterTextField, Priority.ALWAYS);
        Bindings.bindContent(container.getStyleClass(), popupFilter.getStyleClass());
        container.getStylesheets().add(
                PopupFilter.class.getResource("/org/controlsfx/control/tableview2/popupfilter.css").toExternalForm());
        container.getChildren().addAll(filterContainer, availableFilters);

        addRemoveCaseButton();
        initializeListeners();
    }

    private void initializeListeners() {
        filterTextField.textProperty().addListener((o, ov, nv) -> {
            updateFilterTextField();
        });
    }

    private void updateFilterTextField() {
        String text = filterTextField.getText();
        if (text == null || text.isEmpty()) {
            filterTextField.setTooltip(null);
            filterTextField.pseudoClassStateChanged(PSEUDO_CLASS_ERROR, false);
        } else {
            if (popupFilter.getParser().isValid(text)) {
                filterTextField.setTooltip(null);
                filterTextField.pseudoClassStateChanged(PSEUDO_CLASS_ERROR,false);
            } else {
                String validateMessage = popupFilter.getParser().getErrorMessage();
                filterTextField.setTooltip(new Tooltip(validateMessage));
                filterTextField.pseudoClassStateChanged(PSEUDO_CLASS_ERROR,true);
            }
        }
    }

    private void addRemoveCaseButton() {
        if (popupFilter instanceof PopupStringFilter) {
            filterContainer.getChildren().add(createCaseButton());
        }
    }

    private ToggleButton createCaseButton() {
        ToggleButton toggleButton = new ToggleButton("Aa");
        toggleButton.setToggleGroup(new ToggleGroup());
        Tooltip enableCaseSensitive = new Tooltip(localize(asKey("popup.filter.case.sensitive.enable")));
        Tooltip disableCaseSensitive = new Tooltip(localize(asKey("popup.filter.case.sensitive.disable")));
        toggleButton.setTooltip(disableCaseSensitive);
        ((PopupStringFilter)popupFilter).caseSensitiveProperty().bind(toggleButton.selectedProperty().not());
        toggleButton.selectedProperty().addListener((o, ov, nv) -> {
            updatePredicate();
            updateFilterTextField();
            if (nv) {
                toggleButton.setTooltip(enableCaseSensitive);
            } else {
                toggleButton.setTooltip(disableCaseSensitive);
            }
        });
        return toggleButton;
    }

    private void updatePredicate() {
        FilteredTableColumn<S, T> tableColumn = popupFilter.getTableColumn();
        String text = filterTextField.getText();
        if (text == null || text.isEmpty()) {
            tableColumn.setPredicate(null);
        } else {
            tableColumn.setPredicate(getSkinnable().getParser().parse(text));
        }
    }

    /** {@inheritDoc} */
    @Override public PopupFilter<S, T> getSkinnable() {
        return popupFilter;
    }

    /** {@inheritDoc} */
    @Override public Node getNode() {
        return container;
    }

    /** {@inheritDoc} */
    @Override public void dispose() {

    }

    private Collection<? extends Node> getOperations(List<String> operations) {
        return operations.stream().map(this::createLeafNode).collect(toList());
    }

    private Label createLeafNode(String text) {
        Label label = new Label(text);
        label.setOnMousePressed(e -> {
            filterTextField.insertText(filterTextField.getCaretPosition(), text);
            if (AggregatorsParser.getStrings().noneMatch(s -> s.equals(text))) {
                positionCaret();
            }
        });
        label.getStyleClass().add("operation");
        return label;
    }

    private void positionCaret() {
        if (getSkinnable() instanceof PopupStringFilter) {
            filterTextField.insertText(filterTextField.getCaretPosition(), " \"\"");
            filterTextField.positionCaret(filterTextField.getCaretPosition() - 1);
        }
    }
}
