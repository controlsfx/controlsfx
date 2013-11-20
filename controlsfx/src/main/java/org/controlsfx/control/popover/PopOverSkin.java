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
package org.controlsfx.control.popover;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.VLineTo;
import javafx.stage.Window;

public class PopOverSkin extends SkinBase<PopOver> {
	private Accordion accordion;
	private BorderPane content;

	private Path path;
	private MoveTo moveTo;
	private HLineTo lineA;
	private QuadCurveTo curveB;
	private VLineTo lineC;
	private QuadCurveTo curveD;
	private HLineTo lineE;
	private QuadCurveTo curveF;
	private VLineTo lineG;
	private LineTo lineH;
	private LineTo lineI;
	private VLineTo lineJ;
	private QuadCurveTo curveK;

	private double xOffset;
	private double yOffset;

	private boolean detaching;

	public PopOverSkin(PopOver editor) {
		super(editor);

		this.accordion = new Accordion();

		content = new BorderPane();
		content.setTop(editor.getHeader());
		content.setCenter(accordion);
		content.setBottom(editor.getFooter());

		path = new Path();
		path.setManaged(false);
		path.visibleProperty().bind(Bindings.not(editor.detachedProperty()));

		createPathElements();
		updatePath();

		editor.headerProperty().addListener(new ChangeListener<Node>() {
			@Override
			public void changed(ObservableValue<? extends Node> value,
					Node oldNode, Node newNode) {
				content.setTop(newNode);
			}
		});

		editor.footerProperty().addListener(new ChangeListener<Node>() {
			@Override
			public void changed(ObservableValue<? extends Node> value,
					Node oldNode, Node newNode) {
				content.setBottom(newNode);
			}
		});

		editor.detachedProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				updatePath();
			}
		});

		final EventHandler<MouseEvent> mousePressedHandler = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent evt) {
				detaching = false;

				xOffset = evt.getScreenX();
				yOffset = evt.getScreenY();

			};
		};

		final EventHandler<MouseEvent> mouseReleasedHandler = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent evt) {
				if (detaching) {
					getSkinnable().detach();
				}
			};
		};

		final EventHandler<MouseEvent> mouseDragHandler = new EventHandler<MouseEvent>() {

			public void handle(MouseEvent evt) {

				double deltaX = evt.getScreenX() - xOffset;
				double deltaY = evt.getScreenY() - yOffset;

				Window window = getSkinnable().getScene().getWindow();

				window.setX(window.getX() + deltaX);
				window.setY(window.getY() + deltaY);

				xOffset = evt.getScreenX();
				yOffset = evt.getScreenY();

				Node owner = getSkinnable().getOwner();
				Point2D screenPoint = getSkinnable().getOwner().localToScreen(
						0, 0);

				Rectangle2D bounds = new Rectangle2D(screenPoint.getX(),
						screenPoint.getY(), owner.getLayoutBounds().getWidth(),
						owner.getLayoutBounds().getHeight());

				// System.out.println("screen point = " + screenPoint);
				// System.out.println("bounds       = " + bounds);

				double arrowX = window.getX();
				double arrowY = window.getY()
						+ getSkinnable().getCornerRadius()
						+ getSkinnable().getArrowIndent()
						+ getSkinnable().getArrowSize();

				// System.out.println("windowX     = " + window.getX());
				// System.out.println("windowY     = " + window.getY());
				// System.out.println("arrowX      = " + arrowX);
				// System.out.println("arrowY      = " + arrowY);

				if (!bounds.contains(arrowX, arrowY)) {
					detaching = true;

					updatePath();
				}
			};
		};

		accordion.getPanes().addListener(new ListChangeListener<TitledPane>() {
			@Override
			public void onChanged(
					ListChangeListener.Change<? extends TitledPane> change) {
				while (change.next()) {
					for (TitledPane pane : change.getAddedSubList()) {
						pane.setOnMousePressed(mousePressedHandler);
						pane.setOnMouseDragged(mouseDragHandler);
						pane.setOnMouseReleased(mouseReleasedHandler);
					}
				}
			}
		});

		Bindings.bindBidirectional(accordion.expandedPaneProperty(),
				editor.expandedPaneProperty());
		Bindings.bindContent(accordion.getPanes(), editor.getPanes());

		getChildren().add(path);
		getChildren().add(content);

		content.getStyleClass().add("content");
		path.getStyleClass().add("border");

		getSkinnable().setOnMousePressed(mousePressedHandler);
		getSkinnable().setOnMouseDragged(mouseDragHandler);
		getSkinnable().setOnMouseReleased(mouseReleasedHandler);
	}

	@Override
	protected void layoutChildren(double contentX, double contentY,
			double contentWidth, double contentHeight) {
		super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

		if (getSkinnable().isDetached()) {
			path.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
		} else {
			path.resizeRelocate(contentX - getSkinnable().getArrowSize(),
					contentY, contentWidth, contentHeight);
		}
	}

	private void createPathElements() {
		DoubleProperty arrowXLocationProperty = new SimpleDoubleProperty();
		DoubleProperty leftEdgeProperty = new SimpleDoubleProperty();
		DoubleProperty leftEdgePlusRadiusProperty = new SimpleDoubleProperty();
		DoubleProperty rightEdgeProperty = new SimpleDoubleProperty();
		DoubleProperty rightEdgeMinusRadiusProperty = new SimpleDoubleProperty();
		DoubleProperty cornerProperty = getSkinnable().cornerRadiusProperty();
		DoubleProperty arrowSizeProperty = getSkinnable().arrowSizeProperty();
		DoubleProperty arrowIndentProperty = getSkinnable()
				.arrowIndentProperty();
		DoubleProperty bottomEdgeProperty = new SimpleDoubleProperty();
		DoubleProperty bottomEdgeMinusRadiusProperty = new SimpleDoubleProperty();

		leftEdgeProperty.bind(Bindings.subtract(getSkinnable()
				.arrowSizeProperty(), 1));
		leftEdgePlusRadiusProperty.bind(Bindings.add(leftEdgeProperty,
				getSkinnable().cornerRadiusProperty()));

		rightEdgeProperty.bind(Bindings.add(arrowSizeProperty,
				Bindings.subtract(getSkinnable().widthProperty(), 1)));
		rightEdgeMinusRadiusProperty.bind(Bindings.subtract(rightEdgeProperty,
				getSkinnable().cornerRadiusProperty()));

		bottomEdgeProperty.bind(Bindings.subtract(getSkinnable()
				.heightProperty(), 1));
		bottomEdgeMinusRadiusProperty.bind(Bindings.subtract(
				bottomEdgeProperty, getSkinnable().cornerRadiusProperty()));

		// INIT
		moveTo = new MoveTo();
		moveTo.xProperty().bind(leftEdgePlusRadiusProperty);
		moveTo.setY(0.0f);

		// SEGMENT A
		lineA = new HLineTo();
		lineA.xProperty().bind(rightEdgeMinusRadiusProperty);

		// SEGMENT B
		curveB = new QuadCurveTo();
		curveB.xProperty().bind(rightEdgeProperty);
		curveB.yProperty().bind(cornerProperty);
		curveB.controlXProperty().bind(rightEdgeProperty);
		curveB.setControlY(0);

		// SEGMENT C
		lineC = new VLineTo();
		lineC.yProperty().bind(
				Bindings.subtract(getSkinnable().heightProperty(),
						cornerProperty));

		// SEGMENT D
		curveD = new QuadCurveTo();
		curveD.xProperty().bind(rightEdgeMinusRadiusProperty);
		curveD.yProperty().bind(bottomEdgeProperty);
		curveD.controlXProperty().bind(rightEdgeProperty);
		curveD.controlYProperty().bind(bottomEdgeProperty);

		// SEGMENT E
		lineE = new HLineTo();
		lineE.xProperty().bind(leftEdgePlusRadiusProperty);

		// SEGMENT F
		curveF = new QuadCurveTo();
		curveF.xProperty().bind(leftEdgeProperty);
		curveF.yProperty().bind(bottomEdgeMinusRadiusProperty);
		curveF.controlXProperty().bind(leftEdgeProperty);
		curveF.controlYProperty().bind(bottomEdgeProperty);

		// SEGMENT G
		lineG = new VLineTo();
		lineG.yProperty().bind(
				Bindings.add(
						cornerProperty,
						Bindings.add(arrowIndentProperty,
								Bindings.multiply(2, arrowSizeProperty))));

		// SEGMENT H (start arrow)
		lineH = new LineTo();
		lineH.xProperty().bind(arrowXLocationProperty);
		lineH.yProperty().bind(
				Bindings.add(cornerProperty,
						Bindings.add(arrowIndentProperty, arrowSizeProperty)));

		// SEGMENT I (finish arrow)
		lineI = new LineTo();
		lineI.xProperty().bind(leftEdgeProperty);
		lineI.yProperty().bind(
				Bindings.add(cornerProperty, arrowIndentProperty));

		// SEGMENT J
		lineJ = new VLineTo();
		lineJ.yProperty().bind(cornerProperty);

		// SEGMENT K
		curveK = new QuadCurveTo();
		curveK.xProperty().bind(leftEdgePlusRadiusProperty);
		curveK.setY(0);
		curveK.controlXProperty().bind(leftEdgeProperty);
		curveK.setControlY(0);
	}

	private void updatePath() {
		path.getElements().clear();
		path.getElements().add(moveTo);
		path.getElements().add(lineA);
		path.getElements().add(curveB);
		path.getElements().add(lineC);
		path.getElements().add(curveD);
		path.getElements().add(lineE);
		path.getElements().add(curveF);
		path.getElements().add(lineG);

		if (!detaching) {
			path.getElements().add(lineH);
			path.getElements().add(lineI);
		}

		path.getElements().add(lineJ);
		path.getElements().add(curveK);
	}
}
