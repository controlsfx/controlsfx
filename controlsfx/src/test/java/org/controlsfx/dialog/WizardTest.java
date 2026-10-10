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
package org.controlsfx.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class WizardTest {
    private Wizard wizard;

    @BeforeClass
    public static void setupToolkit() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            if (wizard != null) wizard.getDialog().close();
        });
        FxToolkit.cleanupStages();
    }

    @Test
    public void identicalPagesKeepWindowSizeInBothDirections() throws TimeoutException {
        show(false);
        double[] initial = bounds();
        navigate(ButtonData.NEXT_FORWARD);
        assertSize(initial);
        navigate(ButtonData.NEXT_FORWARD);
        assertSize(initial);
        navigate(ButtonData.BACK_PREVIOUS);
        assertSize(initial);
        navigate(ButtonData.BACK_PREVIOUS);
        assertSize(initial);
    }

    @Test
    public void largerPageResizesWindowAndKeepsItsCenter() throws TimeoutException {
        show(true);
        double[] initial = bounds();
        navigate(ButtonData.NEXT_FORWARD);
        double[] expanded = bounds();
        assertTrue(expanded[2] > initial[2]);
        assertTrue(expanded[3] > initial[3]);
        assertEquals(initial[0] + initial[2] / 2, expanded[0] + expanded[2] / 2, 1.0);
        assertEquals(initial[1] + initial[3] / 2, expanded[1] + expanded[3] / 2, 1.0);
        navigate(ButtonData.NEXT_FORWARD);
        assertSize(initial);
    }

    private void show(boolean largerSecondPage) throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            WizardPane second = new WizardPane();
            if (largerSecondPage) {
                Region content = new Region();
                content.setPrefSize(800, 300);
                second.setContent(content);
            }
            wizard = new Wizard();
            wizard.setFlow(new Wizard.LinearFlow(new WizardPane(), second, new WizardPane()));
            wizard.getDialog().show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void navigate(ButtonData direction) throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            DialogPane pane = wizard.getDialog().getDialogPane();
            ButtonType type = pane.getButtonTypes().stream()
                    .filter(button -> button.getButtonData() == direction)
                    .findFirst().get();
            ((Button) pane.lookupButton(type)).fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private double[] bounds() throws TimeoutException {
        double[] bounds = new double[4];
        FxToolkit.setupFixture(() -> {
            Dialog<ButtonType> dialog = wizard.getDialog();
            bounds[0] = dialog.getX();
            bounds[1] = dialog.getY();
            bounds[2] = dialog.getWidth();
            bounds[3] = dialog.getHeight();
        });
        return bounds;
    }

    private void assertSize(double[] expected) throws TimeoutException {
        double[] actual = bounds();
        assertEquals(expected[2], actual[2], 0.01);
        assertEquals(expected[3], actual[3], 0.01);
    }
}
