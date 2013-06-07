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
    private HBox windowBtns;
    private Button closeButton;
    private Button minButton;
    private Button maxButton;
    private Rectangle resizeCorner;
    private double mouseDragDeltaX = 0;
    private double mouseDragDeltaY = 0;
    protected Label titleLabel;
    
    private final Scene scene;
    private Region opaqueLayer;
    private Pane dialogStack;
    private Parent originalParent;
    private StackPane lightweightDialog;

    private static final int HEADER_HEIGHT = 28;

    LightweightDialog(String title, Scene scene) {
        this.scene = scene;
        
        setTitle(title);
        
        resizableProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {
                resizeCorner.setVisible(resizableProperty().get());
                maxButton.setVisible(resizableProperty().get());

                if (resizableProperty().get()) {
                    if (! windowBtns.getChildren().contains(maxButton)) {
                        windowBtns.getChildren().add(1, maxButton);
                    }
                } else {
                    windowBtns.getChildren().remove(maxButton);
                }
            }
        });

        root = new BorderPane();




        // *** The rest is for adding window decorations ***

        lightweightDialog = new StackPane() {
            @Override protected void layoutChildren() {
                super.layoutChildren();
                if (resizeCorner != null) {
                    resizeCorner.relocate(getWidth() - 20, getHeight() - 20);
                }
            }
        };
        lightweightDialog.getChildren().add(root);
        lightweightDialog.getStyleClass().addAll("dialog", "decorated-root");

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
                mouseDragDeltaX = lightweightDialog.getLayoutX() - event.getSceneX();
                mouseDragDeltaY = lightweightDialog.getLayoutY() - event.getSceneY();
            }
        });
        toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                lightweightDialog.setLayoutX(event.getSceneX() + mouseDragDeltaX);
                lightweightDialog.setLayoutY(event.getSceneY() + mouseDragDeltaY);
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
        maxButton.setVisible(false);
        maxButton.setOnAction(new EventHandler<ActionEvent>() {
            private double restoreX;
            private double restoreY;
            private double restoreW;
            private double restoreH;

            @Override public void handle(ActionEvent event) {
                // TODO redo this code - we don't want to use Screen here
                Screen screen = Screen.getPrimary(); 
                double minX = screen.getVisualBounds().getMinX();
                double minY = screen.getVisualBounds().getMinY();
                double maxW = screen.getVisualBounds().getWidth();
                double maxH = screen.getVisualBounds().getHeight();

                final double layouxX = lightweightDialog.getLayoutX();
                final double layouxY = lightweightDialog.getLayoutY();
                final double width = lightweightDialog.getWidth();
                final double height = lightweightDialog.getHeight();
                
                if (restoreW == 0 || layouxX != minX || layouxY != minY || width != maxW || height != maxH) {
                    restoreX = layouxX;
                    restoreY = layouxY;
                    restoreW = width;
                    restoreH = height;
                    lightweightDialog.setLayoutX(minX);
                    lightweightDialog.setLayoutY(minY);
                    lightweightDialog.setPrefWidth(maxW);
                    lightweightDialog.setPrefHeight(maxH);
                } else {
                    lightweightDialog.setLayoutX(restoreX);
                    lightweightDialog.setLayoutY(restoreY);
                    lightweightDialog.setPrefWidth(restoreW);
                    lightweightDialog.setPrefHeight(restoreH);
                }
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
                
                if (type == MouseEvent.MOUSE_PRESSED) {
                    width = lightweightDialog.getWidth();
                    height = lightweightDialog.getHeight();
                    dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
                } else if (type == MouseEvent.MOUSE_DRAGGED) {
                    lightweightDialog.setPrefWidth(Math.max(lightweightDialog.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
                    lightweightDialog.setPrefHeight(Math.max(lightweightDialog.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
                }
            }
        };
        resizeCorner.setOnMousePressed(resizeHandler);
        resizeCorner.setOnMouseDragged(resizeHandler);

        resizeCorner.setManaged(false);
        lightweightDialog.getChildren().add(resizeCorner);
        
        lightweightDialog.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        lightweightDialog.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);
    }
    
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
        dialogStack = new Pane(originalParent, opaqueLayer, lightweightDialog) {
            protected void layoutChildren() {
                final double w = dialogStack.getWidth();
                final double h = dialogStack.getHeight();
                opaqueLayer.resizeRelocate(0, 0, w, h);
                
                final double dialogWidth = lightweightDialog.prefWidth(-1);
                final double dialogHeight = lightweightDialog.prefHeight(-1);
                
                double dialogX = lightweightDialog.getLayoutX();
                dialogX = dialogX == 0.0 ? w/2.0-dialogWidth/2.0 : dialogX;
                
                double dialogY = lightweightDialog.getLayoutY();
                dialogY = dialogY == 0.0 ? h/2.0-dialogHeight/2.0 : dialogY;
                
                lightweightDialog.relocate(dialogX, dialogY);
                lightweightDialog.resize(dialogWidth, dialogHeight);
            }
        };
        lightweightDialog.setVisible(true);
        scene.setRoot(dialogStack);
    }
    
    @Override public void hide() {
        opaqueLayer.setVisible(false);
        lightweightDialog.setVisible(false);
        
        dialogStack.getChildren().remove(originalParent);
        originalParent.getStyleClass().remove("root");
        
        scene.setRoot(originalParent);
    }

    private BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable", false);
    @Override BooleanProperty resizableProperty() {
        return resizable;
    }

    @Override public Node getRoot() {
        return lightweightDialog;
    }

    @Override ReadOnlyDoubleProperty widthProperty() {
        return lightweightDialog.widthProperty();
    }

    @Override ReadOnlyDoubleProperty heightProperty() {
        return lightweightDialog.heightProperty();
    }    
    
    @Override
    public void setContentPane(Pane pane) {
        root.setCenter(pane);        
    }
    
    @Override public void sizeToScene() {
        // no-op: This isn't needed when there is not stage...
    }
    
    
    
    

    static class WindowButton extends Button {
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
