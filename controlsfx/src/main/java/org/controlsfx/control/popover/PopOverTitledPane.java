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

import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.control.ContentDisplay.TEXT_ONLY;
import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.util.Duration;

public class PopOverTitledPane extends TitledPane {

	public PopOverTitledPane(final String title, final Node detailedContent) {
		this(title, null, detailedContent);
	}

	public PopOverTitledPane(final String title, final Node summaryContent,
			final Node detailedContent) {

		super(title, detailedContent);

		if (title == null) {
			throw new IllegalArgumentException("title can not be null");
		}

		if (detailedContent == null) {
			throw new IllegalArgumentException(
					"detailed content can not be null");
		}

		setContentDisplay(TEXT_ONLY);
		setGraphic(summaryContent);

		expandedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> value,
					Boolean oldExpanded, Boolean newExpanded) {
				if (newExpanded) {
					setContentDisplay(TEXT_ONLY);
					detailedContent.setOpacity(0);
					FadeTransition fadeInContent = new FadeTransition(
							getFadingDuration());
					fadeInContent.setFromValue(0);
					fadeInContent.setToValue(1);
					fadeInContent.setNode(detailedContent);
					fadeInContent.play();
				} else {
					if (summaryContent != null) {
						setContentDisplay(GRAPHIC_ONLY);
						summaryContent.setOpacity(0);
						FadeTransition fadeInSummary = new FadeTransition(
								getFadingDuration());
						fadeInSummary.setFromValue(0);
						fadeInSummary.setToValue(1);
						fadeInSummary.setNode(summaryContent);
						fadeInSummary.play();
					}
				}
			}
		});
	}

	private final ObjectProperty<Duration> fadingDuration = new SimpleObjectProperty<>(
			this, "fadingDuration", Duration.seconds(.5));

	public final ObjectProperty<Duration> fadingDurationProperty() {
		return fadingDuration;
	}

	public final void setFadingDuration(Duration duration) {
		fadingDurationProperty().set(duration);
	}

	public final Duration getFadingDuration() {
		return fadingDurationProperty().get();
	}
}
