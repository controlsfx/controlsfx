/**
 * Copyright (c) 2014 - 2016 ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
package impl.org.controlsfx.skin;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.controlsfx.control.MasterDetailPane;

import java.util.List;

import static java.lang.Double.MAX_VALUE;
import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;

public class MasterDetailPaneSkin extends SkinBase<MasterDetailPane> {

    private boolean changing = false;
    private SplitPane splitPane;
    private final Timeline timeline = new Timeline();
    private BooleanProperty showDetailForTimeline = new SimpleBooleanProperty();

    public MasterDetailPaneSkin(MasterDetailPane pane) {
        super(pane);

        this.splitPane = new SplitPane();
        this.splitPane.setDividerPosition(0, pane.getDividerPosition());

        /**
         * We listen to the change of dividers (when adding or removing node), and then
         * we listen to their position to update correctly the dividerPosition of 
         * the MasterDetailPane.
         */
        this.splitPane.getDividers().addListener((ListChangeListener.Change<? extends Divider> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().get(0).positionProperty().addListener(updateDividerPositionListener);
                } else if (change.wasRemoved()) {
                    change.getRemoved().get(0).positionProperty().removeListener(updateDividerPositionListener);
                }
            }
        });

        SplitPane.setResizableWithParent(getSkinnable().getDetailNode(), false);

        switch (getSkinnable().getDetailSide()) {
            case BOTTOM:
            case TOP:
                splitPane.setOrientation(VERTICAL);
                break;
            case LEFT:
            case RIGHT:
                splitPane.setOrientation(HORIZONTAL);
                break;
        }

        getSkinnable().masterNodeProperty().addListener((observable, oldNode, newNode) -> {

            if (oldNode != null) {
                splitPane.getItems().remove(oldNode);
            }

            if (newNode != null) {

                updateMinAndMaxSizes();

                int masterIndex = 0;
                switch (splitPane.getOrientation()) {
                    case HORIZONTAL:
                        switch (getSkinnable().getDetailSide()) {
                            case LEFT:
                                masterIndex = 1;
                                break;
                            case RIGHT:
                                masterIndex = 0;
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "illegal details position " //$NON-NLS-1$
                                                + getSkinnable()
                                                .getDetailSide()
                                                + " for orientation " //$NON-NLS-1$
                                                + splitPane
                                                .getOrientation());
                        }
                        break;
                    case VERTICAL:
                        switch (getSkinnable().getDetailSide()) {
                            case TOP:
                                masterIndex = 1;
                                break;
                            case BOTTOM:
                                masterIndex = 0;
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "illegal details position " //$NON-NLS-1$
                                                + getSkinnable()
                                                .getDetailSide()
                                                + " for orientation " //$NON-NLS-1$
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
        });

        getSkinnable().detailNodeProperty().addListener((observable, oldNode, newNode) -> {

            if (oldNode != null) {
                splitPane.getItems().remove(oldNode);
            }

            /**
             * If the detailNode is not showing, we do not force
             * it to show.
             */
            if (newNode != null && getSkinnable().isShowDetailNode()) {

                /**
                 * Force the divider to take the value of the Pane,
                 * and not compute his.
                 */
                splitPane.setDividerPositions(getSkinnable().getDividerPosition());
                updateMinAndMaxSizes();

                SplitPane.setResizableWithParent(newNode, false);

                int detailsIndex = 0;
                switch (splitPane.getOrientation()) {
                    case HORIZONTAL:
                        switch (getSkinnable().getDetailSide()) {
                            case LEFT:
                                detailsIndex = 0;
                                break;
                            case RIGHT:
                                detailsIndex = 1;
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "illegal details position " //$NON-NLS-1$
                                                + getSkinnable()
                                                .getDetailSide()
                                                + " for orientation " //$NON-NLS-1$
                                                + splitPane
                                                .getOrientation());
                        }
                        break;
                    case VERTICAL:
                        switch (getSkinnable().getDetailSide()) {
                            case TOP:
                                detailsIndex = 0;
                                break;
                            case BOTTOM:
                                detailsIndex = 1;
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "illegal details position " //$NON-NLS-1$
                                                + getSkinnable()
                                                .getDetailSide()
                                                + " for orientation " //$NON-NLS-1$
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
        });

        getSkinnable().showDetailNodeProperty().addListener((observable, oldShow, newShow) -> {

            /**
             * https://bitbucket.org/controlsfx/controlsfx/issue/456/masterdetailpane-bug-of-adding-infinite
             *
             * Fixed bug - when close or show is still animated jump to last frame of animation
             ** and fire finished event to complete the previous demand
             *
             */
            if (getSkinnable().isAnimated() && timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.jumpTo("endAnimation");
                timeline.getOnFinished().handle(null);
            }

            if (newShow) {
                open();
            } else {
                close();
            }
        });

        getSkinnable().detailSideProperty().addListener((observable, oldPos, newPos) -> {
            final Node detailNode = getSkinnable().getDetailNode();
            final Node masterNode = getSkinnable().getMasterNode();

            final boolean showDetailNode = getSkinnable().isShowDetailNode() && detailNode != null;

            if (showDetailNode) {
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
                    if (showDetailNode) {
                        splitPane.getItems().add(detailNode);
                        splitPane.getItems().add(masterNode);
                    }
                    switch (oldPos) {
                        case BOTTOM:
                        case RIGHT:
                            getSkinnable().setDividerPosition(1 - getSkinnable().getDividerPosition());
                            break;
                        default:
                            break;
                    }
                    break;
                case BOTTOM:
                case RIGHT:
                    if (showDetailNode) {
                        splitPane.getItems().add(masterNode);
                        splitPane.getItems().add(detailNode);
                    }
                    switch (oldPos) {
                        case TOP:
                        case LEFT:
                            getSkinnable().setDividerPosition(1 - getSkinnable().getDividerPosition());
                            break;
                        default:
                            break;
                    }
                    break;
            }
            if (showDetailNode) {
                splitPane.setDividerPositions(getSkinnable().getDividerPosition());
            }
        });

        updateMinAndMaxSizes();

        getChildren().add(splitPane);

        splitPane.getItems().

                add(getSkinnable().getMasterNode());

        if (getSkinnable().isShowDetailNode()) {
            switch (getSkinnable().getDetailSide()) {
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

        timeline.setOnFinished(evt -> {
            if (!showDetailForTimeline.get()) {
                unbindDividerPosition();
                splitPane.getItems().remove(
                        getSkinnable().getDetailNode());
                getSkinnable().getDetailNode().setOpacity(1);
            }
            changing = false;
        });
    }

    private InvalidationListener listenersDivider = new InvalidationListener() {
        @Override
        public void invalidated(Observable arg0) {
            changing = true;
            splitPane.setDividerPosition(0, getSkinnable().getDividerPosition());
            changing = false;
        }
    };

    private void bindDividerPosition() {
        getSkinnable().dividerPositionProperty().addListener(listenersDivider);
    }

    private void unbindDividerPosition() {
        getSkinnable().dividerPositionProperty().removeListener(listenersDivider);
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
        changing = true;
        Node node = getSkinnable().getDetailNode();
        if (node == null) {
            return;
        }

        switch (getSkinnable().getDetailSide()) {
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

        updateMinAndMaxSizes();
        maybeAnimatePositionChange(getSkinnable().getDividerPosition(), true);
    }

    private void close() {
        changing = true;
        if (!splitPane.getDividers().isEmpty()) {

            /*
             * Do we collapse by moving the divider to the left/right or
             * top/bottom?
             */
            double targetLocation = 0;
            switch (getSkinnable().getDetailSide()) {
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

        Node detailNode = getSkinnable().getDetailNode();
        if (detailNode == null) {
            return;
        }

        showDetailForTimeline.set(showDetail);

        Divider divider = splitPane.getDividers().get(0);

        if (showDetailForTimeline.get()) {
            unbindDividerPosition();
            bindDividerPosition();
        }

        if (getSkinnable().isAnimated() && detailNode != null) {
            KeyValue positionKeyValue = new KeyValue(
                    divider.positionProperty(), position);
            KeyValue opacityKeyValue = new KeyValue(detailNode.opacityProperty(), showDetailForTimeline.get() ? 1 : 0);

            KeyFrame keyFrame = new KeyFrame(Duration.seconds(.1), "endAnimation", positionKeyValue, opacityKeyValue);

            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(keyFrame);

            timeline.playFromStart();
        } else {
            detailNode.setOpacity(1);
            divider.setPosition(position);

            if (!showDetailForTimeline.get()) {
                unbindDividerPosition();
                splitPane.getItems().remove(getSkinnable().getDetailNode());
            }
            changing = false;
        }
    }

    private ChangeListener<Number> updateDividerPositionListener = new ChangeListener<Number>() {

        @Override
        public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            if (!changing) {
                getSkinnable().setDividerPosition(t1.doubleValue());
            }
        }
    };
}
