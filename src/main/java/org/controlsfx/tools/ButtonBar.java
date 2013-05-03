package org.controlsfx.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.javafx.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class ButtonBar extends HBox {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/

    private static String CATEGORIZED_TYPES = "LRHEYNXBIACO";
    
    // represented as a ButtonType
    private static String BUTTON_TYPE_PROPERTY = "controlfx.button.type";
    
    /**
     * The default button ordering on Windows.
     */
    public static final String BUTTON_ORDER_WINDOWS = "L_E+U+FBI_YNOCAH_R";
    
    /**
     * The default button ordering on Mac OS.
     */
    public static final String BUTTON_ORDER_MAC_OS  = "L_HE+U+FBI_NYCOA_R";
    
    /**
     * The default button ordering on Linux (specifically, GNOME).
     */
    public static final String BUTTON_ORDER_LINUX   = "L_HE+UNYACBXIO_R";
    
    
    
    /**************************************************************************
     * 
     * Static enumerations
     * 
     **************************************************************************/
    
    /**
     * An enumeration of all available button types. By designating ever button
     * in a {@link ButtonBar} as one of these types, the buttons will be 
     * appropriately positioned relative to all other buttons in the ButtonBar.
     */
    public static enum ButtonType {
        /**
         * Buttons with this style tag will statically end up on the left end of the bar.
         */
        LEFT("L"),
        
        /**
         * Buttons with this style tag will statically end up on the right end of the bar.
         */
        RIGHT("R"),
        
        /**
         * A tag for the "help" button that normally is supposed to be on the right.
         */
        HELP("H"),
        
        /**
         * A tag for the "help2" button that normally is supposed to be on the left.
         */
        HELP_2("E"),
        
        /**
         * A tag for the "yes" button.
         */
        YES("Y"),
        
        /**
         * A tag for the "no" button.
         */
        NO("N"),
        
        /**
         * A tag for the "next >" or "forward >" button.
         */
        NEXT_FORWARD("X"),
        
        /**
         * A tag for the "< back>" or "< previous" button.
         */
        BACK_PREVIOUS("B"),
        
        /**
         * A tag for the "finish".
         */
        FINISH("I"),
        
        /**
         * A tag for the "apply" button.
         */
        APPLY("A"),
        
        /**
         * A tag for the "cancel" or "close" button.
         */
        CANCEL_CLOSE("C"),
        
        /**
         * A tag for the "ok" or "done" button.
         */
        OK_DONE("O"),
        
        /**
         * All Uncategorized, Other, or "Unknown" buttons. Tag will be "other".
         */
        OTHER("U"),
        
        /**
         * A glue push gap that will take as much space as it can and at least 
         * an "unrelated" gap. (Platform dependant)
         */
        BIG_GAP("+"),
        
        /**
         * An "unrelated" gap. (Platform dependant)
         */
        SMALL_GAP("_");
        
        private final String type;
        private ButtonType(String type) {
            this.type = type;
        }
        
        private String getType() {
            return type;
        }
    }
    
    /**
     * Sets the given ButtonType on the given button. If this button is
     * subsequently placed in a {@link ButtonBar} it will be placed in the 
     * correct position relative to all other buttons in the bar.
     * 
     * @param button The button to tag with the given type.
     * @param type The type to designate the button as.
     */
    public static void setType(ButtonBase button, ButtonType type) {
        button.getProperties().put(BUTTON_TYPE_PROPERTY, type);
    }
    
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private String buttonOrder;
    
    private ObservableList<ButtonBase> buttons = FXCollections.<ButtonBase>observableArrayList();
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    public ButtonBar() {
        // TODO: the gap has to be OS dependent
        super(10);
        buttons.addListener( new ListChangeListener<ButtonBase>() {
            @Override public void onChanged(ListChangeListener.Change<? extends ButtonBase> change) {
                layoutButtons(buttons);
            }
        });
        
        // set the default button order 
        if (Utils.isMac()) {
            setButtonOrder(BUTTON_ORDER_MAC_OS);
        } else if (Utils.isUnix()) {
            setButtonOrder(BUTTON_ORDER_LINUX);
        } else {
            // windows by default
            setButtonOrder(BUTTON_ORDER_WINDOWS);
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/

    public ObservableList<ButtonBase> getButtons() {
        return buttons;
    }
    
    
    /**
     * Sets the order for the typical buttons in a standard button bar. It is 
     * one letter per button type, and the applicable options are part of 
     * {@link ButtonType}. Default button orders for operating systems are also 
     * available: {@link #BUTTON_ORDER_WINDOWS}, {@link #BUTTON_ORDER_MAC_OS},
     * {@link #BUTTON_ORDER_LINUX}.
     */
    public void setButtonOrder(String buttonOrder) {
        this.buttonOrder = buttonOrder;
        
        layoutButtons(buttons);
    }
    
    
    
    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
    private void layoutButtons(List<? extends ButtonBase> buttons) {
        
        getChildren().clear();
        
        Map<String, List<ButtonBase>> buttonMap = buildButtonMap(buttons);
        SpacerType preparedSpacer = SpacerType.NONE;
        for ( char type: buttonOrder.toCharArray()) {
            
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
    
    private String getButtonType( ButtonBase btn ) {
        ButtonType buttonType =  (ButtonType) btn.getProperties().get(BUTTON_TYPE_PROPERTY);
        String typeString = buttonType.getType();
        typeString = typeString.length() > 0? typeString.substring(0,1): "";
        return CATEGORIZED_TYPES.contains(typeString.toUpperCase())? typeString: "U"; 
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

    
    
    /**************************************************************************
     * 
     * Support classes / enums
     * 
     **************************************************************************/
    
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
}
