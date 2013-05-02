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
package org.controlsfx.dialogs;

import static org.controlsfx.dialogs.Dialog.Actions.CANCEL;
import static org.controlsfx.dialogs.DialogResources.getString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

import com.sun.javafx.Utils;

/**
 * A lower-level API for creating standardized dialogs consisting of the following
 * subsections:
 * 
 * <ul>
 *   <li>masthead, 
 *   <li>content, 
 *   <li>expandable content,
 *   <li>button bar
 * </ul>
 * 
 * For developers wanting a simpler API, consider using the high-level 
 * {@link Dialogs} class, which provides a simpler, fluent API for displaying
 * the most common types of dialog.
 * 
 * @see Dialogs
 */
public class Dialog {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    // According to the UI spec, the width of the main message text in the upper
    // panel should be 426 pixels.
    private static int MAIN_TEXT_WIDTH = 400;

    // Specifies the minimum allowable width for all buttons in the dialog
    private static int MINIMUM_BUTTON_WIDTH = 75;
    
    
    
    /**************************************************************************
     * 
     * Fields
     * 
     **************************************************************************/

    private final FXDialog dialog;

    // Dialog result.
    protected Action result = Actions.CANCEL;

    private final BorderPane contentPane;
    
    // list containing user input buttons at bottom of dialog
    private List<ButtonBase> buttons = new ArrayList<ButtonBase>();
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    /**
     * Creates a dialog using specified owner and title.
     * 
     * @param owner The dialog window owner - if specified the dialog will be
     *      centered over the owner, otherwise the dialog will be shown in the 
     *      middle of the screen.
     * @param title The dialog title to be shown at the top of the dialog.
     */
    public Dialog(Window owner, String title) {
        this.dialog = new FXDialog(title, owner, true);
        this.contentPane = new BorderPane();
        this.contentPane.getStyleClass().add("content-pane");
        contentPane.setPrefWidth(MAIN_TEXT_WIDTH);
        this.dialog.setContentPane(contentPane);
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/

    /**
     * Shows the dialog and waits for user response (in other words, brings up a
     * modal dialog).
     * 
     * @return The {@link Action} used to close the dialog.
     */
    public Action show() {
        try {
            buildDialogContent();
            dialog.centerOnScreen();
            dialog.showAndWait();
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return CANCEL;
        }
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        dialog.hide();
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/


    // --- resizable

    /**
     * Determines of dialog is resizable
     * @return true if dialog is resizable
     */
    public final boolean isResizable() {
        return dialog.isResizable();
    }

    /**
     * Changes the dialog's resizable attribute
     * @param resizable true if dialog should be resizable
     */
    public final void setResizable(boolean resizable) {
        dialog.setResizable(resizable);
    }

    /**
     * Represents whether the dialog is resizable.
     */
    public BooleanProperty resizableProperty() {
        return dialog.resizableProperty();
    }

    
    // --- graphic
    private final ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();

    /**
     * Dialog's graphic.
     * Presented either in the masthead, if one is available or in the content 
     * @return dialog's graphic
     */
    public final Node getGraphic() {
        return graphicProperty.get();
    }

    /**
     * Sets dialog's graphic
     * @param graphic dialog's graphic. Used if not null.
     */
    public final void setGraphic(Node graphic) {
        this.graphicProperty.set(graphic);
    }

    public ObjectProperty<Node> graphicProperty() {
        return graphicProperty;
    }

    
    // --- masthead
    private final ObjectProperty<Node> masthead = new SimpleObjectProperty<Node>();

    /**
     * Node which acts as dialog's masthead
     * @return dialog's masthead
     */
    public final Node getMasthead() {
        return masthead.get();
    }

    /**
     * Assigns dialog's masthead. Any Node can be used 
     * @param masthead future masthead
     */
    public final void setMasthead(Node masthead) {
        this.masthead.setValue(masthead);
    }

    /**
     * Asssign string as dialog's masthead
     * @param mastheadText masthead text. Used if not null.
     */
    public final void setMasthead(String mastheadText) {

        if (mastheadText == null)
            return;

        BorderPane mastheadPanel = new BorderPane();
        mastheadPanel.getStyleClass().add("masthead-panel");

        // Create panel with text area and graphic or just a background image:
        // Create topPanel's components. UITextArea determines
        // the size of the dialog by defining the number of columns
        // based on font size.
        Label mastheadTextArea = new Label();
        mastheadTextArea.setPrefWidth(MAIN_TEXT_WIDTH);
        mastheadTextArea.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        mastheadTextArea.setWrapText(true);

        VBox mastheadVBox = new VBox();
        mastheadVBox.setAlignment(Pos.CENTER_LEFT);
        mastheadTextArea.setText(mastheadText);
        mastheadTextArea.setAlignment(Pos.CENTER_LEFT);
        mastheadVBox.getChildren().add(mastheadTextArea);

        mastheadPanel.setLeft(mastheadVBox);
        BorderPane.setAlignment(mastheadVBox, Pos.CENTER_LEFT);

        if (graphicProperty.get() != null) {
            mastheadPanel.setRight(getGraphic());
        }

        setMasthead(mastheadPanel);
    }

    public ObjectProperty<Node> mastheadProperty() {
        return masthead;
    }

    
    // --- content
    private final ObjectProperty<Node> content = new SimpleObjectProperty<Node>() {
        @Override protected void invalidated() {
            Node contentNode = getContent();
            if (contentNode != null) {
                contentNode.getStyleClass().addAll("content");
            }
        };
    };

    /**
     * Current dialog's content as Node
     * @return dialog's content
     */
    public final Node getContent() {
        return content.get();
    }

    /**
     * Assign dialog content. Any Node can be used
     * @param content dialog's content
     */
    public final void setContent(Node content) {
        this.content.setValue(content);
    }

    /**
     * Assign text as dialog's content
     * @param contentText content text. Used if not null.
     */
    public final void setContent(String contentText) {
        if (contentText == null) return;

        Label label = new Label(contentText);
        label.setAlignment(Pos.TOP_LEFT);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setMaxWidth(Double.MAX_VALUE);

        // FIXME we don't want to restrict the width, but for now this works ok
        label.setPrefWidth(MAIN_TEXT_WIDTH);
//        label.setMaxWidth(360);
        label.setWrapText(true);

        setContent(label);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    
    // --- expandable content
    private final ObjectProperty<Node> expandableContentProperty = new SimpleObjectProperty<Node>();

    /**
     * Dialog's expandable content
     * @return expandable content as Node
     */
    public final Node getExpandableContent() {
        return expandableContentProperty.get();
    }

    /**
     * Assigns dialog's expandable content. Any Node can be used.
     * By default expandable conntent is hidden and can be made visible by clicking "Mode Details" hyperlink,
     * which appears automatically of non-null expandable content exists 
     * @param content expandable content.
     */
    public final void setExpandableContent(Node content) {
        this.expandableContentProperty.set(content);
    }

    public ObjectProperty<Node> expandableContentProperty() {
        return expandableContentProperty;
    }
    
    
    // --- width
    ReadOnlyDoubleProperty widthProperty() {
        return dialog.widthProperty();
    }
    
    
    // --- height
    ReadOnlyDoubleProperty heightProperty() {
        return dialog.heightProperty();
    }
    

    // --- actions
    private final ObservableList<Action> actions = FXCollections.<Action> observableArrayList();

    /**
     * Observable list of actions used for dialog's buttons bar.
     * Can be used for manipulating actions before presenting the dialog
     * @return actions list
     */
    public final ObservableList<Action> getActions() {
        return actions;
    }
    
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/

    /**
     * Common dialog actions
     */
    public enum Actions implements Action {

        CANCEL( getString("common.cancel.button"), true, true),
        CLOSE ( getString("common.close.button"),  true, true),
        NO    ( getString("common.no.button"),     true, true),
        OK    ( getString("common.ok.button"),     true, false),
        YES   ( getString("common.yes.button"),    true, false);

        private final StringProperty title = new SimpleStringProperty();
        private final BooleanProperty disabled = new SimpleBooleanProperty(false);
        private final StringProperty longText = new SimpleStringProperty(null);
        private final ObjectProperty<Node> graphic = new SimpleObjectProperty<Node>();
        
        private boolean isClosing;
        private boolean isDefault;
        private boolean isCancel;

        /**
         * Creates common dialog action
         * @param title action title
         * @param isDefault true if it should be default action on the dialog. Only one can be, so the first us used.
         * @param isCancel true if action produces the dialog cancellation. 
         * @param isClosing true if action is closing the dialog
         */
        private Actions(String title, boolean isDefault, boolean isCancel, boolean isClosing) {
            this.title.set(title);
            this.isClosing = isClosing;
            this.isDefault = isDefault;
            this.isCancel = isCancel;
        }

        private Actions(String title, boolean isDefault, boolean isCancel) {
            this(title, isDefault, isCancel, true);
        }

        @Override public StringProperty textProperty() {
            return title;
        }

        @Override public BooleanProperty disabledProperty() {
            return disabled;
        }
        
        @Override public StringProperty longTextProperty() {
            return longText;
        }
        
        @Override public ObjectProperty<Node> graphicProperty() {
            return graphic;
        }
        
        @Override public void execute(ActionEvent ae) {
            if ( !disabled.get() ) {
                if (ae.getSource() instanceof Dialog && (isCancel || isClosing) ) {
                    Dialog dlg = ((Dialog) ae.getSource());
                    dlg.result = Actions.this;        
                    dlg.hide();
                }
            }
        }

    }

    
    
    /**************************************************************************
     * 
     * Private Implementation
     * 
     **************************************************************************/

    /**
     * TODO delete me - this is just for testing!!
     */
    private static boolean isMac = false;
    private static boolean isWindows = false;

    static void setMacOS(boolean b) {
        isMac = b;
        isWindows = !b;
    }

    static void setWindows(boolean b) {
        isMac = !b;
        isWindows = b;
    }

    private boolean isMac() {
        return isMac || (!isWindows && Utils.isMac());
    }

    private boolean hasMasthead() {
        return getMasthead() != null;
    }

    private boolean hasExpandableContent() {
        return getExpandableContent() != null;
    }

    private void buildDialogContent() {
        contentPane.getChildren().clear();
        
        final boolean hasMasthead = hasMasthead();
        if (hasMasthead) {
            contentPane.setTop(getMasthead());
        }
        contentPane.setCenter(createCenterPanel());
        
        Parent root = dialog.getScene().getRoot();
        root.pseudoClassStateChanged(MASTHEAD_PSEUDO_CLASS,      hasMasthead);
        root.pseudoClassStateChanged(NO_MASTHEAD_PSEUDO_CLASS,   !hasMasthead);
    }

    private Pane createCenterPanel() {
        // -- new approach
        GridPane grid = new GridPane();
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.getStyleClass().add("center-panel");
        
        Node content = getContent();
        if (content != null) {
            grid.add(content, 1, 0);
            GridPane.setVgrow(content, Priority.ALWAYS);
            GridPane.setValignment(content, VPos.TOP);
            
            // FIXME this should be enabled (otherwise TextField input is not
            // stretched, but by enabling we get an infinite loop in GridPane)
//            GridPane.setHgrow(content, Priority.ALWAYS);
        }
        
        // dialog image can go to the left if there is no masthead
        final Node graphic = getGraphic();
        if (!hasMasthead() && graphic != null) {
            Pane graphicPane = new Pane(graphic);
            
            final double w = graphic.getLayoutBounds().getWidth();
            final double h = graphic.getLayoutBounds().getHeight();
            graphicPane.setMinSize(w, h);
            graphicPane.getStyleClass().add("graphic");
            grid.add(graphicPane, 0, 0);
            GridPane.setValignment(graphicPane, VPos.TOP);
            GridPane.setMargin(graphicPane, new Insets(0,8,0,0));
        }
        
        if (hasExpandableContent()) {
            Node ec = getExpandableContent();
            grid.add(ec, 0, 1, 2, 1);
            ec.setVisible(false);
            ec.managedProperty().bind(ec.visibleProperty());
        }
        
        if ( !getActions().isEmpty() || hasExpandableContent()) {
            Node buttonPanel = createButtonPanel();
            grid.add(buttonPanel, 0, 2, 2, 1);
       }

        return grid;
    }

    private Node createButtonPanel() {
        buttons.clear();
        
        final HBox buttonsPanel = new HBox(6) {
            @Override protected void layoutChildren() {
                resizeButtons();
                super.layoutChildren();
            }
        };
        buttonsPanel.getStyleClass().add("button-bar");

        // show details button if expandable content is present
        if (hasExpandableContent()) {
            buttonsPanel.getChildren().add(createDetailsButton());
        }

        // push buttons to the right
        buttonsPanel.getChildren().add(createButtonSpacer());

        boolean hasDefault = false;
        for (Action cmd : getActions()) {
            ButtonBase b = createButton(cmd, !hasDefault);
            // keep only first default button
            if (b instanceof Button) {
                hasDefault |= ((Button) b).isDefaultButton();
            }
            buttons.add(b);
        }

        // OS based order of buttons
        if (isMac())
            Collections.reverse(buttons);

        for (ButtonBase button : buttons) {
            buttonsPanel.getChildren().add(button);
        }

        return buttonsPanel;
    }
    
    /*
     * According to UI guidelines, all buttons should have the same length. This
     * function is to define the longest button in the array of buttons and set
     * all buttons in array to be the length of the longest button.
     */
    private void resizeButtons() {
        // Find out the longest button...
        double widest = MINIMUM_BUTTON_WIDTH;
        for (ButtonBase btn : buttons) {
            if (btn == null)
                continue;
            widest = Math.max(widest, btn.prefWidth(-1));
        }

        // ...and set all buttons to be this width
        for (ButtonBase btn : buttons) {
            if (btn == null)
                continue;
            btn.setPrefWidth(btn.isVisible() ? widest : 0);
        }
    }

    private Node createButtonSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Hyperlink createDetailsButton() {
        final Hyperlink detailsButton = new Hyperlink();
        detailsButton.getStyleClass().setAll("details-button", "more");
        final String moreText = getString("common.detail.button.more");
        final String lessText = getString("common.detail.button.less");
        detailsButton.setText(moreText);
        detailsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                final Node content = getExpandableContent();
                setResizable(!content.isVisible());
                content.setVisible(!content.isVisible());
                detailsButton.setText(content.isVisible() ? lessText : moreText);
                dialog.sizeToScene();
                detailsButton.getStyleClass().setAll("details-button", (content.isVisible() ? "less" : "more"));
            }
        });
        return detailsButton;
    }

    private Button createButton(final Action action, boolean keepDefault) {
        final Button button = new Button();
        
        // button bind to action properties
        button.textProperty().bind(action.textProperty());
        button.disableProperty().bind(action.disabledProperty());
        button.graphicProperty().bind(action.graphicProperty());
        
        // tooltip requires some special handling (i.e. don't have one when
        // the text property is null
        button.tooltipProperty().bind(new ObjectBinding<Tooltip>() {
            private Tooltip tooltip = new Tooltip();
            
            { 
                bind(action.longTextProperty()); 
                tooltip.textProperty().bind(action.longTextProperty());
            }
            
            @Override protected Tooltip computeValue() {
                String longText = action.longTextProperty().get();
                return longText == null || longText.isEmpty() ? null : tooltip;
            }
        });
        
        if (action instanceof Actions) {
            Actions stdAction = (Actions) action;
            button.setDefaultButton(stdAction.isDefault && keepDefault);
            button.setCancelButton(stdAction.isCancel);
        }
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                action.execute(new ActionEvent(Dialog.this, ae.getTarget()));
                result = action;
            }
        });
        return button;
    }

    
    /***************************************************************************
     *                                                                         
     * Stylesheet Handling                                                     
     *                                                                         
     **************************************************************************/
    private static final PseudoClass MASTHEAD_PSEUDO_CLASS = 
            PseudoClass.getPseudoClass("masthead");
    private static final PseudoClass NO_MASTHEAD_PSEUDO_CLASS = 
            PseudoClass.getPseudoClass("no-masthead");
}
