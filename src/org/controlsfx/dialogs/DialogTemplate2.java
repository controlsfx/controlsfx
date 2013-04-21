package org.controlsfx.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import com.sun.javafx.Utils;

public class DialogTemplate2 {

    // According to the UI spec, the width of the main message text in the upper
    // panel should be 426 pixels.
    private static int MAIN_TEXT_WIDTH = 400;

    // Specifies the minimum allowable width for all buttons in the dialog
    private static int MINIMUM_BUTTON_WIDTH = 75;

    private final FXDialog dialog;

    // Dialog result. By default set to CLOSE
    private Action result = DialogAction.CLOSE;

    private final BorderPane contentPane;
    

    public DialogTemplate2(Stage owner, String title) {
        this.dialog = new FXDialog(title, owner, true);
        this.contentPane = new BorderPane();
        contentPane.setPrefWidth(MAIN_TEXT_WIDTH);
        this.dialog.setContentPane(contentPane);
    }

    public void show() {
        buildDialogContent();
        dialog.showAndWait();
    }

    public void hide() {
        dialog.hide();
    }

    public Action getResult() {
        return result;
    }
    
    protected FXDialog getDialog() {
        return dialog;
    }


    // Resizable property 
    
    public boolean isResizable() {
        return dialog.isResizable();
    }
    
    public void setResizable( boolean resizable ) {
        dialog.setResizable(resizable);
    }
    
    public BooleanProperty resizableProperty() { return dialog.resizableProperty(); }
    

    // Resizable property 
    
    private final ObjectProperty<Image> iconProperty = new SimpleObjectProperty<Image>(); 
    
    public Image getIcon() {
        return iconProperty.get();
    }
    
    public void setIcon( Image icon ) {
        this.iconProperty.set(icon);
    }
    
    public ObjectProperty<Image> iconProperty() { return iconProperty; }
    
    
    
    // Masthead property

    private final ObjectProperty<Node> masthead = new SimpleObjectProperty<Node>();

    public final Node getMasthead() {
        return masthead.get();
    }
    
    public final void setMasthead( Node masthead ) {
        this.masthead.setValue(masthead);
    }

    public final void setMasthead( String mastheadText) {
        
        if ( mastheadText == null ) return;
        
        BorderPane mastheadPanel = new BorderPane();
        mastheadPanel.getStyleClass().add("top-panel");

        // Create panel with text area and icon or just a background image:
        // Create topPanel's components. UITextArea determines
        // the size of the dialog by defining the number of columns
        // based on font size.
        Label mastheadTextArea = new Label();
        mastheadTextArea.setPrefWidth(MAIN_TEXT_WIDTH);
        mastheadTextArea.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        mastheadTextArea.setWrapText(true);
        mastheadTextArea.getStyleClass().add("masthead-label-1");

        VBox mastheadVBox = new VBox();
        mastheadVBox.setAlignment(Pos.CENTER_LEFT);
        mastheadTextArea.setText(mastheadText);
        mastheadTextArea.setAlignment(Pos.CENTER_LEFT);
        mastheadVBox.getChildren().add(mastheadTextArea);

        mastheadPanel.setLeft(mastheadVBox);
        BorderPane.setAlignment(mastheadVBox, Pos.CENTER_LEFT);
        
        if ( iconProperty.get() != null ) {
            mastheadPanel.setRight(new ImageView(iconProperty.get()));
        }

        setMasthead(mastheadPanel);
    }
    
    public ObjectProperty<Node> mastheadProperty() { return masthead; }    
    

    // Content property

    private final ObjectProperty<Node> content = new SimpleObjectProperty<Node>();

    public final Node getContent() {
        return content.get();
    }
    
    public final void setContent( Node content ) {
        this.content.setValue(content);
    }
    

    public final void setContent( String contentText) {
        
        if ( contentText == null ) return;
        
        Label label = new Label(contentText);
        label.getStyleClass().add("center-content-area");
        label.setAlignment(Pos.TOP_LEFT);
        label.setTextAlignment(TextAlignment.CENTER);
//        label.setStyle("-fx-border-color: red;");

        // FIXME we don't want to restrict the width, but for now this works ok
        label.setPrefWidth(MAIN_TEXT_WIDTH);
        label.setMaxWidth(360);
        label.setWrapText(true);

        setContent(label);
    }
    
    public ObjectProperty<Node> contentProperty() { return content; }        
    
    
    //Actions     
    
    private final ObservableList<Action> actions = FXCollections.<Action>observableArrayList();  
    
    public final ObservableList<Action> getActions() {
        return actions;
    }
    
    public interface Action {
        String getText();
        void execute(DialogTemplate2 template);
    }

    public enum DialogAction implements Action {

        CANCEL("Cancel", true, true),
        CLOSE("Close", true, true),
        NO("No", true, true),
        OK("Ok", true, false),
        YES("Yes", true, false);

        private String title;
        private boolean isClosing;
        private boolean isDefault;
        private boolean isCancel;

        DialogAction(String title, boolean isDefault, boolean isCancel, boolean isClosing) {
            this.title = title;
            this.isClosing = isClosing;
            this.isDefault = isDefault;
            this.isCancel = isCancel;
        }

        DialogAction(String title, boolean isDefault, boolean isCancel) {
            this(title, isDefault, isCancel, true);
        }

        @Override public String getText() {
            return title;
        }

        public boolean isClosing() {
            return isClosing;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public boolean isCancel() {
            return isCancel;
        }

        public void execute(DialogTemplate2 template) {
            if (isClosing())
                template.hide();
        }

    }

    // ///// PRIVATE API ///////////////////////////////////////////////////////////////////

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

//    private boolean isWindows() {
//        return isWindows || (!isMac && Utils.isWindows());
//    }

    private boolean isMac() {
        return isMac || (!isWindows && Utils.isMac());
    }

//    private boolean isUnix() {
//        return Utils.isUnix();
//    }    
    

    protected boolean isMastheadPresent() {
        return getMasthead() != null;//mastheadBuilder != null;
    }

    protected void buildDialogContent() {
        contentPane.getChildren().clear();
        if (isMastheadPresent()) {
            contentPane.setTop(getMasthead());//mastheadBuilder.build());
        }
        Pane centerPanel = createCenterPanel( getContent() != null ? getContent() : new Pane() );
        contentPane.setCenter(centerPanel);
        centerPanel.getChildren().add(createButtonPanel());
    }    
    
    private Node createButtonPanel() {

        HBox buttonsPanel = new HBox(6);
        buttonsPanel.getStyleClass().add("button-bar");

        // push buttons to the right
        buttonsPanel.getChildren().add(createButtonSpacer());

        List<ButtonBase> buttons = new ArrayList<ButtonBase>();
        double widest = MINIMUM_BUTTON_WIDTH;
        boolean hasDefault = false;
        for (Action cmd : actions) {
            Button b = createButton(cmd, !hasDefault);
            // keep only first default button
            hasDefault |= b.isDefaultButton();
            widest = Math.max(widest, b.prefWidth(-1));
            buttons.add(b);
        }

        // OS based order of buttons
        if ( isMac()) Collections.reverse(buttons);
        
        for (ButtonBase button : buttons) {
            button.setPrefWidth(button.isVisible() ? widest : 0);
            buttonsPanel.getChildren().add(button);
        }
        
        // if (isWindows() || isUnix()) {
        // } else if (isMac()) {
        // }

        return buttonsPanel;
    }

    private Node createButtonSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Button createButton(final Action command, boolean keepDefault) {
        Button button = new Button(command.getText());
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                command.execute(DialogTemplate2.this);
                result = command;
            }
        });
        if (command instanceof DialogAction) {
            DialogAction stdCommand = (DialogAction) command;
            button.setDefaultButton(stdCommand.isDefault() && keepDefault);
            button.setCancelButton(stdCommand.isCancel());
        }
        return button;
    }

    private Pane createCenterPanel(Node content) {
        VBox centerPanel = new VBox();
        centerPanel.getStyleClass().add("center-panel");

        BorderPane contentPanel = new BorderPane();
        contentPanel.getStyleClass().add("center-content-panel");
        VBox.setVgrow(contentPanel, Priority.ALWAYS);

        if (content != null) {
            contentPanel.setCenter(content);
            //contentPanel.setPadding(new Insets(0, 0, 12, 0));
        }

        if (contentPanel.getChildren().size() > 0) {
            centerPanel.getChildren().add(contentPanel);
        }

        // dialog image can go to the left if there is no masthead
        if (!isMastheadPresent() && iconProperty != null ) {
             ImageView dialogBigIcon = new ImageView(iconProperty.get());
             Pane pane = new Pane(dialogBigIcon);
             pane.setPadding(new Insets(12, 12, 12, 12));
             contentPanel.setLeft(pane);
        }

        return centerPanel;
    }


}
