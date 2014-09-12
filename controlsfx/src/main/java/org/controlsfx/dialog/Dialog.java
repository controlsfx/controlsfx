/**
 * Copyright (c) 2013, 2014 ControlsFX
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
package org.controlsfx.dialog;

import impl.org.controlsfx.i18n.Localization;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.tools.Utils;

/**
 * A lower-level API for creating standardized dialogs consisting of the following
 * subsections:
 * 
 * <ul>
 *   <li>Title,
 *   <li>System buttons (min, max, close),
 *   <li>Masthead, 
 *   <li>Content, 
 *   <li>Expandable content,
 *   <li>Button bar
 * </ul>
 * 
 * <p>This is more easily demonstrated in the diagram shown below:
 * 
 * <br>
 * <center><img src="dialog-overview.png"></center>
 * 
 * <p>For developers wanting a simpler API, consider using the high-level 
 * {@link Dialogs} class, which provides a simpler, fluent API for displaying
 * the most common types of dialog.
 * 
 * <h3>Code Examples</h3>
 * 
 * <p><strong>Getting The User Response</strong><br>The most important point to 
 * note about the dialogs is that they are modal,
 * which means that they stop the application code from progressing until the
 * dialog is closed. Because of this, it is very easy to retrieve the users input:
 * when the user closes the dialog (e.g. by clicking on one of the buttons), the 
 * dialog will be hidden, and their response will be returned from the 
 * {@link #show() show} method that was called to bring the dialog up in the 
 * first place. In other words, you might do the following:
 * 
 * <pre>
 * {@code 
 * Action response = Dialogs.create()
 *      .title("You do want dialogs right?")
 *      .masthead("Just Checkin'")
 *      .message( "I was a bit worried that you might not want them, so I wanted to double check.")
 *      .showConfirm();
 *      
 * if (response == Dialog.Actions.OK) {
 *     // ... submit user input
 * } else {
 *     // ... user cancelled, reset form to default
 * }}</pre>
 * 
 * <p><strong>Custom Dialogs</strong><br>It is not always the case that one of 
 * the pre-built {@link Dialogs} suits the requirements of your application. In 
 * that case, you can build a custom dialog. Shown below is a screenshot of one
 * such custom dialog, and below that the source code used to create the code.
 * I hope you'll agree that the code is logical and simple!
 * 
 * <br>
 * <center>
 * <img src="dialog-login-sample.png"/>
 * </center>
 * 
 * <pre>
 * {@code
 * // This dialog will consist of two input fields (username and password),
 * // and have two buttons: Login and Cancel.
 * 
 * final TextField txUserName = new TextField();
 * final PasswordField txPassword = new PasswordField();
 * final Action actionLogin = new AbstractAction("Login") {
 *     {  
 *         ButtonBar.setType(this, ButtonType.OK_DONE); 
 *     }
 *       
 *     // This method is called when the login button is clicked...
 *     public void execute(ActionEvent ae) {
 *          Dialog dlg = (Dialog) ae.getSource();
 *          // real login code here
 *          dlg.hide();
 *      }
 * };
 *   
 * // This method is called when the user types into the username / password fields  
 * private void validate() {
 *     actionLogin.disabledProperty().set( 
 *           txUserName.getText().trim().isEmpty() || txPassword.getText().trim().isEmpty());
 * }
 *   
 * // Imagine that this method is called somewhere in your codebase
 * private void showLoginDialog() {
 *     Dialog dlg = new Dialog(null, "Login Dialog");
 *       
 *     // listen to user input on dialog (to enable / disable the login button)
 *     ChangeListener<String> changeListener = new ChangeListener<String>() {
 *         public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
 *             validate();
 *         }
 *     };
 *     txUserName.textProperty().addListener(changeListener);
 *     txPassword.textProperty().addListener(changeListener);
 *       
 *     // layout a custom GridPane containing the input fields and labels
 *     final GridPane content = new GridPane();
 *     content.setHgap(10);
 *     content.setVgap(10);
 *       
 *     content.add(new Label("User name"), 0, 0);
 *     content.add(txUserName, 1, 0);
 *     GridPane.setHgrow(txUserName, Priority.ALWAYS);
 *     content.add(new Label("Password"), 0, 1);
 *     content.add(txPassword, 1, 1);
 *     GridPane.setHgrow(txPassword, Priority.ALWAYS);
 *       
 *     // create the dialog with a custom graphic and the gridpane above as the
 *     // main content region
 *     dlg.setResizable(false);
 *     dlg.setIconifiable(false);
 *     dlg.setGraphic(new ImageView(HelloDialog.class.getResource("login.png").toString()));
 *     dlg.setContent(content);
 *     dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
 *     validate();
 *       
 *     // request focus on the username field by default (so the user can
 *     // type immediately without having to click first)
 *     Platform.runLater(new Runnable() {
 *         public void run() {
 *             txUserName.requestFocus();
 *         }
 *     });
 *
 *     dlg.show();
 * }
 * }</pre>
 * 
 * @see Dialogs
 * @see Action
 * @see Actions
 */
@Deprecated
public class Dialog {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    /**
     * Defines a native dialog style.
     * The dialogs rendered using this style will have a native title bar.
     */
    public static final String STYLE_CLASS_NATIVE = "native";
    
    /**
     * Defines a cross-platform dialog style.
     * The dialogs rendered using this style will have a cross-platform title bar.
     */
    public static final String STYLE_CLASS_CROSS_PLATFORM = "cross-platform";
    
    /**
     * Defines a dialog style with no decorations.
     * The dialogs rendered using this style will not have a title bar.
     */
    public static final String STYLE_CLASS_UNDECORATED = "undecorated";
    
    
    
    /**
     * An action that, by default, will show 'Cancel'.
     */
    public static final Action ACTION_CANCEL = new DialogAction( Localization.asKey("dlg.cancel.button"), ButtonType.CANCEL_CLOSE ){ //$NON-NLS-1$
        { lock();}
        @Override public String toString() { return "DialogAction.CANCEL";} //$NON-NLS-1$
    }; 
    
    /**
     * An action that, by default, will show 'Close'.
     */
    public static final Action ACTION_CLOSE = new DialogAction( Localization.asKey("dlg.close.button"), ButtonType.CANCEL_CLOSE ){ //$NON-NLS-1$
        { lock();}
        @Override public String toString() { return "DialogAction.CLOSE";} //$NON-NLS-1$
    }; 
    
    /**
     * An action that, by default, will show 'No'.
     */
    public static final Action ACTION_NO = new DialogAction( Localization.asKey("dlg.no.button"), ButtonType.NO ){ //$NON-NLS-1$
        { lock();}
        @Override public String toString() { return "DialogAction.NO";} //$NON-NLS-1$
    }; 
    
    /**
     * An action that, by default, will show 'OK'.
     */
    public static final Action ACTION_OK = new DialogAction( Localization.asKey("dlg.ok.button"), ButtonType.OK_DONE,  false, true, true){ //$NON-NLS-1$
        { lock();}
        @Override public String toString() { return "DialogAction.OK";} //$NON-NLS-1$
    }; 
    
    /**
     * An action that, by default, will show 'Yes'.
     */
    public static final Action ACTION_YES = new DialogAction( Localization.asKey("dlg.yes.button"), ButtonType.YES, false, true, true ){ //$NON-NLS-1$
        { lock();}
        @Override public String toString() { return "DialogAction.YES";} //$NON-NLS-1$
    }; 
    
    
    
    
    // enable to turn on grid lines, etc
    private static final boolean DEBUG = false;
    
    static int MIN_DIALOG_WIDTH = 426;
    
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/

    private final FXDialog dialog;

    // Dialog result, package-protected for Dialogs
    Action result = ACTION_CANCEL;

    private GridPane contentPane;
    
    // used to represent whether the masthead being used is generated by the
    // developer passing in a String, or whether they passed in a Node. If it is
    // a String, this is true. Otherwise it is false.
    private boolean isDefaultMasthead = false;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a heavyweight dialog using specified owner and title.
     * 
     * @param owner The dialog window owner - if specified the dialog will be
     *      centered over the owner, otherwise the dialog will be shown in the 
     *      middle of the screen.
     * @param title The dialog title to be shown at the top of the dialog.
     */
    public Dialog(Object owner, String title) {
        this(owner, title, false);
    }

    /**
     * Creates a dialog using specified owner and title, which may be rendered
     * in either a heavyweight or lightweight fashion.
     * 
     * @param owner The dialog window owner - if specified the dialog will be
     *      centered over the owner, otherwise the dialog will be shown in the 
     *      middle of the screen.
     * @param title The dialog title to be shown at the top of the dialog.
     * @param lightweight If true this dialog will be rendered inside the given
     *      owner, rather than in a separate window (as heavyweight dialogs are).
     *      Refer to the {@link Dialogs} class documentation for more details on
     *      the difference between heavyweight and lightweight dialogs.
     */
    public Dialog(Object owner, String title, boolean lightweight) {
        if (lightweight) {
            this.dialog = new LightweightDialog(title, owner);
        } else {
            Window window = Utils.getWindow(owner);
            this.dialog = new HeavyweightDialog(title, window);
            this.dialog.setModal(true);
        }
        
        updateStyleClasses(dialog.getStyleClass(), false);
        
        // if the actions change, we dynamically update the dialog
        getActions().addListener((ListChangeListener<Action>) c -> {
            if (dialog.getWindow().isShowing()) {
                updateDialogContent();
            }
        });
    }
    
    void updateStyleClasses(List<String> styleClasses, boolean addUnique) {
        // let's check to see what styleclasses have been set. If any have been
        // set outside the 'common' set, we will not set the default cross-platform
        // style class, otherwise we will
        if (styleClasses == null) return;
        
        for (String styleClass : styleClasses) {
            if (addUnique && ! dialog.getStyleClass().contains(styleClass)) {
                dialog.getStyleClass().add(styleClass);
            }
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * Shows the dialog and waits for the user response (in other words, brings 
     * up a modal dialog, with the returned value the users input).
     * 
     * @return The {@link Action} used to close the dialog.
     */
    public Action show() {
        try {
            updateDialogContent();
            dialog.show();
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return ACTION_CANCEL;
        }
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        dialog.hide();
    }
    
    /**
     * Visibly shakes the dialog to get the users attention.
     */
    public void shake() {
        dialog.shake();
    }
    
    /**
     * Assigns the resulting action. If action is a {@link DialogAction} and has either CANCEL or CLOSING traits
     * the dialog will be closed.
     * @param result
     */
    public void setResult(Action result) {
        this.result = result;
        
        if (result instanceof DialogAction) {
            DialogAction dlgAction = (DialogAction) result;
            if (dlgAction.isCancel() || dlgAction.isClosing()) {
                hide();
            }
        }
    }
    
    /**
     * Return the StyleSheets associated with the scene used in the Dialog (see {@link Scene#getStylesheets()}
     * This allow you to specify custom CSS rules to be applied on your dialog's elements.
     */
    public ObservableList<String> getStylesheets(){
        return dialog.getStylesheets();
    }
    
    /**
     * Return the style classes specified on this dialog instance.
     */
    public ObservableList<String> getStyleClass() {
        return dialog.getStyleClass();
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/


    // --- resizable
    /**
     * Represents whether the dialog is resizable.
     */
    public BooleanProperty resizableProperty() {
        return dialog.resizableProperty();
    }
    
    /**
     * Returns whether or not the dialog is resizable.
     * 
     * @return true if dialog is resizable.
     */
    public final boolean isResizable() {
        return resizableProperty().get();
    }

    /**
     * Sets whether the dialog can be resized by the user.
     * Resizable dialogs can also be maximized ( maximize button
     * becomes visible)  
     * 
     * @param resizable true if dialog should be resizable.
     */
    public final void setResizable(boolean resizable) {
        resizableProperty().set(resizable);
    }

    /**
     * Sets whether the dialog can be iconified (minimized)
     * @param iconifiable if dialog should be iconifiable
     */
    public void setIconifiable(boolean iconifiable) {
        dialog.setIconifiable(iconifiable);
    }
    
    /**
     * Sets whether the dialog can be closed
     * @param closable if dialog should be closable
     */
    public void setClosable( boolean closable ) {
        dialog.setClosable( closable );
    }
    
    // --- graphic
    private final ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>() {
        @Override protected void invalidated() {
            updateGraphic();
        }
    };

    /**
     * The dialog graphic, presented either in the masthead, if one is showing,
     * or to the left of the {@link #contentProperty() content}.
     *  
     * @return An ObjectProperty wrapping the current graphic.
     */
    public ObjectProperty<Node> graphicProperty() {
        return graphicProperty;
    }
    
    /**
     * @see #graphicProperty() 
     * @return The currently set dialog graphic.
     */
    public final Node getGraphic() {
        return graphicProperty.get();
    }

    /**
     * Sets the dialog graphic, which will be displayed either in the masthead, 
     * if one is showing, or to the left of the {@link #contentProperty() content}.
     * 
     * @param graphic The new dialog graphic, or null if no graphic should be shown. 
     */
    public final void setGraphic(Node graphic) {
        this.graphicProperty.set(graphic);
    }

    
    // --- masthead
    private final ObjectProperty<Node> masthead = new SimpleObjectProperty<Node>() {
        @Override protected void invalidated() {
            // we don't know where this masthead come from, so we reset the
            // default masthead flag to false.
            isDefaultMasthead = false;
        }
    };
    
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
     * Sets the string to show in the dialog masthead area.
     * 
     * @param mastheadText The masthead text to show in the masthead area.
     */
    public final void setMasthead(String mastheadText) {
        if (mastheadText == null) {
            return;
        }
        
        BorderPane mastheadPanel = new BorderPane();
        mastheadPanel.getStyleClass().add("masthead-panel"); //$NON-NLS-1$

        // on left of masthead is the text
        Label mastheadLabel = new Label(mastheadText);
        mastheadLabel.setWrapText(true);
        mastheadLabel.setAlignment(Pos.CENTER_LEFT);
        mastheadLabel.setMaxWidth(MIN_DIALOG_WIDTH);
        mastheadPanel.setLeft(mastheadLabel);
        BorderPane.setAlignment(mastheadLabel, Pos.CENTER_LEFT);

        setMasthead(mastheadPanel);
        
        // after we set the masthead, we reset the isDefaultMasthead flag to
        // true, as we know this is a default masthead
        isDefaultMasthead = true;
        
        // on the right of the masthead is a graphic, if one is specified
        updateGraphic();
    }
    
    private void updateGraphic() {
        final Node graphic = getGraphic();
        final Node masthead = getMasthead(); 
        
        if (masthead != null) {
            // we need to update the graphic on the right of the masthead area,
            // but we only do this if we have a default masthead.
            if (isDefaultMasthead) {
                BorderPane mastheadBorderPane = (BorderPane)masthead;
                if (graphic == null) {
                    mastheadBorderPane.setRight(null);
                } else {
                    StackPane pane = new StackPane(graphic);
                    pane.getStyleClass().add("graphic-container"); //$NON-NLS-1$
                    mastheadBorderPane.setRight(pane);
                }
            }
        }
    }

    /**
     * Property representing the masthead area of the dialog.
     */
    public ObjectProperty<Node> mastheadProperty() {
        return masthead;
    }

    
    // --- content
    private final ObjectProperty<Node> content = new SimpleObjectProperty<Node>() {
        @Override protected void invalidated() {
            Node contentNode = getContent();
            if (contentNode != null) {
                contentNode.getStyleClass().addAll("content"); //$NON-NLS-1$
            }
            
            if (dialog.getWindow().isShowing()) {
                updateDialogContent();
            }
        };
    };

    /**
     * Returns the dialog content as a Node (even if it was set as a String using
     * {@link #setContent(String)} - this was simply transformed into a 
     * {@link Node} (most probably a {@link Label}).
     * 
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
     * Assign text as the dialog's content (this will be transformed into a 
     * {@link Node} and then set via {@link #setContent(Node)}).
     * 
     * @param contentText The text to show in the main section of the dialog. 
     */
    public final void setContent(String contentText) {
        if (contentText == null) return;

        Label label = new Label(contentText);
        label.setAlignment(Pos.TOP_LEFT);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);

        // FIXME we don't want to restrict the width, but for now this works ok
//        label.setPrefWidth(MAIN_TEXT_WIDTH);
        label.setMaxWidth(360);
        label.setWrapText(true);

        setContent(label);
    }

    /**
     * Property representing the content area of the dialog.
     */
    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    
    // --- expandable content
    private final ObjectProperty<Node> expandableContentProperty = new SimpleObjectProperty<>();
    
    /**
     * A property that represents the dialog expandable content area. Any Node 
     * can be placed in this area, but it will only be shown when the user clicks
     * the 'Show Details' expandable button. This button will be added automatically
     * when the expandable content property is non-null.
     */
    public ObjectProperty<Node> expandableContentProperty() {
        return expandableContentProperty;
    }

    /**
     * Returns the dialog expandable content node, if one is set, or null otherwise.
     */
    public final Node getExpandableContent() {
        return expandableContentProperty.get();
    }

    /**
     * Sets the dialog expandable content node, or null if no expandable content
     * needs to be shown.
     */
    public final void setExpandableContent(Node content) {
        this.expandableContentProperty.set(content);
    }

    
    
    
    // --- width
    /**
     * Property representing the width of the dialog.
     */
    ReadOnlyDoubleProperty widthProperty() {
        return dialog.widthProperty();
    }
    
    /**
     * Returns the width of the dialog.
     */
    public final double getWidth() {
        return widthProperty().get();
    }
    
    
    // --- height
    /**
     * Property representing the height of the dialog.
     */
    ReadOnlyDoubleProperty heightProperty() {
        return dialog.heightProperty();
    }
    
    /**
     * Returns the height of the dialog.
     */
    public final double getHeight() {
        return heightProperty().get();
    }
    

    // --- actions
    private final ObservableList<Action> actions = FXCollections.<Action> observableArrayList();

    /**
     * Observable list of actions used for the dialog {@link ButtonBar}. 
     * Modifying the contents of this list will change the actions available to
     * the user.
     * @return The {@link ObservableList} of actions available to the user.
     */
    public final ObservableList<Action> getActions() {
        return actions;
    }
    
    /**
     * Return the titleProperty of the dialog.
     */
    public StringProperty titleProperty(){
        return this.dialog.titleProperty();
    }
    
    /**
     * Return the title of the dialog.
     */
    public String getTitle(){
        return this.dialog.titleProperty().get();
    }
    /**
     * Change the Title of the dialog.
     * @param title
     */
    public void setTitle(String title){
        this.dialog.titleProperty().set(title);
    }
    
    
    // --- background effect
    private final ObjectProperty<Effect> backgroundEffectProperty = new SimpleObjectProperty<Effect>() {
        @Override protected void invalidated() {
            dialog.setEffect(getValue());
        }
    };

    /**
     * Background effect property of the dialog
     */
    public ObjectProperty<Effect> backgroundEffectProperty() {
        return backgroundEffectProperty;
    }
    
    /**
     * Gets background effect of the dialog
     */
    public final Effect getBackgroundEffect() {
        return backgroundEffectProperty.get();
    }

    /**
     * Sets background effect of the dialog
     */
    public final void setBackgroundEffect(Effect effect) {
        this.backgroundEffectProperty.set(effect);
    }


    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/


    
    
    /**************************************************************************
     * 
     * Private Implementation
     * 
     **************************************************************************/
    
    // TODO we use this with the login dialog to change the content, but the longer
    // term solution is to automatically call sizeToScene on the FXDialog when
    // the content changes.
    void sizeToScene() {
        dialog.sizeToScene();
    }
    
    /**
     * TODO delete me - this is just for testing!!
     */
    static String buttonBarOrder = null;

    static void setMacOS(boolean b) {
        if (b) {
            buttonBarOrder = ButtonBar.BUTTON_ORDER_MAC_OS;
        }
    }

    static void setWindows(boolean b) {
        if (b) {
            buttonBarOrder = ButtonBar.BUTTON_ORDER_WINDOWS;
        }
    }
    
    static void setLinux(boolean b) {
        if (b) {
            buttonBarOrder = ButtonBar.BUTTON_ORDER_LINUX;
        }
    }
    
    // -- end of testing code
    
    

    private boolean hasMasthead() {
        return getMasthead() != null;
    }

    private boolean hasExpandableContent() {
        return getExpandableContent() != null;
    }

    private void updateDialogContent() {
        this.contentPane = new GridPane();
        this.contentPane.getStyleClass().add("content-pane"); //$NON-NLS-1$
        this.contentPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        int row = 0;
        
        final boolean hasMasthead = hasMasthead();
        if (hasMasthead) {
            Node masthead = getMasthead();
            contentPane.add(masthead, 0, row++);
        }
        
        createCenterPanel(row);
        
        Node root = dialog.getRoot();
        root.pseudoClassStateChanged(MASTHEAD_PSEUDO_CLASS,      hasMasthead);
        root.pseudoClassStateChanged(NO_MASTHEAD_PSEUDO_CLASS,   !hasMasthead);
        
        // the dialog has a simple grid layout. If there is a masthead, it consists
        // of a single column. If there is no masthead, there are two columns.
        // We always want the right-most column to expand to fill all available
        // width.
        ColumnConstraints leftColumn = new ColumnConstraints();
        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setFillWidth(true);
        rightColumn.setHgrow(Priority.ALWAYS);
        if (! hasMasthead) {
            contentPane.getColumnConstraints().add(leftColumn);
        }
        contentPane.getColumnConstraints().add(rightColumn);
        
        // now that all the content is built, we apply the min size rules
        if (! contentPane.minWidthProperty().isBound()) {
            contentPane.setMinWidth(MIN_DIALOG_WIDTH);
        }
        
        this.contentPane.setGridLinesVisible(DEBUG);
        this.dialog.setContentPane(contentPane);
    }

    private void createCenterPanel(final int startRow) {
        final boolean hasMasthead = hasMasthead();
        
        final Node content = getContent();
        if (content != null) {
            content.getStyleClass().add("center"); //$NON-NLS-1$
            
            contentPane.add(content, hasMasthead ? 0 : 1, startRow);
            GridPane.setVgrow(content, Priority.SOMETIMES);
            GridPane.setValignment(content, VPos.TOP);
            GridPane.setHgrow(content, Priority.ALWAYS);
            
            if (content instanceof Region) {
                contentPane.minWidthProperty().bind(new DoubleBinding() {
                    {
                        bind(((Region)content).minWidthProperty());
                    }
                    
                    @Override protected double computeValue() {
                        return Math.max(MIN_DIALOG_WIDTH, ((Region) content).getMinWidth());
                    }
                });
            }
        }
        
        // dialog image can go to the left if there is no masthead
        final Node graphic = getGraphic();
        if (!hasMasthead && graphic != null) {
            Pane graphicPane = new Pane(graphic);
            
            final double w = graphic.getLayoutBounds().getWidth();
            final double h = graphic.getLayoutBounds().getHeight();
            graphicPane.setMinSize(w, h);
            graphicPane.getStyleClass().add("graphic"); //$NON-NLS-1$
            contentPane.add(graphicPane, 0, startRow);
            GridPane.setValignment(graphicPane, VPos.TOP);
            GridPane.setMargin(graphicPane, new Insets(0,8,0,0));
        }
        
        if (hasExpandableContent()) {
            Node ec = getExpandableContent();
            ec.getStyleClass().add("expandable-content"); //$NON-NLS-1$
            
            contentPane.add(ec, 0, startRow + 1, 2, 1);
            ec.setVisible(false);
            ec.managedProperty().bind(ec.visibleProperty());
            
            GridPane.setHgrow(ec, Priority.ALWAYS);
            GridPane.setVgrow(ec, Priority.ALWAYS);
        }
        
        if ( !getActions().isEmpty() || hasExpandableContent()) {
            createButtonPanel(startRow + 2);
       }
    }

    private void createButtonPanel(final int startRow) {
        ButtonBar buttonBar = new ButtonBar();
        if (buttonBarOrder != null) {
            buttonBar.setButtonOrder(buttonBarOrder);
        }
        
        // show details button if expandable content is present
        if (hasExpandableContent()) {
            buttonBar.addSizeIndependentButton(createDetailsButton(), ButtonType.HELP_2);
        }

        List<ButtonBase> buttons = new ArrayList<>();
        boolean hasDefault = false;
        for (Action cmd : getActions()) {
            ButtonBase b = createButton(cmd, !hasDefault);
            
            // keep only first default button
            if (b instanceof Button) {
                hasDefault |= ((Button) b).isDefaultButton();
            }
            buttons.add(b);
        }
        
        buttonBar.getButtons().addAll(buttons);
        
        contentPane.add(buttonBar, 0, startRow, 2, 1);
        GridPane.setHgrow(buttonBar, Priority.ALWAYS);
        GridPane.setVgrow(buttonBar, Priority.NEVER);
        GridPane.setMargin(buttonBar, new Insets(14, 0, 14, 0));
//        GridPane.setValignment(buttonBar, VPos.BASELINE);
    }
    
    private Hyperlink createDetailsButton() {
        final Hyperlink detailsButton = new Hyperlink();
        detailsButton.getStyleClass().setAll("details-button", "more"); //$NON-NLS-1$ //$NON-NLS-2$
        final String moreText = Localization.getString("dlg.detail.button.more"); //$NON-NLS-1$
        final String lessText = Localization.getString("dlg.detail.button.less"); //$NON-NLS-1$
        
        detailsButton.setText(moreText);
        detailsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                final Node content = getExpandableContent();
                setResizable(!content.isVisible());
                content.setVisible(!content.isVisible());
                detailsButton.setText(content.isVisible() ? lessText : moreText);
                detailsButton.getStyleClass().setAll("details-button", (content.isVisible() ? "less" : "more")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                dialog.sizeToScene();
            }
        });
        return detailsButton;
    }

    private Button createButton(final Action action, boolean keepDefault) {
        final Button button = ActionUtils.createButton(action);
        
        if (action instanceof DialogAction) {
            DialogAction dlgAction = (DialogAction) action;
            button.setDefaultButton(keepDefault && dlgAction.isDefault());
            button.setCancelButton(dlgAction.isCancel());
        }
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                action.handle(new ActionEvent(Dialog.this, ae.getTarget()));
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
            PseudoClass.getPseudoClass("masthead"); //$NON-NLS-1$
    private static final PseudoClass NO_MASTHEAD_PSEUDO_CLASS = 
            PseudoClass.getPseudoClass("no-masthead"); //$NON-NLS-1$
}
