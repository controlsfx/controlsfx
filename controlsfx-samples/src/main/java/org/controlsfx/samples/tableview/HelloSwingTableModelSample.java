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
//import javafx.scene.Node;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//import javax.swing.table.DefaultTableModel;
//import javax.swing.table.TableModel;
//
//import org.controlsfx.ControlsFXSample;
//import org.controlsfx.control.table.model.JavaFXTableModels;
//import org.controlsfx.control.table.model.TableModelTableView;
//import org.controlsfx.samples.Utils;
//
//// TODO sorting doesn't work due to readonlyunbacked list
//public class HelloSwingTableModelSample extends ControlsFXSample {
//
//    @Override public String getSampleName() {
//        return "Swing TableModel TableView";
//    }
//    
//    @Override public String getJavaDocURL() {
//        return Utils.JAVADOC_BASE + "org/controlsfx/control/table/TableModelTableView.html";
//    }
//    
//    @Override public Node getPanel(final Stage stage) {
//        TableModel swingTableModel = new DefaultTableModel(
//            new Object[][] { /* Data: row, column */
//                { "1", "2", "3" }, 
//                { "4", "5", "6" },
//                { "7", "8", "9" },
//                { "10", "11", "12" },
//            }, 
//            new String[] {  /* Column names */
//                "Column 1", "Column 2", "Column 3"
//            }
//        );
//        
//        TableModelTableView<String> tableView = new TableModelTableView<>(JavaFXTableModels.wrap(swingTableModel));
//        
////        tableView.getSelectionModel().selectedItemProperty().addListener((o, oldRow, newRow) -> {
////            System.out.println("Old row: " + oldRow + ", new row: " + newRow);
////        });
//        
//        VBox root = new VBox();
//        root.getChildren().add(tableView);
//        return root;
//    }
//    
//    public static void main(String[] args) {
//        launch(args);
//    } 
//}
