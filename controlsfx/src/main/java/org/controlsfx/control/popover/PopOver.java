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

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class PopOver extends Control {

	private static final String DEFAULT_STYLE_CLASS = "popup-editor";

	private Popup popup;

	private Node owner;

	public PopOver() {
		super();

		getStyleClass().add(DEFAULT_STYLE_CLASS);

		setHeader(new PopOverHeader<Node>());

		footerProperty().addListener(new ChangeListener<Node>() {
			@Override
			public void changed(ObservableValue<? extends Node> value,
					Node oldNode, Node newNode) {
				if (newNode != null) {
					String style = "footer";
					if (!newNode.getStyleClass().contains(style)) {
						newNode.getStyleClass().add(style);
					}
				}
			}
		});
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new PopOverSkin(this);
	}

	public final Node getOwner() {
		return owner;
	}

	public final void show(Node owner, double x, double y) {
		show(owner, x, y, Duration.seconds(.1));
	}

	public final void show(Node owner, double x, double y, Duration duration) {

		if (owner == null) {
			throw new IllegalArgumentException("owner can not be null");
		}

		if (duration == null) {
			throw new IllegalArgumentException("duration can not be null");
		}

		this.owner = owner;

		if (popup == null) {
			popup = new Popup();
			popup.getContent().add(this);
			popup.setAutoFix(false);
		}

		if (isDetached()) {
			popup.getContent().add(this);
		}

		setDetached(false);
		setOpacity(0);

		owner.boundsInParentProperty().addListener(
				new WeakInvalidationListener(new InvalidationListener() {
					@Override
					public void invalidated(Observable observable) {
						hide();
					}
				}));

		popup.show(owner, x, y - getArrowIndent() - getCornerRadius()
				- getArrowSize());

		FadeTransition fadeIn = new FadeTransition(duration, this);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.play();
	}

	public final boolean isShowing() {
		return popup != null && popup.isShowing();
	}

	public final void hide() {
		if (popup != null) {
			popup.hide();
		}
	}

	public final void detach() {
		if (isDetachable() && !isDetached()) {

			// store your current positon on the screen
			double screenX = localToScreen(getBoundsInLocal()).getMinX();
			double screenY = localToScreen(getBoundsInLocal()).getMinY();

			hide();

			final Group root = new Group();
			root.getStyleClass().add("detached-root");
			root.getChildren().add(this);

			final Stage stage = new Stage(getDetachedStageStyle());
			stage.setTitle(getDetachedTitle());

			root.boundsInLocalProperty().addListener(
					new ChangeListener<Bounds>() {
						private boolean sizing;

						@Override
						public void changed(
								ObservableValue<? extends Bounds> value,
								Bounds oldBounds, Bounds newBounds) {
							if (!sizing) {
								sizing = true;
								stage.sizeToScene();
								sizing = false;
							}
						}
					});

			final Scene scene = new Scene(root);
			stage.setScene(scene);

			stage.setX(screenX);
			stage.setY(screenY);

			stage.sizeToScene();
			stage.show();

			setDetached(true);
		}
	}

	@Override
	protected String getUserAgentStylesheet() {
		return PopOver.class.getResource("popover.css").toExternalForm();
	}

	// detach support

	private final BooleanProperty detachable = new SimpleBooleanProperty(this,
			"detachable", true);

	public final BooleanProperty detachableProperty() {
		return detachable;
	}

	public final void setDetachable(boolean detachable) {
		detachableProperty().set(detachable);
	}

	public final boolean isDetachable() {
		return detachableProperty().get();
	}

	private final BooleanProperty detached = new SimpleBooleanProperty(this,
			"detached", false);

	public final BooleanProperty detachedProperty() {
		return detached;
	}

	public final void setDetached(boolean detached) {
		detachedProperty().set(detached);
	}

	public final boolean isDetached() {
		return detachedProperty().get();
	}

	// header support

	private final ObjectProperty<Node> header = new SimpleObjectProperty<Node>(
			this, "header");

	public final ObjectProperty<Node> headerProperty() {
		return header;
	}

	public final Node getHeader() {
		return headerProperty().get();
	}

	public final void setHeader(Node node) {
		headerProperty().set(node);
	}

	// footer support

	private final ObjectProperty<Node> footer = new SimpleObjectProperty<Node>(
			this, "footer");

	public final ObjectProperty<Node> footerProperty() {
		return footer;
	}

	public final Node getFooter() {
		return footerProperty().get();
	}

	public final void setFooter(Node node) {
		footerProperty().set(node);
	}

	// panes

	private final ObservableList<TitledPane> panes = FXCollections
			.observableArrayList();

	public final ObservableList<TitledPane> getPanes() {
		return panes;
	}

	// arrow size support

	// TODO: make styleable

	private final DoubleProperty arrowSize = new SimpleDoubleProperty(this,
			"arrowSize", 16);

	public final DoubleProperty arrowSizeProperty() {
		return arrowSize;
	}

	public final double getArrowSize() {
		return arrowSizeProperty().get();
	}

	public final void setArrowSize(double size) {
		arrowSizeProperty().set(size);
	}

	// arrow indent support

	// TODO: make styleable

	private final DoubleProperty arrowIndent = new SimpleDoubleProperty(this,
			"arrowIndent", 16);

	public final DoubleProperty arrowIndentProperty() {
		return arrowIndent;
	}

	public final double getArrowIndent() {
		return arrowIndentProperty().get();
	}

	public final void setArrowIndent(double size) {
		arrowIndentProperty().set(size);
	}

	// radius support

	// TODO: make styleable

	private final DoubleProperty cornerRadius = new SimpleDoubleProperty(this,
			"cornerRadius", 8);

	public final DoubleProperty cornerRadiusProperty() {
		return cornerRadius;
	}

	public final double getCornerRadius() {
		return cornerRadiusProperty().get();
	}

	public final void setCornerRadius(double radius) {
		cornerRadiusProperty().set(radius);
	}

	// Detached stage title

	private final StringProperty detachedTitle = new SimpleStringProperty(this,
			"detachedTitle", "Editor");

	public final StringProperty detachedTitleProperty() {
		return detachedTitle;
	}

	public final String getDetachedTitle() {
		return detachedTitleProperty().get();
	}

	public final void setDetachedTitle(String title) {
		if (title == null) {
			throw new IllegalArgumentException("title can not be null");
		}

		detachedTitleProperty().set(title);
	}

	// Detached stage style

	private final ObjectProperty<StageStyle> detachedStageStyle = new SimpleObjectProperty<StageStyle>(
			this, "detachedStageStyle", StageStyle.UTILITY);

	public final ObjectProperty<StageStyle> detachedStageStyleProperty() {
		return detachedStageStyle;
	}

	public final StageStyle getDetachedStageStyle() {
		return detachedStageStyleProperty().get();
	}

	public final void setDetachedStageStyle(StageStyle style) {
		if (style == null) {
			throw new IllegalArgumentException("stage style can not be null");
		}

		detachedStageStyleProperty().set(style);
	}

	// Expanded pane support

	private final ObjectProperty<TitledPane> expandedPane = new SimpleObjectProperty<TitledPane>(
			this, "expandedPane");

	public final ObjectProperty<TitledPane> expandedPaneProperty() {
		return expandedPane;
	}

	public final void setExpandedPane(TitledPane titledPane) {
		expandedPaneProperty().set(titledPane);
	}

	public final TitledPane getExpanedPane() {
		return expandedPane.get();
	}
}
