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

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class NotificationsTest extends FxRobot {

    private Stage stage;
    private Scene scene;

    @BeforeClass
    public static void setupSpec() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @Before
    public void setup() throws TimeoutException {
        FxToolkit.setupStage(localStage -> {
            stage = localStage;
            scene = new Scene(new StackPane(), 600, 400);
            stage.setScene(scene);
        });
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
        FxToolkit.hideStage();
    }

    @Test
    public void firstNotificationDoesNotChangePreparedStylesheets() {
        interact(() -> {
            Notifications.warmup(scene);
            stage.show();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });
        interact(() -> {
            AtomicInteger changes = new AtomicInteger();
            scene.getStylesheets().addListener((ListChangeListener<String>) change -> changes.incrementAndGet());

            Notifications.create().owner(stage).text("Ready").show();

            assertTrue(Window.getWindows().stream().anyMatch(window ->
                    window instanceof Popup && ((Popup) window).getOwnerWindow() == stage && window.isShowing()));
            assertEquals(0, changes.get());
        });
    }

    @Test
    public void warmupRegistersStylesheetOncePerSceneAndPreservesOrder() {
        interact(() -> {
            String custom = Notifications.class.getResource("notificationpane.css").toExternalForm();
            String other = Notifications.class.getResource("statusbar.css").toExternalForm();
            String stylesheet = Notifications.class.getResource("notificationpopup.css").toExternalForm();
            scene.getStylesheets().addAll(custom, other);
            ArrayList<Window> windows = new ArrayList<>(Window.getWindows());

            Notifications.warmup(scene);

            assertEquals(Arrays.asList(stylesheet, custom, other), scene.getStylesheets());
            assertEquals(windows, Window.getWindows());
            assertFalse(stage.isShowing());

            AtomicInteger changes = new AtomicInteger();
            scene.getStylesheets().addListener((ListChangeListener<String>) change -> changes.incrementAndGet());
            Notifications.warmup(scene);
            assertEquals(0, changes.get());

            scene.getStylesheets().setAll(custom, stylesheet, other);
            changes.set(0);
            Notifications.warmup(scene);
            assertEquals(0, changes.get());
            assertEquals(Arrays.asList(custom, stylesheet, other), scene.getStylesheets());

            scene.getStylesheets().remove(stylesheet);
            Notifications.warmup(scene);
            assertEquals(Arrays.asList(stylesheet, custom, other), scene.getStylesheets());

            Scene secondScene = new Scene(new StackPane());
            assertTrue(secondScene.getStylesheets().isEmpty());
            Notifications.warmup(secondScene);
            assertEquals(Collections.singletonList(stylesheet), secondScene.getStylesheets());
        });
    }

    @Test
    public void notificationWithPopupOwnerStillRegistersStylesheetLazily() {
        interact(() -> {
            stage.show();
            Popup owner = new Popup();
            owner.getContent().add(new Label("Owner"));
            owner.show(stage);
            AtomicInteger changes = new AtomicInteger();
            scene.getStylesheets().addListener((ListChangeListener<String>) change -> changes.incrementAndGet());

            Notifications.create().owner(owner).text("Ready").show();

            String stylesheet = Notifications.class.getResource("notificationpopup.css").toExternalForm();
            assertEquals(Collections.singletonList(stylesheet), scene.getStylesheets());
            assertFalse(owner.getScene().getStylesheets().contains(stylesheet));
            assertTrue(Window.getWindows().stream().anyMatch(window -> window instanceof Popup
                    && window != owner && ((Popup) window).getOwnerWindow() == stage && window.isShowing()));
            assertEquals(1, changes.get());

            Notifications.create().owner(owner).text("Again").show();
            assertEquals(1, changes.get());
        });
    }

    @Test
    public void warmupRejectsNullWithoutShowingPopup() {
        interact(() -> {
            ArrayList<Window> windows = new ArrayList<>(Window.getWindows());
            try {
                Notifications.warmup(null);
                fail("Expected NullPointerException");
            } catch (NullPointerException exception) {
                assertEquals("scene", exception.getMessage());
            }
            assertEquals(windows, Window.getWindows());
        });
    }
}
