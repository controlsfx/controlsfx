package org.controlsfx.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DialogTemplate2 {

    // According to the UI spec, the width of the main message text in the upper
    // panel should be 426 pixels.
    private static int MAIN_TEXT_WIDTH = 400;

    // Specifies the minimum allowable width for all buttons in the dialog
    private static int MINIMUM_BUTTON_WIDTH = 75;

    private final FXDialog dialog;

    private Action result = DialogAction.CLOSE;

    private final BorderPane contentPane;
    private NodeBuilder mastheadBuilder;
    private NodeBuilder contentBuilder;
    private List<? extends Action> commands = Arrays.asList(DialogAction.CLOSE);

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

    protected boolean isMastheadPresent() {
        return mastheadBuilder != null;
    }

    protected void buildDialogContent() {
        if (isMastheadPresent()) {
            contentPane.setTop(mastheadBuilder.build());
        }
        Node content = contentBuilder != null ? contentBuilder.build() : new Pane();
        Pane centerPanel = createCenterPanel(content);
        contentPane.setCenter(centerPanel);
        centerPanel.getChildren().add(createButtonPanel());
    }

    public DialogTemplate2 resizable(boolean resizable) {
        dialog.setResizable(resizable);
        return this;
    }

    public DialogTemplate2 masthead(Node masthead) {
        this.mastheadBuilder = new MasteheadBuilder(masthead);
        return this;
    }

    public DialogTemplate2 masthead(String masthead) {
        this.mastheadBuilder = new MasteheadBuilder(masthead);
        return this;
    }

    public DialogTemplate2 content(Node message) {
        this.contentBuilder = new ContentBuilder(message);
        return this;
    }

    public DialogTemplate2 content(String message) {
        this.contentBuilder = new ContentBuilder(message);
        return this;
    }

    public DialogTemplate2 commands(List<? extends Action> commands) {
        this.commands = new ArrayList<Action>(commands);
        return this;
    }

    public DialogTemplate2 commands(Action... commands) {
        return commands(Arrays.asList(commands));
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

    private Node createButtonPanel() {

        HBox buttonsPanel = new HBox(6);
        buttonsPanel.getStyleClass().add("button-bar");

        // push buttons to the right
        buttonsPanel.getChildren().add(createButtonSpacer());

        List<ButtonBase> buttons = new ArrayList<ButtonBase>();
        double widest = MINIMUM_BUTTON_WIDTH;
        boolean hasDefault = false;
        for (Action cmd : commands) {
            Button b = createButton(cmd, !hasDefault);
            // keep only first default button
            hasDefault |= b.isDefaultButton();
            widest = Math.max(widest, b.prefWidth(-1));
            buttons.add(b);
        }

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

        // Node content = createCenterContent();
        if (content != null) {
            contentPanel.setCenter(content);
            contentPanel.setPadding(new Insets(0, 0, 12, 0));
        }

        if (contentPanel.getChildren().size() > 0) {
            centerPanel.getChildren().add(contentPanel);
        }

        // OS-specific button positioning
        // Node buttonPanel = createButtonPanel();
        // centerPanel.getChildren().add(buttonPanel);
        //
        // dialog image can go to the left if there is no masthead
        if (!isMastheadPresent()) {
            // ImageView dialogBigIcon = new ImageView(
            // dialogType == null ? DialogResources
            // .getImage("java48.image") : dialogType.getImage());
            // Pane pane = new Pane(dialogBigIcon);
            // contentPanel.setLeft(pane);
        }

        return centerPanel;
    }

    private static abstract class NodeBuilder {

        private Node node;
        private String text;

        public NodeBuilder(Node node) {
            this.node = node;
        }

        public NodeBuilder(String text) {
            this.text = text;
        }

        public final Node build() {
            return node == null ? buildFromString(text) : node;
        }

        protected abstract Node buildFromString(String text);

    }

    private static class MasteheadBuilder extends NodeBuilder {

        public MasteheadBuilder(Node node) {
            super(node);
        }

        public MasteheadBuilder(String text) {
            super(text);
        }

        @Override protected Node buildFromString(String text) {

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
            mastheadTextArea.setText(text);
            mastheadTextArea.setAlignment(Pos.CENTER_LEFT);
            mastheadVBox.getChildren().add(mastheadTextArea);

            mastheadPanel.setLeft(mastheadVBox);
            BorderPane.setAlignment(mastheadVBox, Pos.CENTER_LEFT);
            // ImageView dialogBigIcon = new ImageView(
            // dialogType == null ? DialogResources.getImage("java48.image")
            // : dialogType.getImage());
            // mastheadPanel.setRight(dialogBigIcon);

            return mastheadPanel;
        }

    }

    private static class ContentBuilder extends NodeBuilder {

        public ContentBuilder(Node node) {
            super(node);
        }

        public ContentBuilder(String text) {
            super(text);
        }

        @Override protected Node buildFromString(String text) {

            Label label = new Label(text);
            label.getStyleClass().add("center-content-area");
            label.setAlignment(Pos.TOP_LEFT);

            // FIXME we don't want to restrict the width, but for now this works ok
            label.setPrefWidth(MAIN_TEXT_WIDTH);
            label.setMaxWidth(360);
            label.setWrapText(true);

            return label;
        }

    }

}
