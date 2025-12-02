/**
 * Copyright (c) 2021, ControlsFX
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
package org.controlsfx.property.editor;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.api.FxToolkit.setupStage;

/**
 * Test fix for reported issue https://github.com/controlsfx/controlsfx/issues/1275
 */
public class NumericPropertyZeroTest extends FxRobot {

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
  public void setToZeroTest_1275() throws Exception {
    var bean = new Bean();
    bean.setNumber(10);
    var items = BeanPropertyUtils.getProperties(bean);
    var sheet = new PropertySheet(items);

    interact(() -> sceneRoot.getChildren().add(sheet));

    Optional<NumericField> nf = findPropertyTextField(sheet);

    Assert.assertTrue(nf.isPresent());

    Assert.assertEquals("Property text field contains incorrect initial string", "10", nf.get().getText());
    Assert.assertEquals("Property bean contains incorrect initial value", 10, bean.getNumber());

    clickOn(nf.get());
    type(KeyCode.DIGIT0);

    type(KeyCode.ENTER);
    type(KeyCode.TAB);

    Assert.assertEquals("Property text field contains incorrect set string", "0", nf.get().getText());
    Assert.assertEquals("Property bean contains incorrect set value", 0, bean.getNumber());
  }

  public static class Bean {
    private int number;
    private String string;

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

    public String getString() {
      return string;
    }

    public void setString(String string) {
      this.string = string;
    }
  }

  private static Optional<NumericField> findPropertyTextField(Node n) {
    if (n instanceof NumericField) {
      return Optional.of((NumericField) n);
    }
    if (n instanceof Parent) {
      return ((Parent) n).getChildrenUnmodifiable().stream()
              .map(NumericPropertyZeroTest::findPropertyTextField)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .findFirst();
    }

    return Optional.empty();
  }
}
