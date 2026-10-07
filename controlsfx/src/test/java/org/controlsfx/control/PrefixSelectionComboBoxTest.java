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

import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import static org.junit.Assert.assertSame;

public class PrefixSelectionComboBoxTest {

    private PrefixSelectionComboBox<Item> comboBox;
    private final Item first = new Item("Alpha", "a");
    private final Item second = new Item("Beta", "ab");

    @BeforeClass
    public static void setupToolkit() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @AfterClass
    public static void cleanupToolkit() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Before
    public void setup() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            comboBox = new PrefixSelectionComboBox<>();
            comboBox.getItems().addAll(first, second);
            comboBox.setSkin(new ComboBoxListViewSkin<>(comboBox));
            // Keep the prefix intact while sending consecutive key events.
            comboBox.setTypingDelay(60000);
        });
    }

    @Test
    public void customLookupSelectsTypedItemsUsingTheAccumulatedPrefix() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            BiFunction<ComboBox<Item>, String, Optional<Item>> lookup = this::lookupByCode;
            comboBox.setLookup(lookup);

            pressKey(KeyCode.A, "a");
            assertSame(first, comboBox.getValue());

            pressKey(KeyCode.B, "b");
            assertSame(second, comboBox.getValue());
        });
    }

    @Test
    public void defaultLookupUsesTheConverterToMatchTypedItems() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            comboBox.getItems().add(0, null);
            comboBox.setConverter(new StringConverter<Item>() {
                @Override
                public String toString(Item item) {
                    return item == null ? "" : item.name;
                }

                @Override
                public Item fromString(String value) {
                    throw new UnsupportedOperationException();
                }
            });

            pressKey(KeyCode.B, "b");
            assertSame(second, comboBox.getValue());
        });
    }

    @Test
    public void customLookupWithNoMatchKeepsTheCurrentSelection() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            comboBox.setLookup(this::lookupByCode);
            comboBox.setValue(second);

            pressKey(KeyCode.Z, "z");
            assertSame(second, comboBox.getValue());
        });
    }

    private Optional<Item> lookupByCode(ComboBox<Item> control, String prefix) {
        return control.getItems().stream()
                .filter(item -> item.code.equals(prefix))
                .findFirst();
    }

    private void pressKey(KeyCode code, String text) {
        comboBox.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", text, code,
                false, false, false, false));
    }

    private static class Item {
        private final String name;
        private final String code;

        private Item(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }
}
