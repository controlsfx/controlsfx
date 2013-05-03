package org.controlsfx.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class ButtonBar extends HBox {

    private static String CATEGORIZED_TYPES = "LRHEYNXBIACO";
    private static String BUTTON_TYPE_PROPERTY = "controlfx.button.type";
    
    private static String order = "L_E+U+FBI_YNOCAH_R";

    public static void setType( ButtonBase button, char type ) {
        button.getProperties().put(BUTTON_TYPE_PROPERTY, String.valueOf(type));
    }
    
    private ObservableList<ButtonBase> buttons = FXCollections.<ButtonBase>observableArrayList();
    
    
    public ButtonBar() {
     // TODO: the gap has to be OS dependent
        super(10);
        buttons.addListener( new ListChangeListener<ButtonBase>() {
            @Override public void onChanged(ListChangeListener.Change<? extends ButtonBase> change) {
                layoutButtons(buttons);
            }
        });
    }

    public ObservableList<ButtonBase> getButtons() {
        return buttons;
    }
    

    private void layoutButtons(List<? extends ButtonBase> buttons) {
        
        getChildren().clear();
        
        Map<String, List<ButtonBase>> buttonMap = buildButtonMap(buttons);
        SpacerType preparedSpacer = SpacerType.NONE;
        for ( char type: order.toCharArray()) {
            
            if ( type == '+') {
               preparedSpacer = preparedSpacer.replace(SpacerType.DYNAMIC);
            } else if ( type == '_') {
               preparedSpacer = preparedSpacer.replace(SpacerType.FIXED);
            } else {
                
                List<ButtonBase> buttonList = buttonMap.get(String.valueOf(type).toUpperCase());
                if ( buttonList != null ) {
                    Node spacer = preparedSpacer.create();
                    if ( spacer != null ) {
                        getChildren().add(spacer);
                        preparedSpacer = preparedSpacer.replace(SpacerType.NONE);
                    }
                    
                    for( ButtonBase btn: buttonList) {
                        getChildren().add(btn);
                        HBox.setHgrow(btn, Priority.ALWAYS);
                    }
                } 
            }
        }
        
    }
    
    private enum SpacerType {
        FIXED {
            @Override public Node create() {
                Region spacer = new Region();
                spacer.setMinWidth(10);
                HBox.setHgrow(spacer, Priority.NEVER);
                return spacer;
            }
            
            public SpacerType replace( SpacerType type ) {
                return type == NONE || type == DYNAMIC? type: this;
            }
            
        },
        DYNAMIC {
            @Override public Node create() {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                return spacer;
            }
            
            public SpacerType replace( SpacerType type ) {
                return type == NONE? type: this;
            }
        },
        NONE {
            public SpacerType replace( SpacerType type ) {
                return type == FIXED || type == DYNAMIC? type: this;
            }

        };
        
        public abstract SpacerType replace( SpacerType type );
        
        public Node create() {
            return null;
        }
    }
    
    private String getButtonType( ButtonBase btn ) {
        String type =  (String) btn.getProperties().get(BUTTON_TYPE_PROPERTY);
        type = type.length() > 0? type.substring(0,1): "";
        return CATEGORIZED_TYPES.contains(type.toUpperCase())? type: "U"; 
    }
    
    private Map<String, List<ButtonBase>> buildButtonMap( List<? extends ButtonBase> buttons ) {
        
        Map<String, List<ButtonBase>> buttonMap = new HashMap<>();
        for (ButtonBase btn : buttons) {
            if ( btn == null ) continue;
            String type =  getButtonType(btn); 
            List<ButtonBase> typedButtons = buttonMap.get(type);
            if ( typedButtons == null ) {
                typedButtons = new ArrayList<ButtonBase>();
                buttonMap.put(type, typedButtons);
            }
            typedButtons.add( btn );
        }
        return buttonMap;
        
    }

}
