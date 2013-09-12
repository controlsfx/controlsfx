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
package org.controlsfx.samples;

import static org.controlsfx.iconfont.IconFontRegistry.glyph;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.iconfont.IconFont;
import org.controlsfx.iconfont.pack.FontAwesomePack;

public class HelloIconFont extends Application implements Sample {

	private IconFont fontAwesome = new IconFont("FontAwesome",
			"http://fontawesome.io/assets/font-awesome/font/fontawesome-webfont.ttf");

	private IconFont icoMoon = new IconFont("icomoon", getClass()
			.getResourceAsStream("icomoon.ttf"));

	private static char FAW_TRASH = '\uf014';
	private static char FAW_GEAR  = '\uf013';
	private static char FAW_STAR  = '\uf005';

	private static char IM_BOLD        = '\ue027';
	private static char IM_UNDERSCORED = '\ue02b';
	private static char IM_ITALIC      = '\ue13e';

	@Override
	public String getSampleName() {
		return "Icon Font";
	}

	@Override
	public String getJavaDocURL() {
		return Utils.JAVADOC_BASE
				+ "org/controlsfx/control/iconfont/IconFont.html";
	}

	@Override
	public boolean includeInSamples() {
		return true;
	}

	@Override
	public Node getPanel(final Stage stage) {

		VBox root = new VBox(10);
		
		root.setPadding(new Insets(10, 10, 10, 10));
		root.setMaxHeight(Double.MAX_VALUE);
		Label title = new Label("Using FontAwesome(CDN)");
		root.getChildren().add(title);
		ToolBar toolbar = new ToolBar(
				new Button("", glyph("FontAwesome:TRASH")), 
				new Button("", fontAwesome.fontColor(Color.RED).create(FAW_GEAR)), 
				new Button("", glyph("FontAwesome:STAR")),
				new Button("", FontAwesomePack.Glyph.ANCHOR.create()));
		root.getChildren().add(toolbar);
		title = new Label("Using IcoMoon (Local)");
		root.getChildren().add(title);
		toolbar = new ToolBar(
				new Button("", icoMoon.fontSize(16).create(IM_BOLD)),
				new Button("", icoMoon.fontColor(Color.GREEN).fontSize(32).create(IM_UNDERSCORED)), 
				new Button("", icoMoon.fontSize(48).create(IM_ITALIC)));
		root.getChildren().add(toolbar);
		return root;

	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Icon Font Demo");

		Scene scene = new Scene((Parent) getPanel(stage), 1300, 300);
		scene.setFill(Color.WHITE);

		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}