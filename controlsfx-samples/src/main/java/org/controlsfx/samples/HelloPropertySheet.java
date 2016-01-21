/**
 * Copyright (c) 2014, 2015 ControlsFX
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
package org.controlsfx.samples;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.PropertySheet.Mode;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.property.BeanProperty;
import org.controlsfx.property.BeanPropertyUtils;
import org.controlsfx.samples.propertysheet.CustomPropertyDescriptor;
import org.controlsfx.samples.propertysheet.SampleBean;

public class HelloPropertySheet extends ControlsFXSample {

    private static Map<String, Object> customDataMap = new LinkedHashMap<>();

    static {
        customDataMap.put("1. Name#First Name", "Jonathan");
        customDataMap.put("1. Name#Last Name", "Giles");
        customDataMap.put("1. Name#Birthday", LocalDate.of(1985, Month.JANUARY, 12));
        customDataMap.put("2. Billing Address#Address 1", "");
        customDataMap.put("2. Billing Address#Address 2", "");
        customDataMap.put("2. Billing Address#City", "");
        customDataMap.put("2. Billing Address#State", "");
        customDataMap.put("2. Billing Address#Zip", "");
        customDataMap.put("3. Phone#Home", "123-123-1234");
        customDataMap.put("3. Phone#Mobile", "234-234-2345");
        customDataMap.put("3. Phone#Work", "");
    }

    private PropertySheet propertySheet = new PropertySheet();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public String getSampleName() {
        return "Property Sheet";
    }

    @Override
    public String getSampleDescription() {
        return "The PropertySheet control is useful when you want to present a number"
                + " of properties to a user for them to edit.";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/PropertySheet.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/propertysheet.css";
    }

    class CustomPropertyItem implements Item {

        private String key;
        private String category, name;

        public CustomPropertyItem(String key) {
            this.key = key;
            String[] skey = key.split("#");
            category = skey[0];
            name = skey[1];
        }

        @Override
        public Class<?> getType() {
            return customDataMap.get(key).getClass();
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Object getValue() {
            return customDataMap.get(key);
        }

        @Override
        public void setValue(Object value) {
            customDataMap.put(key, value);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.empty();
        }

    }

    class ActionShowInPropertySheet extends Action {

        private Object bean;

        public ActionShowInPropertySheet(String title, Object bean) {
            super(title);
            setEventHandler(this::handleAction);
            this.bean = bean;
        }

        private ObservableList<Item> getCustomModelProperties() {
            ObservableList<Item> list = FXCollections.observableArrayList();
            for (String key : customDataMap.keySet()) {
                list.add(new CustomPropertyItem(key));
            }
            return list;
        }

        private void handleAction(ActionEvent ae) {

            // retrieving bean properties may take some time
            // so we have to put it on separate thread to keep UI responsive
            Service<?> service = new Service<ObservableList<Item>>() {

                @Override
                protected Task<ObservableList<Item>> createTask() {
                    return new Task<ObservableList<Item>>() {
                        @Override
                        protected ObservableList<Item> call() throws Exception {
                            return bean == null ? getCustomModelProperties() : BeanPropertyUtils.getProperties(bean);
                        }
                    };
                }

            };
            service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @SuppressWarnings("unchecked")
                @Override
                public void handle(WorkerStateEvent e) {
                    if (bean instanceof SampleBean) {
                        for (Item i : (ObservableList<Item>) e.getSource().getValue()) {
                            if (i instanceof BeanProperty && ((BeanProperty) i).getPropertyDescriptor() instanceof CustomPropertyDescriptor) {
                                BeanProperty bi = (BeanProperty) i;
                                bi.setEditable(((CustomPropertyDescriptor) bi.getPropertyDescriptor()).isEditable());
                            }
                        }
                    }
                    propertySheet.getItems().setAll((ObservableList<Item>) e.getSource().getValue());
                }
            });
            service.start();

        }

    }

    @Override
    public Node getPanel(Stage stage) {
        return propertySheet;
    }

    @Override
    public Node getControlPanel() {
        VBox infoPane = new VBox(10);

        Button button = new Button("Title");
        TextField textField = new TextField();
        SampleBean sampleBean = new SampleBean();

        SegmentedButton segmentedButton = ActionUtils.createSegmentedButton(
                new ActionShowInPropertySheet("Bean: Button", button),
                new ActionShowInPropertySheet("Bean: TextField", textField),
                new ActionShowInPropertySheet("Custom Model", null),
                new ActionShowInPropertySheet("Custom BeanInfo", sampleBean)
        );
        segmentedButton.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
        segmentedButton.getButtons().get(0).fire();

        CheckBox toolbarModeVisible = new CheckBox("Show Mode Buttons");
        toolbarModeVisible.selectedProperty().bindBidirectional(propertySheet.modeSwitcherVisibleProperty());

        CheckBox toolbarSearchVisible = new CheckBox("Show Search Field");
        toolbarSearchVisible.selectedProperty().bindBidirectional(propertySheet.searchBoxVisibleProperty());

        infoPane.getChildren().add(toolbarModeVisible);
        infoPane.getChildren().add(toolbarSearchVisible);
        infoPane.getChildren().add(segmentedButton);
        infoPane.getChildren().add(button);
        infoPane.getChildren().add(textField);

        return infoPane;
    }

    class ActionModeChange extends Action {

        public ActionModeChange(String title, Mode mode) {
            super(title);
            setEventHandler(ae -> propertySheet.modeProperty().set(mode));
        }

    }

}
