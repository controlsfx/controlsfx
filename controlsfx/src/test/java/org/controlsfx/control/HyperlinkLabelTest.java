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

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class HyperlinkLabelTest {
    private HyperlinkLabel label;
    private StackPane root;
    private Hyperlink first;
    private Hyperlink second;

    @BeforeClass
    public static void setupToolkit() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @AfterClass
    public static void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Before
    public void setup() throws TimeoutException {
        FxToolkit.setupStage(stage -> {
            label = new HyperlinkLabel("[First] and [Second]");
            root = new StackPane(label);
            stage.setScene(new Scene(root, 300, 100));
            stage.show();
            root.applyCss();
            first = (Hyperlink) label.lookupAll(".hyperlink").stream()
                    .filter(node -> ((Hyperlink) node).getText().equals("First"))
                    .findFirst().get();
            second = (Hyperlink) label.lookupAll(".hyperlink").stream()
                    .filter(node -> ((Hyperlink) node).getText().equals("Second"))
                    .findFirst().get();
        });
    }

    @Test
    public void eachLinkCallsHandlerOnceAndBubbles() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            List<Object> sources = new ArrayList<>();
            List<Object> targets = new ArrayList<>();
            List<ActionEvent> bubbled = new ArrayList<>();
            label.setOnAction(event -> {
                sources.add(event.getSource());
                targets.add(event.getTarget());
            });
            root.addEventHandler(ActionEvent.ACTION, bubbled::add);

            first.fire();
            second.fire();

            assertEquals(List.of(first, second), sources);
            assertEquals(List.of(first, second), targets);
            assertEquals(2, bubbled.size());
        });
    }

    @Test
    public void consumedCallbackStopsBubbling() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            List<ActionEvent> actions = new ArrayList<>();
            List<ActionEvent> bubbled = new ArrayList<>();
            label.setOnAction(event -> {
                actions.add(event);
                event.consume();
            });
            root.addEventHandler(ActionEvent.ACTION, bubbled::add);

            first.fire();

            assertEquals(1, actions.size());
            assertSame(first, actions.get(0).getSource());
            assertTrue(bubbled.isEmpty());
        });
    }

    @Test
    public void actionBubblesWithoutCallback() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            List<ActionEvent> bubbled = new ArrayList<>();
            root.addEventHandler(ActionEvent.ACTION, bubbled::add);
            first.fire();
            assertEquals(1, bubbled.size());
            assertSame(first, bubbled.get(0).getTarget());
        });
    }

    @Test
    public void replacingAndClearingCallbackUpdatesDispatch() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            List<ActionEvent> original = new ArrayList<>();
            List<ActionEvent> replacement = new ArrayList<>();
            List<ActionEvent> bubbled = new ArrayList<>();
            root.addEventHandler(ActionEvent.ACTION, bubbled::add);
            label.setOnAction(original::add);
            label.setOnAction(replacement::add);
            first.fire();
            label.setOnAction(null);
            second.fire();

            assertTrue(original.isEmpty());
            assertEquals(1, replacement.size());
            assertSame(first, replacement.get(0).getSource());
            assertEquals(2, bubbled.size());
        });
    }

    @Test
    public void mouseAndKeyboardActivationEachCallHandlerOnce() throws TimeoutException {
        List<Object> sources = new ArrayList<>();
        FxToolkit.setupFixture(() -> label.setOnAction(event -> sources.add(event.getSource())));
        FxRobot robot = new FxRobot();
        robot.clickOn(first);
        FxToolkit.setupFixture(() -> {
            assertEquals(List.of(first), sources);
            second.requestFocus();
        });
        robot.type(KeyCode.SPACE);
        FxToolkit.setupFixture(() -> assertEquals(List.of(first, second), sources));
    }
}
