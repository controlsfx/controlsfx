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

import static javafx.geometry.VPos.TOP;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class PopOverHeader<T extends Node> extends GridPane {

	private TextField titleField = new TextField();

	private TextField subtitleField = new TextField();

	public PopOverHeader() {
		add(titleField, 0, 0);
		add(subtitleField, 0, 1);

		getStyleClass().add("header");

		titleField.getStyleClass().add("title");
		titleField.setEditable(true);
		titleField.setPromptText("Title");
		titleField.setMaxWidth(500);

		subtitleField.getStyleClass().add("subtitle");
		subtitleField.setEditable(true);
		subtitleField.setPromptText("Subtitle");
		subtitleField.setMaxWidth(500);

		Bindings.bindBidirectional(titleField.textProperty(), titleProperty());
		Bindings.bindBidirectional(subtitleField.textProperty(),
				subtitleProperty());

		if (getExtra() != null) {
			add(getExtra(), 1, 0, 1, 2);
			GridPane.setValignment(getExtra(), TOP);
		}

		GridPane.setValignment(titleField, TOP);
		GridPane.setValignment(subtitleField, TOP);
		GridPane.setHgrow(titleField, Priority.ALWAYS);
		GridPane.setHgrow(subtitleField, Priority.ALWAYS);
		GridPane.setFillWidth(titleField, true);
		GridPane.setFillWidth(subtitleField, true);

		extraProperty().addListener(new ChangeListener<Node>() {
			@Override
			public void changed(ObservableValue<? extends Node> value,
					Node oldNode, Node newNode) {
				if (oldNode != null) {
					getChildren().remove(oldNode);
				}
				if (newNode != null) {
					add(newNode, 1, 0, 1, 2);
					GridPane.setValignment(getExtra(), TOP);
				}

				if (newNode != null) {
					String style = "header-extra";
					if (!newNode.getStyleClass().contains(style)) {
						newNode.getStyleClass().add(style);
					}
				}
			}
		});

		@SuppressWarnings("unchecked")
		T picker = (T) new ColorPicker();
		setExtra(picker);
	}

	public final TextField getTitleField() {
		return titleField;
	}

	public final TextField getSubtitleField() {
		return subtitleField;
	}

	// title support

	private final StringProperty title = new SimpleStringProperty(this, "title");

	public final StringProperty titleProperty() {
		return title;
	}

	public final void setTitle(String title) {
		titleProperty().set(title);
	}

	public final String getTitle() {
		return titleProperty().get();
	}

	// subtitle support

	private final StringProperty subtitle = new SimpleStringProperty(this,
			"subtitle");

	public final StringProperty subtitleProperty() {
		return subtitle;
	}

	public final void setSubtitle(String subtitle) {
		subtitleProperty().set(subtitle);
	}

	public final String getSubtitle() {
		return subtitleProperty().get();
	}

	// extras support

	private final ObjectProperty<T> extra = new SimpleObjectProperty<T>(this,
			"extra");

	public final ObjectProperty<T> extraProperty() {
		return extra;
	}

	public final void setExtra(T extra) {
		extraProperty().set(extra);
	}

	public final Node getExtra() {
		return extraProperty().get();
	}
}