/**
 * Copyright (c) 2026, ControlsFX
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests that what a {@link CheckComboBox} renders in its button is the checks held by the check
 * model the control has.
 *
 * <p>The button of the control is a cell of its own, whose text is built from
 * {@link IndexedCheckModel#getCheckedItems()} and refreshed from a listener of
 * {@link IndexedCheckModel#getCheckedIndices()}: both are read from the model, and the control
 * lets an application replace that model through {@link CheckComboBox#setCheckModel(IndexedCheckModel)}.
 *
 * <p>The control is therefore shown in a scene of its own, and every assertion goes all the way
 * down to the text the button renders.
 */
public class CheckComboBoxTest extends FxRobot {

    private static final String ITEM_1 = "Item 1";
    private static final String ITEM_2 = "Item 2";
    private static final String ITEM_3 = "Item 3";

    private ObservableList<String> items;
    private CheckComboBox<String> checkComboBox;

    private static final int SCENE_WIDTH = 300;
    private static final int SCENE_HEIGHT = 200;
    private static final long LOOKUP_TIMEOUT_MILLIS = 5000;

    @BeforeClass
    public static void setupSpec() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @After
    public void afterEach() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    public void testTheButtonRendersTheItemCheckedThroughTheModelOfTheControl() {
        givenCheckComboBox(ITEM_1, ITEM_2, ITEM_3);

        interact(() -> checkComboBox.getCheckModel().check(ITEM_2));

        assertEquals("The button does not render the checked item", ITEM_2, renderedButtonText());
    }

    @Test
    public void testTheButtonRendersTheItemCheckedThroughTheModelTheControlIsGiven() {
        // setCheckModel(IndexedCheckModel) is public API, and the model the control is given is
        // the model it has: the checks made through it are the ones the button renders, just as
        // the checks of the model the control built for itself are.
        // The replacement is built on a map of its own, as the one the control keeps for its own
        // model is private to it - which leaves the check boxes of the popup out of this scenario,
        // the button text being read from the checked items of the model alone.
        givenCheckComboBox(ITEM_1, ITEM_2, ITEM_3);

        interact(() -> checkComboBox.setCheckModel(
            new CheckComboBox.CheckComboBoxBitSetCheckModel<>(checkComboBox.getItems(), new HashMap<>())));
        interact(() -> checkComboBox.getCheckModel().check(ITEM_2));

        assertEquals("The button does not render the checks of the model the control has",
            ITEM_2, renderedButtonText());
    }

    /**
     * Shows a {@link CheckComboBox} holding the given items. The control is put in a scene of its
     * own, and that scene shown, so that its button is really rendered.
     */
    private void givenCheckComboBox(String... itemValues) {
        items = FXCollections.observableArrayList(itemValues);
        try {
            FxToolkit.setupStage(stage -> {
                checkComboBox = new CheckComboBox<>(items);
                stage.setScene(new Scene(checkComboBox, SCENE_WIDTH, SCENE_HEIGHT));
                stage.show();
                stage.toFront();
            });
        } catch (TimeoutException e) {
            throw new AssertionError("The CheckComboBox could not be shown", e);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Returns the text rendered by the button of the control - what the user actually reads of the
     * checks that were made.
     */
    private String renderedButtonText() {
        // let the button the checks reached be laid out again before reading what it renders
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<?> renderedComboBox = WaitForAsyncUtils.waitForAsyncFx(LOOKUP_TIMEOUT_MILLIS, () -> {
            for (Node node : checkComboBox.getChildrenUnmodifiable()) {
                if (node instanceof ComboBox) {
                    return (ComboBox<?>) node;
                }
            }
            return null;
        });
        assertNotNull("Expected the CheckComboBox to be rendered by a ComboBox of its own",
            renderedComboBox);
        return WaitForAsyncUtils.waitForAsyncFx(LOOKUP_TIMEOUT_MILLIS,
            () -> renderedComboBox.getButtonCell().getText());
    }
}
