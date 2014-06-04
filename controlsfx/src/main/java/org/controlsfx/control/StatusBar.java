/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.control;

import impl.org.controlsfx.skin.StatusBarSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class StatusBar extends Control {

	public StatusBar() {
		getStyleClass().add("status-bar");
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new StatusBarSkin(this);
	}

	@Override
	protected String getUserAgentStylesheet() {
		return StatusBar.class.getResource("statusbar.css").toExternalForm();
	}

	private final StringProperty text = new SimpleStringProperty(this, "text",
			"OK");

	public final StringProperty textProperty() {
		return text;
	}

	public final void setText(String text) {
		textProperty().set(text);
	}

	public final String getText() {
		return textProperty().get();
	}

	private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(
			this, "graphic");

	public final ObjectProperty<Node> graphicProperty() {
		return graphic;
	}

	public final Node getGraphic() {
		return graphicProperty().get();
	}

	public final void setGraphic(Node node) {
		graphicProperty().set(node);
	}

	private final ObservableList<Node> leftItems = FXCollections
			.observableArrayList();

	public final ObservableList<Node> getLeftItems() {
		return leftItems;
	}

	private final ObservableList<Node> rightItems = FXCollections
			.observableArrayList();

	public final ObservableList<Node> getRightItems() {
		return rightItems;
	}

	private final DoubleProperty progress = new SimpleDoubleProperty(this,
			"progress");

	public final DoubleProperty progressProperty() {
		return progress;
	}

	public final void setProgress(double progress) {
		progressProperty().set(progress);
	}

	public final double getProgress() {
		return progressProperty().get();
	}
}
