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

import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public abstract class PopOverController<T extends PopOver, S> {

	private T popOver;

	private Map<S, T> popOverMap = new HashMap<S, T>();

	protected PopOverController() {
	}

	public final void setupPopOver(final S target) {
		popOver = popOverMap.get(target);

		if (popOver == null) {
			popOver = createPopOver(target);

			popOver.detachedProperty().addListener(
					new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> value,
								Boolean oldDetached, Boolean newDetached) {
							if (newDetached) {
								popOverMap.put(target, popOver);
								Stage stage = (Stage) popOver.getScene()
										.getWindow();
								stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
									public void handle(WindowEvent evt) {
										popOverMap.remove(target);
									};
								});
								popOver = null;
							}
						}
					});

			popOverMap.put(target, popOver);
		}
	}

	public final void showPopOver(Node owner, double screenX, double screenY) {
		if (popOver == null) {
			throw new IllegalStateException(
					"no pop over has been setup yet (call setupPopOver() first)");
		}

		if (popOver.isDetached()) {
			((Stage) popOver.getScene().getWindow()).toFront();
		} else {
			popOver.show(owner, screenX, screenY);
		}
	}

	public final void hidePopOver() {
		if (popOver != null && !popOver.isDetached()) {
			popOver.hide();
		}
	}

	protected abstract T createPopOver(S userObject);
}
