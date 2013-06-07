package org.controlsfx.dialog;

import java.net.URL;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

class LightweightDialog extends FXDialog {

    private static final URL DIALOGS_CSS_URL = HeavyweightDialog.class.getResource("dialogs.css");   

    private BorderPane root;
    private StackPane decoratedRoot;
    private HBox windowBtns;
    private Button closeButton;
    private Button minButton;
    private Button maxButton;
    private Rectangle resizeCorner;
    private double mouseDragOffsetX = 0;
    private double mouseDragOffsetY = 0;
    protected Label titleLabel;
    
    private final Scene scene;
    private Region opaqueLayer;
    private StackPane dialogStack;
    private Parent originalParent;

    private static final int HEADER_HEIGHT = 28;

    LightweightDialog(String title, Scene scene) {
        this.scene = scene;
        
        setTitle(title);
        
        resizableProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {
//                resizeCorner.setVisible(isResizable());
//                maxButton.setVisible(isResizable());
//
//                if (isResizable()) {
//                    windowBtns.getChildren().add(1, maxButton);
//                } else {
//                    windowBtns.getChildren().remove(maxButton);
//                }
            }
        });

        root = new BorderPane();




        // *** The rest is for adding window decorations ***

        decoratedRoot = new StackPane() {
            @Override protected void layoutChildren() {
                super.layoutChildren();
                if (resizeCorner != null) {
                    resizeCorner.relocate(getWidth() - 20, getHeight() - 20);
                }
            }
        };
        decoratedRoot.getChildren().add(root);
        decoratedRoot.getStyleClass().addAll("dialog", "decorated-root");

//        focusedProperty().addListener(new InvalidationListener() {
//            @Override public void invalidated(Observable valueModel) {                
//                boolean active = ((ReadOnlyBooleanProperty)valueModel).get();
//                decoratedRoot.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, active);
//            }
//        });

        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("window-header");
        toolBar.setPrefHeight(HEADER_HEIGHT);
        toolBar.setMinHeight(HEADER_HEIGHT);
        toolBar.setMaxHeight(HEADER_HEIGHT);

        // add window dragging
        toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                mouseDragOffsetX = event.getSceneX();
                mouseDragOffsetY = event.getSceneY();
            }
        });
        toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
//                setLayoutX(event.getScreenX() - mouseDragOffsetX);
//                setLayoutY(event.getScreenY() - mouseDragOffsetY);
            }
        });

        titleLabel = new Label();
        titleLabel.getStyleClass().add("window-title");
        titleLabel.setText(getTitle());

        titleProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {
                titleLabel.setText(getTitle());
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // add close min max
        closeButton = new WindowButton("close");
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                LightweightDialog.this.hide();
            }
        });
        minButton = new WindowButton("minimize");
        minButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
//                setIconified(!isIconified());
            }
        });

        maxButton = new WindowButton("maximize");
        maxButton.setOnAction(new EventHandler<ActionEvent>() {
            private double restoreX;
            private double restoreY;
            private double restoreW;
            private double restoreH;

            @Override public void handle(ActionEvent event) {
                Screen screen = Screen.getPrimary(); // todo something more sensible
                double minX = screen.getVisualBounds().getMinX();
                double minY = screen.getVisualBounds().getMinY();
                double maxW = screen.getVisualBounds().getWidth();
                double maxH = screen.getVisualBounds().getHeight();

//                if (restoreW == 0 || getLayoutX() != minX || getLayoutY() != minY || getWidth() != maxW || getHeight() != maxH) {
//                    restoreX = getLayoutX();
//                    restoreY = getLayoutY();
//                    restoreW = getWidth();
//                    restoreH = getHeight();
//                    setLayoutX(minX);
//                    setLayoutY(minY);
//                    setWidth(maxW);
//                    setHeight(maxH);
//                } else {
//                    setLayoutX(restoreX);
//                    setLayoutY(restoreY);
//                    setWidth(restoreW);
//                    setHeight(restoreH);
//                }
            }
        });

        windowBtns = new HBox(3);
        windowBtns.getStyleClass().add("window-buttons");
        windowBtns.getChildren().addAll(minButton, maxButton, closeButton);

        toolBar.getItems().addAll(titleLabel, spacer, windowBtns);
        root.setTop(toolBar);

        resizeCorner = new Rectangle(10, 10);
        resizeCorner.getStyleClass().add("window-resize-corner");

        // add window resizing
        EventHandler<MouseEvent> resizeHandler = new EventHandler<MouseEvent>() {
            private double width;
            private double height;
            private Point2D dragAnchor;

            @Override public void handle(MouseEvent event) {
                EventType<? extends MouseEvent> type = event.getEventType();

//                if (type == MouseEvent.MOUSE_PRESSED) {
//                    width = getWidth();
//                    height = getHeight();
//                    dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
//                } else if (type == MouseEvent.MOUSE_DRAGGED) {
//                    setWidth(Math.max(decoratedRoot.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
//                    setHeight(Math.max(decoratedRoot.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
//                }
            }
        };
        resizeCorner.setOnMousePressed(resizeHandler);
        resizeCorner.setOnMouseDragged(resizeHandler);

        resizeCorner.setManaged(false);
        decoratedRoot.getChildren().add(resizeCorner);
        
        decoratedRoot.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        decoratedRoot.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);
        
//        getChildren().add(decoratedRoot);
    }
    
//    @Override public void showAndWait() {
//        Window owner = getOwner();
//        if (owner != null) {
//            // because Stage does not seem to centre itself over its owner, we
//            // do it here.
//            final double x = owner.getX() + (owner.getWidth() / 2.0) - (root.prefWidth(-1) / 2.0);
//            final double y = owner.getY() + (owner.getHeight() / 2.0) - (root.prefHeight(-1)) / 2.0 - 50;
//            setX(x);
//            setY(y);
//        }
//        super.showAndWait();
//    }

    public void setIconifiable(boolean iconifiable) {
        minButton.setVisible(iconifiable);
    }
    
    public void setClosable( boolean closable ) {
        closeButton.setVisible( closable );
    }
    
    
    
    
    private StringProperty title = new SimpleStringProperty(this, "title");
    public StringProperty titleProperty() {
        return title;
    }
    
    public final void setTitle(String value) {
        title.set(value);
    }
    
    public final String getTitle() {
        return title.get();
    }
    
    @Override
    public void show() {
        // opaque layer
        opaqueLayer = new Region();
        opaqueLayer.setStyle("-fx-background-color: #00000044");
        
        // install CSS
        scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
        
        // modify scene root to install opaque layer and the dialog
        originalParent = scene.getRoot();
        dialogStack = new StackPane(originalParent, opaqueLayer, decoratedRoot);
        decoratedRoot.setVisible(true);
        scene.setRoot(dialogStack);
    }
    
    @Override public void hide() {
        opaqueLayer.setVisible(false);
        decoratedRoot.setVisible(false);
        
        dialogStack.getChildren().remove(originalParent);
        scene.setRoot(originalParent);
    }

    private BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable", false);
    @Override BooleanProperty resizableProperty() {
        return resizable;
    }

    @Override public Node getRoot() {
        return decoratedRoot;
    }

    @Override
    ReadOnlyDoubleProperty widthProperty() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    ReadOnlyDoubleProperty heightProperty() {
        // TODO Auto-generated method stub
        return null;
    }    
    
    @Override
    public void setContentPane(Pane pane) {
        root.setCenter(pane);        
    }
    
    @Override public void sizeToScene() {
        // TODO Auto-generated method stub
    }
    
    
    
    

    private static class WindowButton extends Button {
        WindowButton(String name) {
            getStyleClass().setAll("window-button");
            getStyleClass().add("window-"+name+"-button");
            StackPane graphic = new StackPane();
            graphic.getStyleClass().setAll("graphic");
            setGraphic(graphic);
            setMinSize(17, 17);
            setPrefSize(17, 17);
        }
    }

    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

   
}
