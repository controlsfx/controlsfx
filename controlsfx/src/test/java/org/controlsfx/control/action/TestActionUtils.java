package org.controlsfx.control.action;

import javafx.scene.control.Button;
import org.controlsfx.control.spreadsheet.JavaFXThreadingRule;
import org.junit.Rule;
import org.junit.Test;

public class TestActionUtils {
    @Rule
    public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

    @Test
    public void testConfiguringButtonTwice() {
        Button button = new Button("button");
        Action action = new Action("action");

        ActionUtils.configureButton(action,button);
        ActionUtils.unconfigureButton(button);
        ActionUtils.configureButton(action,button);
        ActionUtils.unconfigureButton(button);
    }

    @Test
    public void testActionIsCollectable() {
        Button button = new Button("button");

        MemoryLeakUtils.doMemTest((checker) -> {
            Action action = new Action("action");

            ActionUtils.configureButton(action,button);
            ActionUtils.unconfigureButton(button);

            checker.accept(action);
        });
    }

    @Test
    public void testButtonIsCollectable() {
        Action action = new Action("button");

        MemoryLeakUtils.doMemTest((checker) -> {
            Button button = new Button("Ignore");

            ActionUtils.configureButton(action,button);
            ActionUtils.unconfigureButton(button);

            checker.accept(button);
        });
    }
}
