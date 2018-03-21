/**
 * Copyright (c) 2015, 2016 ControlsFX
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

import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.PrefixSelectionChoiceBox;
import org.controlsfx.control.PrefixSelectionComboBox;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * <p>This utility class can be used to customize a {@link ChoiceBox} or
 * {@link ComboBox} and enable the "prefix selection" feature. This will enable
 * the user to type letters or digits on the keyboard while the {@link ChoiceBox}
 * or {@link ComboBox} has the focus. 
 * 
 * By default, the {@link ChoiceBox} or {@link ComboBox} 
 * will attempt to select the first item it can find with a matching prefix ignoring 
 * case.
 * 
 * <p>This feature is available natively on the Windows combo box control, so many
 * users have asked for it. There is a feature request to include this feature
 * into JavaFX (<a href="https://javafx-jira.kenai.com/browse/RT-18064">Issue RT-18064</a>). 
 * The class is published as part of ContorlsFX to allow testing and feedback.
 * 
 * <h3>Example</h3>
 *  
 * <p>Let's look at an example to clarify this. The combo box offers the items 
 * ["Aaaaa", "Abbbb", "Abccc", "Abcdd", "Abcde"]. The user now types "abc" in 
 * quick succession (and then stops typing). The combo box will select a new entry 
 * on every key pressed. The first entry it will select is "Aaaaa" since it is the 
 * first entry that starts with an "a" (case ignored). It will then select "Abbbb", 
 * since this is the first entry that started with "ab" and will finally settle for 
 * "Abccc".
 * 
 * <ul><table>
 *   <tr><th>Keys typed</th><th>Element selected</th></tr>
 *   <tr><td>a</td><td>Aaaaa<td></tr>
 *   <tr><td>aaa</td><td>Aaaaa<td></tr>
 *   <tr><td>ab</td><td>Abbbb<td></tr>
 *   <tr><td>abc</td><td>Abccc<td></tr>
 *   <tr><td>xyz</td><td>-<td></tr>
 * </table></ul>
 * 
 * <h3>Usage</h3>
 * 
 * <p>A common use case is to customize a {@link ChoiceBox} or {@link ComboBox}
 * that has been loaded as part of an FXML. In this case you can use the utility
 * methods {@link #customize(ChoiceBox)} or {@link #customize(ComboBox)}. This
 * will install a {@link EventHandler} that monitors the {@link KeyEvent}
 * events.
 * 
 * <p>If you are coding, you can also use the preconfigured classes
 * {@link PrefixSelectionChoiceBox} and {@link PrefixSelectionComboBox} as a
 * substitute for {@link ChoiceBox} and {@link ComboBox}.
 * 
 * In this case, {@link PrefixSelectionComboBox} includes some properties that will
 * allow modifying the elapsed time before the selection is clear, listen to back
 * space to clear the selection, show the popup when the control gains the focus, 
 * or set a different lookup criteria.
 * 
 * @see PrefixSelectionChoiceBox
 * @see PrefixSelectionComboBox
 */
public class PrefixSelectionCustomizer {
    
    public static final int DEFAULT_TYPING_DELAY = 500;
    
    private static final String SELECTION_PREFIX_STRING = "selectionPrefixString";
    private static final Object SELECTION_PREFIX_TASK = "selectionPrefixTask";

    public static final BiFunction<ComboBox, String, Optional> DEFAULT_LOOKUP_COMBOBOX = 
            (comboBox, selection) -> {
                if (comboBox == null || selection == null || selection.isEmpty()) {
                    return Optional.empty();
                }
                
                return comboBox.getItems().stream()
                        .filter(Objects::nonNull)
                        .filter(item -> {
                            String s = comboBox.getConverter() == null ? item.toString() : comboBox.getConverter().toString(item);
                            if (s != null && ! s.isEmpty()) {
                                return s.toUpperCase(Locale.ROOT).startsWith(selection.toUpperCase(Locale.ROOT));
                            }
                            return false;
                        })
                        .findFirst();
    };
    
    public static final BiFunction<ChoiceBox, String, Optional> DEFAULT_LOOKUP_CHOICEBOX = 
            (choiceBox, selection) -> {
                if (choiceBox == null || selection == null || selection.isEmpty()) {
                    return Optional.empty();
                }
                
                return choiceBox.getItems().stream()
                        .filter(Objects::nonNull)
                        .filter(item -> {
                            String s = choiceBox.getConverter() == null ? item.toString() : choiceBox.getConverter().toString(item);
                            if (s != null && ! s.isEmpty()) {
                                return s.toUpperCase(Locale.ROOT).startsWith(selection.toUpperCase(Locale.ROOT));
                            }
                            return false;
                        })
                        .findFirst();
    };

    private static EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>() {
        private ScheduledExecutorService executorService = null;
        private PrefixSelectionComboBox prefixSelectionComboBox;
        private int typingDelay;
        private Object result;
        
        @Override
        public void handle(KeyEvent event) {
            keyPressed(event);
        }

        private <T> void keyPressed(KeyEvent event) {
            KeyCode code = event.getCode();
            if (code.isLetterKey() || code.isDigitKey() || code == KeyCode.SPACE || code == KeyCode.BACK_SPACE) {
                if (event.getSource() instanceof PrefixSelectionComboBox) {
                    if (code == KeyCode.BACK_SPACE && ! ((PrefixSelectionComboBox) event.getSource()).isBackSpaceAllowed()) {
                        return;
                    }
                }
                String letter = event.getText();
                if (event.getSource() instanceof ComboBox) {
                    ComboBox<T> comboBox = (ComboBox<T>) event.getSource();
                    T item = getEntryWithKey(letter, comboBox);
                    if (item != null) {
                        comboBox.setValue(item);
                        // scroll to selection
                        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) comboBox.getSkin(); 
                        ((ListView<T>) skin.getPopupContent()).scrollTo(item); 
                    }
                } else if (event.getSource() instanceof ChoiceBox) {
                    ChoiceBox<T> choiceBox = (ChoiceBox<T>) event.getSource();
                    T item = getEntryWithKey(letter, choiceBox);
                    if (item != null) {
                        choiceBox.setValue(item);
                    }
                }
            }
        }

        private <T> T getEntryWithKey(String letter, Control control) {
            result = null;
            typingDelay = DEFAULT_TYPING_DELAY;
            prefixSelectionComboBox = (control instanceof PrefixSelectionComboBox) ? (PrefixSelectionComboBox) control : null;
            
            String selectionPrefixString = processInput((String) control.getProperties().get(SELECTION_PREFIX_STRING), letter);
            control.getProperties().put(SELECTION_PREFIX_STRING, selectionPrefixString);

            if (prefixSelectionComboBox != null) {
                typingDelay = prefixSelectionComboBox.getTypingDelay();
                final BiFunction<ComboBox, String, Optional> lookup = prefixSelectionComboBox.getLookup();
                if (lookup != null) {
                    lookup.apply(prefixSelectionComboBox, selectionPrefixString).ifPresent(t -> result = t);
                }
            } else if (control instanceof ComboBox) {
                DEFAULT_LOOKUP_COMBOBOX.apply((ComboBox) control, selectionPrefixString).ifPresent(t -> result = t);
            } else if (control instanceof ChoiceBox) {
                DEFAULT_LOOKUP_CHOICEBOX.apply((ChoiceBox) control, selectionPrefixString).ifPresent(t -> result = t);
            }
            
            ScheduledFuture<?> task = (ScheduledFuture<?>) control.getProperties().get(SELECTION_PREFIX_TASK);
            if (task != null) {
                task.cancel(false);
            }
            task = getExecutorService().schedule(
                    () -> control.getProperties().put(SELECTION_PREFIX_STRING, ""), typingDelay, TimeUnit.MILLISECONDS); 
            control.getProperties().put(SELECTION_PREFIX_TASK, task);

            return (T) result;
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
        
        private String processInput(String initialText, String letter) {
            if (initialText == null) {
                initialText = "";
            }

            StringBuilder sb = new StringBuilder();
            for (char c : initialText.concat(letter).toCharArray()) {
                if (c == '\b') { // back space, remove all
                    if (sb.length() > 0) {
                        sb.delete(0, sb.length());
                        break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

    };

    /**
     * This will install an {@link EventHandler} that monitors the
     * {@link KeyEvent} events to enable the "prefix selection" feature.
     * The {@link EventHandler} will only be installed if the {@link ComboBox}
     * is <b>not</b> editable.
     * 
     * @param comboBox
     *            The {@link ComboBox} that should be customized
     * 
     * @see PrefixSelectionCustomizer
     */
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

    /**
     * This will install an {@link EventHandler} that monitors the
     * {@link KeyEvent} events to enable the "prefix selection" feature.
     * 
     * @param choiceBox
     *            The {@link ChoiceBox} that should be customized
     * 
     * @see PrefixSelectionCustomizer
     */
    public static void customize(ChoiceBox<?> choiceBox) {
        choiceBox.addEventHandler(KeyEvent.KEY_PRESSED, handler);
    }

}
