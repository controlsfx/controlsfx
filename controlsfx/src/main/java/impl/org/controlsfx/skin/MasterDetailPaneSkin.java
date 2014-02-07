package impl.org.controlsfx.skin;

import static java.lang.Double.MAX_VALUE;
import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import org.controlsfx.control.MasterDetailPane;

public class MasterDetailPaneSkin extends SkinBase<MasterDetailPane> {

    private double lastDividerPosition;

    private SplitPane splitPane;
    private Timeline timeline;

    public MasterDetailPaneSkin(MasterDetailPane pane) {
        super(pane);

        this.lastDividerPosition = pane.getDividerPosition();

        this.splitPane = new SplitPane();
        this.splitPane.setDividerPosition(0, lastDividerPosition);

        SplitPane.setResizableWithParent(getSkinnable().getDetailNode(), false);

        switch (getSkinnable().getDetailPos()) {
        case BOTTOM:
        case TOP:
            splitPane.setOrientation(VERTICAL);
            break;
        case LEFT:
        case RIGHT:
            splitPane.setOrientation(HORIZONTAL);
            break;
        }

        getSkinnable().masterNodeProperty().addListener(
                new ChangeListener<Node>() {
                    @Override
                    public void changed(ObservableValue<? extends Node> value,
                            Node oldNode, Node newNode) {

                        if (oldNode != null) {
                            splitPane.getItems().remove(oldNode);
                        }

                        if (newNode != null) {

                            updateMinAndMaxSizes();

                            int masterIndex = 0;
                            switch (splitPane.getOrientation()) {
                            case HORIZONTAL:
                                switch (getSkinnable().getDetailPos()) {
                                case LEFT:
                                    masterIndex = 1;
                                    break;
                                case RIGHT:
                                    masterIndex = 0;
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "illegal details position "
                                                    + getSkinnable()
                                                            .getDetailPos()
                                                    + " for orientation "
                                                    + splitPane
                                                            .getOrientation());
                                }
                                break;
                            case VERTICAL:
                                switch (getSkinnable().getDetailPos()) {
                                case TOP:
                                    masterIndex = 1;
                                    break;
                                case BOTTOM:
                                    masterIndex = 0;
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "illegal details position "
                                                    + getSkinnable()
                                                            .getDetailPos()
                                                    + " for orientation "
                                                    + splitPane
                                                            .getOrientation());
                                }
                                break;
                            }
                            List<Node> items = splitPane.getItems();
                            if (items.isEmpty()) {
                                items.add(newNode);
                            } else {
                                items.add(masterIndex, newNode);
                            }
                        }
                    }
                });

        getSkinnable().detailNodeProperty().addListener(
                new ChangeListener<Node>() {
                    @Override
                    public void changed(ObservableValue<? extends Node> value,
                            Node oldNode, Node newNode) {

                        if (oldNode != null) {
                            splitPane.getItems().remove(oldNode);
                        }

                        if (newNode != null) {

                            updateMinAndMaxSizes();

                            SplitPane.setResizableWithParent(newNode, false);

                            int detailsIndex = 0;
                            switch (splitPane.getOrientation()) {
                            case HORIZONTAL:
                                switch (getSkinnable().getDetailPos()) {
                                case LEFT:
                                    detailsIndex = 0;
                                    break;
                                case RIGHT:
                                    detailsIndex = 1;
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "illegal details position "
                                                    + getSkinnable()
                                                            .getDetailPos()
                                                    + " for orientation "
                                                    + splitPane
                                                            .getOrientation());
                                }
                                break;
                            case VERTICAL:
                                switch (getSkinnable().getDetailPos()) {
                                case TOP:
                                    detailsIndex = 0;
                                    break;
                                case BOTTOM:
                                    detailsIndex = 1;
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "illegal details position "
                                                    + getSkinnable()
                                                            .getDetailPos()
                                                    + " for orientation "
                                                    + splitPane
                                                            .getOrientation());
                                }
                                break;
                            }
                            List<Node> items = splitPane.getItems();
                            if (items.isEmpty()) {
                                items.add(newNode);
                            } else {
                                items.add(detailsIndex, newNode);
                            }
                        }
                    }
                });

        getSkinnable().showDetailNodeProperty().addListener(
                new ChangeListener<Boolean>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Boolean> value,
                            Boolean oldShow, Boolean newShow) {
                        if (newShow) {
                            open();
                        } else {
                            close();
                        }
                    }
                });

        getSkinnable().detailPosProperty().addListener(
                new ChangeListener<Side>() {
                    @Override
                    public void changed(ObservableValue<? extends Side> value,
                            Side oldPos, Side newPos) {
                        if (getSkinnable().isShowDetailNode()) {
                            lastDividerPosition = splitPane.getDividers()
                                    .get(0).getPosition();
                            splitPane.getItems().clear();
                        }
                        switch (newPos) {
                        case TOP:
                        case BOTTOM:
                            splitPane.setOrientation(VERTICAL);
                            break;
                        case LEFT:
                        case RIGHT:
                            splitPane.setOrientation(HORIZONTAL);
                        }
                        switch (newPos) {
                        case TOP:
                        case LEFT:
                            if (getSkinnable().isShowDetailNode()) {
                                splitPane.getItems().add(
                                        getSkinnable().getDetailNode());
                                splitPane.getItems().add(
                                        getSkinnable().getMasterNode());
                            }
                            switch (oldPos) {
                            case BOTTOM:
                            case RIGHT:
                                lastDividerPosition = 1 - lastDividerPosition;
                                break;
                            default:
                                break;
                            }
                            break;
                        case BOTTOM:
                        case RIGHT:
                            if (getSkinnable().isShowDetailNode()) {
                                splitPane.getItems().add(
                                        getSkinnable().getMasterNode());
                                splitPane.getItems().add(
                                        getSkinnable().getDetailNode());
                            }
                            switch (oldPos) {
                            case TOP:
                            case LEFT:
                                lastDividerPosition = 1 - lastDividerPosition;
                                break;
                            default:
                                break;
                            }
                            break;
                        }
                        if (getSkinnable().isShowDetailNode()) {
                            splitPane.setDividerPositions(lastDividerPosition);
                        }
                    }
                });

        updateMinAndMaxSizes();

        getChildren().add(splitPane);

        splitPane.getItems().add(getSkinnable().getMasterNode());

        if (getSkinnable().isShowDetailNode()) {
            switch (getSkinnable().getDetailPos()) {
            case TOP:
            case LEFT:
                splitPane.getItems().add(0, getSkinnable().getDetailNode());
                break;
            case BOTTOM:
            case RIGHT:
                splitPane.getItems().add(getSkinnable().getDetailNode());
                break;
            }

            bindDividerPosition();
        }
    }

    private void bindDividerPosition() {
        final ObservableList<Divider> dividers = splitPane.getDividers();
        if (dividers.size() > 0) {
            Bindings.bindBidirectional(
                    getSkinnable().dividerPositionProperty(), dividers.get(0)
                            .positionProperty());
        }
    }

    private void unbindDividerPosition() {
        ObservableList<Divider> dividers = splitPane.getDividers();
        if (dividers.size() > 0) {
            Bindings.unbindBidirectional(getSkinnable()
                    .dividerPositionProperty(), dividers.get(0)
                    .positionProperty());
        }
    }

    private void updateMinAndMaxSizes() {
        if (getSkinnable().getMasterNode() instanceof Region) {
            ((Region) getSkinnable().getMasterNode()).setMinSize(0, 0);
            ((Region) getSkinnable().getMasterNode()).setMaxSize(MAX_VALUE,
                    MAX_VALUE);
        }

        if (getSkinnable().getDetailNode() instanceof Region) {
            ((Region) getSkinnable().getDetailNode()).setMinSize(0, 0);
            ((Region) getSkinnable().getDetailNode()).setMaxSize(MAX_VALUE,
                    MAX_VALUE);
        }
    }

    private void open() {
        Node node = getSkinnable().getDetailNode();

        switch (getSkinnable().getDetailPos()) {
        case TOP:
        case LEFT:
            splitPane.getItems().add(0, node);
            splitPane.setDividerPositions(0);
            break;
        case BOTTOM:
        case RIGHT:
            splitPane.getItems().add(node);
            splitPane.setDividerPositions(1);
            break;
        }

        maybeAnimatePositionChange(lastDividerPosition, true);
    }

    private void close() {
        if (!splitPane.getDividers().isEmpty()) {

            Divider divider = splitPane.getDividers().get(0);

            lastDividerPosition = divider.getPosition();

            /*
             * Do we collapse by moving the divider to the left/right or
             * top/bottom?
             */
            double targetLocation = 0;
            switch (getSkinnable().getDetailPos()) {
            case BOTTOM:
            case RIGHT:
                targetLocation = 1;
                break;
            default:
                break;
            }

            maybeAnimatePositionChange(targetLocation, false);
        }
    }

    private void maybeAnimatePositionChange(final double position,
            final boolean showDetail) {

        Divider divider = splitPane.getDividers().get(0);

        if (getSkinnable().isAnimated()) {
            if (showDetail) {
                unbindDividerPosition();
                bindDividerPosition();
            }

            KeyValue positionKeyValue = new KeyValue(
                    divider.positionProperty(), position);
            KeyValue opacityKeyValue = new KeyValue(getSkinnable()
                    .getDetailNode().opacityProperty(), showDetail ? 1 : 0);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(.1),
                    positionKeyValue, opacityKeyValue);
            timeline = new Timeline(keyFrame);
            timeline.setOnFinished(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent evt) {
                    if (!showDetail) {
                        unbindDividerPosition();
                        splitPane.getItems().remove(
                                getSkinnable().getDetailNode());
                    }
                }
            });
            timeline.play();
        } else {
            if (showDetail) {
                unbindDividerPosition();
                bindDividerPosition();
            }
            getSkinnable().getDetailNode().setOpacity(1);
            divider.setPosition(position);

            if (!showDetail) {
                unbindDividerPosition();
                splitPane.getItems().remove(getSkinnable().getDetailNode());
            }
        }
    }
}
