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

package org.controlsfx.control.action;

import javafx.scene.control.Button;
import org.controlsfx.control.spreadsheet.JavaFXThreadingRule;
import org.junit.Rule;
import org.junit.Test;
import de.sandec.jmemorybuddy.JMemoryBuddy;

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

        JMemoryBuddy.memoryTest((checker) -> {
            Action action = new Action("action");

            ActionUtils.configureButton(action,button);
            ActionUtils.unconfigureButton(button);

            checker.assertCollectable(action);
        });
    }

    @Test
    public void testButtonIsCollectable() {
        Action action = new Action("button");

        JMemoryBuddy.memoryTest((checker) -> {
            Button button = new Button("Ignore");

            ActionUtils.configureButton(action,button);
            ActionUtils.unconfigureButton(button);

            checker.assertCollectable(button);
        });
    }
}
