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
