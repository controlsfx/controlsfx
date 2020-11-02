/**
 * Copyright (c) 2014, 2020 ControlsFX
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

import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.SelectionMode;
import org.junit.*;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class CheckTreeViewTest extends FxRobot {
    
    private CheckTreeView<String> checkTreeView;
    
    private final CheckBoxTreeItem<String> treeItem_Jonathan = new CheckBoxTreeItem<>("Jonathan");
    private final CheckBoxTreeItem<String> treeItem_Eugene = new CheckBoxTreeItem<>("Eugene");
    private final CheckBoxTreeItem<String> treeItem_Henry = new CheckBoxTreeItem<>("Henry");
    private final CheckBoxTreeItem<String> treeItem_Samir = new CheckBoxTreeItem<>("Samir");

    @BeforeClass
    public static void setupSpec() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @AfterClass
    public static void afterClass() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    public CheckTreeViewTest() {
    }
    
    @Before
    public void setUp() {
        CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>("Root");
        root.setExpanded(true);
        root.getChildren().addAll(
                treeItem_Jonathan,
                treeItem_Eugene,
                treeItem_Henry,
                treeItem_Samir);
        
        // lets check Eugene to make sure that it shows up in the tree
        treeItem_Eugene.setSelected(true);
        
        // CheckListView
        checkTreeView = new CheckTreeView<>(root);
        checkTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }


    /**
     * This is related to https://bitbucket.org/controlsfx/controlsfx/issue/447
     * We test if the clearChecks raise ConcurrentModificationException.
     */
    @Test
    public void testConcurrentModification() {
       checkTreeView.getCheckModel().checkAll();
       checkTreeView.getCheckModel().clearChecks();
    }
}
