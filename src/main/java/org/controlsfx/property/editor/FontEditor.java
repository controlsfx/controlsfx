/**
 * Copyright (c) 2013, ControlsFX
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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.dialog.Dialog;

public class FontEditor extends AbstractObjectPropertyEditor<Font, FontObjectEditor> {

    public FontEditor( Item property ) {
        super(property, new FontObjectEditor());
    }

}

class FontObjectEditor extends AbstractObjectField<Font> {
    
    @Override
    protected Class<Font> getType() {
        return Font.class;
    }
    
    @Override
    protected String objectToString( Font font ) {
        return font == null? "": String.format("%s, %.1f", font.getName(), font.getSize());
    }

    
    
    
    @Override
    protected Font edit( Font font ) {
        // TODO: Fully working font selection dialog
        FontPanel fontPanel = new FontPanel();
        fontPanel.setFont(font);
        Dialog dlg = new Dialog( this.getScene().getWindow(), "Select Font" );
        dlg.setIconifiable(false);
        dlg.setContent(fontPanel);
        dlg.getActions().addAll(Dialog.Actions.OK, Dialog.Actions.CANCEL);
        return Dialog.Actions.OK == dlg.show()? fontPanel.getFont(): font; 
    }
    
    
    private static class FontPanel extends GridPane {
        
        private static Double[] fontSizes = new Double[] {8d,9d,11d,12d,14d,16d,18d,20d,22d,24d,26d,28d,36d,48d,72d};
        
        private TextField fontSearch = new TextField();
        private TextField postureSearch = new TextField();
        private NumericField sizeSearch = new NumericField();
        
        private ListView<String> fontList = new ListView<String>( FXCollections.observableArrayList(Font.getFamilies()));
        private ListView<FontPosture> styleList = new ListView<FontPosture>( FXCollections.observableArrayList(FontPosture.values()));
        private ListView<Double> sizeList = new ListView<Double>( FXCollections.observableArrayList(fontSizes));
        private Label sample = new Label("Sample");
        
        public FontPanel() {
            
            setHgap(10);
            setVgap(5);
            setPrefSize(500, 300);
            setMinSize(500, 300);
            
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setPercentWidth(60);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setPercentWidth(25);
            ColumnConstraints c3 = new ColumnConstraints();
            c3.setPercentWidth(15);
            getColumnConstraints().addAll(c1, c2, c3);
            
            RowConstraints r1 = new RowConstraints();
            r1.setFillHeight(true);
            r1.setVgrow(Priority.NEVER);
            RowConstraints r2 = new RowConstraints();
            r2.setFillHeight(true);
            r2.setVgrow(Priority.NEVER);
            RowConstraints r3 = new RowConstraints();
            r3.setFillHeight(true);
            r3.setVgrow(Priority.ALWAYS);
            RowConstraints r4 = new RowConstraints();
            r4.setFillHeight(true);
            r4.setPrefHeight(250);
            r4.setVgrow(Priority.SOMETIMES);
            getRowConstraints().addAll(r1, r2, r3, r4);
            
            add( new Label("Font"), 0, 0);
            fontSearch.setMinHeight(Control.USE_PREF_SIZE);
            add( fontSearch, 0, 1);
            add(fontList, 0, 2);
            fontList.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                    refreshSample();
                }});

            add( new Label("Style"), 1, 0);
            postureSearch.setMinHeight(Control.USE_PREF_SIZE);
            add( postureSearch, 1, 1);
            add(styleList, 1, 2);
            styleList.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<FontPosture>() {
                @Override public void changed(ObservableValue<? extends FontPosture> arg0, FontPosture arg1, FontPosture arg2) {
                    refreshSample();
                }});
            
            
            add( new Label("Size"), 2, 0);
            sizeSearch.setMinHeight(Control.USE_PREF_SIZE);
            add( sizeSearch, 2, 1);
            add(sizeList, 2, 2);
            sizeList.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<Double>() {
                @Override public void changed(ObservableValue<? extends Double> arg0, Double arg1, Double arg2) {
                    refreshSample();
                }});
            
            
            sample.setTextAlignment(TextAlignment.CENTER);
            add(sample, 0, 3, 1, 3);
            
        }
        
        public void setFont( Font font ) {
            selectInList( fontList,  font.getFamily() );
            selectInList( styleList, FontPosture.findByName(font.getStyle().toUpperCase()) );
            selectInList( sizeList, font.getSize() );
        }
        
        public Font getFont() {
            try {
                return Font.font( listSelection(fontList),listSelection(styleList),listSelection(sizeList));
            } catch( Throwable ex ) {
                return null;
            }
        }
        
        private void refreshSample() {
            sample.setFont(getFont());
        }
        
        private <T> void selectInList( final ListView<T> listView, final T selection ) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    listView.scrollTo(selection);
                    listView.getSelectionModel().select(selection);
                }
            });
        }
        
        private <T> T listSelection( final ListView<T> listView) {
            return listView.selectionModelProperty().get().getSelectedItem();
        }
        
        
    }
    
    
}
