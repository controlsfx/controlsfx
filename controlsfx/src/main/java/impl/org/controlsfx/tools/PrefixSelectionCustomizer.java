/**
 * Copyright (c) 2015, ControlsFX
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
package impl.org.controlsfx.tools;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.controlsfx.control.PrefixSelectionChoiceBox;
import org.controlsfx.control.PrefixSelectionComboBox;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

/**
 * This utility class can be used to customize a {@link ChoiceBox} or {@link ComboBox}
 * and enable the "prefix selection" feature. This will enable the user to type letters or 
 * digits on the keyboard and die {@link ChoiceBox} or {@link ComboBox} will attempt to
 * select the first item it can find with a matching prefix.
 * 
 * You can also use the preconfigured classes {@link PrefixSelectionChoiceBox} and 
 * {@link PrefixSelectionComboBox}.
 */
public class PrefixSelectionCustomizer {
    private static final String SELECTION_PREFIX_STRING = "selectionPrefixString";
    private static final Object SELECTION_PREFIX_TASK = "selectionPrefixTask";

    private static EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>() {
        private ScheduledExecutorService executorService = null;

        @Override
        public void handle(KeyEvent event) {
            keyPressed(event);
        }

        private <T> void keyPressed(KeyEvent event) {
            KeyCode code = event.getCode();
            if (code.isLetterKey() || code.isDigitKey()) {
                String letter = code.impl_getChar();
                if (event.getSource() instanceof ComboBox) {
                    ComboBox<T> comboBox = (ComboBox<T>) event.getSource();
                    T item = getEntryWithKey(letter, comboBox.getConverter(), comboBox.getItems(), comboBox);
                    if (item != null) {
                        comboBox.setValue(item);
                    }
                } else if (event.getSource() instanceof ChoiceBox) {
                    ChoiceBox<T> choiceBox = (ChoiceBox<T>) event.getSource();
                    T item = getEntryWithKey(letter, choiceBox.getConverter(), choiceBox.getItems(), choiceBox);
                    if (item != null) {
                        choiceBox.setValue(item);
                    }
                }
            }
        }

        private <T> T getEntryWithKey(String letter, StringConverter<T> converter, ObservableList<T> items, Control control) {
            T result = null;

            if (converter == null) {
                converter = new StringConverter<T>() {
                    @Override
                    public String toString(T t) {
                        return t == null ? null : t.toString();
                    }

                    @Override
                    public T fromString(String string) {
                        return null;
                    }
                };
            }

            String selectionPrefixString = (String) control.getProperties().get(SELECTION_PREFIX_STRING);
            if (selectionPrefixString == null) {
                selectionPrefixString = letter.toUpperCase();
            } else {
                selectionPrefixString += letter.toUpperCase();
            }
            control.getProperties().put(SELECTION_PREFIX_STRING, selectionPrefixString);

            for (T item : items) {
                String string = converter.toString(item);
                if (string != null && string.toUpperCase().startsWith(selectionPrefixString)) {
                    result = item;
                    break;
                }
            }

            ScheduledFuture<?> task = (ScheduledFuture<?>) control.getProperties().get(SELECTION_PREFIX_TASK);
            if (task != null) {
                task.cancel(false);
            }
            task = getExecutorService().schedule(
                    () -> control.getProperties().put(SELECTION_PREFIX_STRING, ""), 500, TimeUnit.MILLISECONDS);
            control.getProperties().put(SELECTION_PREFIX_TASK, task);

            return result;
        }

        private ScheduledExecutorService getExecutorService() {
            if (executorService == null) {
                executorService = Executors.newScheduledThreadPool(1,
                        runnabble -> {
                            Thread result = new Thread(runnabble);
                            result.setDaemon(true);
                            return result;
                        });
            }
            return executorService;
        }

    };

    public static void customize(ComboBox<?> comboBox) {
        if (!comboBox.isEditable()) {
            comboBox.addEventHandler(KeyEvent.KEY_PRESSED, handler);
        }
        comboBox.editableProperty().addListener((o, oV, nV) -> {
            if (!nV) {
                comboBox.addEventHandler(KeyEvent.KEY_PRESSED, handler);
            } else {
                comboBox.removeEventHandler(KeyEvent.KEY_PRESSED, handler);
            }
        });
    }

    public static void customize(ChoiceBox<?> choiceBox) {
        choiceBox.addEventHandler(KeyEvent.KEY_PRESSED, handler);
    }

}
