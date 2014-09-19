///**
// * Copyright (c) 2014, ControlsFX
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *     * Redistributions of source code must retain the above copyright
// * notice, this list of conditions and the following disclaimer.
// *     * Redistributions in binary form must reproduce the above copyright
// * notice, this list of conditions and the following disclaimer in the
// * documentation and/or other materials provided with the distribution.
// *     * Neither the name of ControlsFX, any associated website, nor the
// * names of its contributors may be used to endorse or promote products
// * derived from this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
// * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package org.controlsfx.samples.tableview;
//
//import javafx.beans.property.ReadOnlyStringWrapper;
//import javafx.scene.Node;
//import javafx.scene.control.MenuItem;
//import javafx.scene.control.SeparatorMenuItem;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.TableView;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//import org.controlsfx.ControlsFXSample;
//import org.controlsfx.control.table.TableMenuButtonAccessor;
//import org.controlsfx.samples.Utils;
//
///**
// *
// */
//public class HelloCustomTableMenu extends ControlsFXSample {
//    
//    @Override public String getSampleName() {
//        return "Custom TableMenuButton";
//    }
//    
//    @Override public String getJavaDocURL() {
//        return Utils.JAVADOC_BASE + "org/controlsfx/control/table/TableMenuButtonAccessor.html";
//    }
//    
//    @Override public Node getPanel(final Stage stage) {
//        // boring - setting up TableView
//        TableView<String> tableView = new TableView<>();
//        tableView.getItems().addAll("Jonathan", "Julia", "Henry");
//        
//        TableColumn<String, String> names = new TableColumn<>("Name");
//        names.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue()));
//        
//        tableView.getColumns().add(names);
//        
//        VBox root = new VBox();
//        root.getChildren().add(tableView);
//        
//        // This is where it gets interesting - we modify the context menu
//        tableView.setTableMenuButtonVisible(true);
//        TableMenuButtonAccessor.modifyTableMenu(tableView, menu -> {
//            menu.getItems().addAll(new SeparatorMenuItem(), new MenuItem("Hello World!"));
//        });
//        
//        return root;
//    }
//    
//    public static void main(String[] args) {
//        launch(args);
//    } 
//}
