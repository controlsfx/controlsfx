/**
 * Copyright (c) 2019, ControlsFX
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
package org.controlsfx.control.textfield;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.api.FxToolkit.setupStage;

public class CustomTextFieldTest extends FxRobot {

    private StackPane sceneRoot;

    @BeforeClass
    public static void setupSpec() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @Before
    public void setup() throws Exception {
        setupStage(localStage -> {
            sceneRoot = new StackPane();
            localStage.setScene(new Scene(sceneRoot, 1000, 800));
            localStage.setX(0);
            localStage.setY(0);
            localStage.show();
        });
    }

    @After
    public void after() throws TimeoutException {
        FxToolkit.cleanupStages();
        FxToolkit.hideStage();
    }

    @Test
    public void setNullTest_1178() {
        CustomTextField textField = new CustomTextField();
        interact(() -> sceneRoot.getChildren().add(textField));

        Label rightLabel = new Label("right");
        rightLabel.setId("right-label");
        Label leftLabel = new Label("left");
        leftLabel.setId("left-label");

        Assert.assertNull(textField.getRight());
        Assert.assertNull(textField.getLeft());

        interact(() -> {
            textField.setRight(rightLabel);
            textField.setLeft(leftLabel);
        });

        Assert.assertNotNull(textField.getRight());
        Assert.assertNotNull(textField.getLeft());

        interact(() -> {
            textField.setRight(null);
            textField.setLeft(null);
        });

        Assert.assertNull(textField.getRight());
        Assert.assertNull(textField.getLeft());

        Assert.assertEquals(Optional.empty(), lookup("#right-label").tryQuery());
        Assert.assertEquals(Optional.empty(), lookup("#left-label").tryQuery());
    }
}
