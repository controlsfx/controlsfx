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
package org.controlsfx.tools;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotResult;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public final class Borders {
    
    private static final Color DEFAULT_BORDER_COLOR = Color.DARKGRAY;
    
    private final Node node;
    private final List<Border> borders;
    
    public static Borders wrap(Node n) {
        return new Borders(n);
    }
    
    private Borders(Node n) { 
        this.node = n;
        this.borders = new ArrayList<>();
    }
    
    public EmptyBorders emptyBorder() {
        return new EmptyBorders(this);
    }
    
    public EtchedBorders etchedBorder() {
        return new EtchedBorders(this);
    }
    
    public LineBorders lineBorder() {
        return new LineBorders(this);
    }
    
    public Borders addBorder(Border border) {
        borders.add(border);
        return this;
    }

    
    /**
     * Returns the original node wrapped in zero or more borders, as specified
     * using the fluent API.
     */
    public Node build() {
        // we iterate through the borders list in reverse order
        Node bundle = node;
        for (int i = borders.size() - 1; i >= 0; i--) {
            Border border = borders.get(i);
            bundle = border.wrap(bundle);
        }
        return bundle;
    }
    
    
    
    public class EmptyBorders {
        private final Borders parent;
        
        private double top;
        private double right;
        private double bottom;
        private double left;
        
        private EmptyBorders(Borders parent) { 
            this.parent = parent;
        }
        
        public EmptyBorders padding(double padding) {
            return padding(padding, padding, padding, padding);
        }
        
        public EmptyBorders padding(double top, double right, double bottom, double left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
            return this;
        }
        
        public Borders build() {
            parent.addBorder(new StrokeBorder(null, buildStroke()));
            return parent;
        }
        
        public Node buildAll() {
            build();
            return parent.build();
        }
        
        private BorderStroke buildStroke() {
            return new BorderStroke(
                null, 
                BorderStrokeStyle.NONE,
                null, 
                new BorderWidths(top, right, bottom, left),
                Insets.EMPTY);
        }
    }
    
    public class EtchedBorders {
        private final Borders parent;
        
        private String title;
        private boolean raised = false;
        
        private Color highlightColor = DEFAULT_BORDER_COLOR;
        private Color shadowColor = Color.WHITE;
        
        private EtchedBorders(Borders parent) { 
            this.parent = parent;
        }
        
        public EtchedBorders highlight(Color highlight) {
            this.highlightColor = highlight;
            return this;
        }
        
        public EtchedBorders shadow(Color shadow) {
            this.shadowColor = shadow;
            return this;
        }
        
        public EtchedBorders raised() {
            raised = true;
            return this;
        }
        
        public EtchedBorders title(String title) {
            this.title = title;
            return this;
        }
        
        public Borders build() {
            Color inner = raised ? shadowColor : highlightColor;
            Color outer = raised ? highlightColor : shadowColor;
            BorderStroke innerStroke = new BorderStroke(inner, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
            BorderStroke outerStroke = new BorderStroke(outer, BorderStrokeStyle.SOLID, null, new BorderWidths(1), new Insets(1));
            parent.addBorder(new StrokeBorder(title, innerStroke, outerStroke));
            return parent;
        }
        
        public Node buildAll() {
            build();
            return parent.build();
        }
    }
    
    public class LineBorders {
        private final Borders parent;
        
        private String title;
        
        private BorderStrokeStyle strokeStyle = BorderStrokeStyle.SOLID;
        
        private Color topColor = DEFAULT_BORDER_COLOR;
        private Color rightColor = DEFAULT_BORDER_COLOR;
        private Color bottomColor = DEFAULT_BORDER_COLOR;
        private Color leftColor = DEFAULT_BORDER_COLOR;
        
        private double topPadding = 10;
        private double rightPadding = 10;
        private double bottomPadding = 10;
        private double leftPadding = 10;
        
        private double topThickness = 1;
        private double rightThickness = 1;
        private double bottomThickness = 1;
        private double leftThickness = 1;

        private double topLeftRadius = 0;
        private double topRightRadius = 0;
        private double bottomRightRadius = 0;
        private double bottomLeftRadius = 0;
        
        private LineBorders(Borders parent) { 
            this.parent = parent;
        }
        
        public LineBorders color(Color color) {
            return color(color, color, color, color);
        }
        
        public LineBorders color(Color topColor, Color rightColor, Color bottomColor, Color leftColor) {
            this.topColor = topColor;
            this.rightColor = rightColor;
            this.bottomColor = bottomColor;
            this.leftColor = leftColor;
            return this;
        }
        
        public LineBorders strokeStyle(BorderStrokeStyle strokeStyle) {
            this.strokeStyle = strokeStyle;
            return this;
        }
        
        public LineBorders thickness(double thickness) {
            return thickness(thickness, thickness, thickness, thickness);
        }
        
        public LineBorders thickness(double topThickness, double rightThickness, double bottomThickness, double leftThickness) {
            this.topThickness = topThickness;
            this.rightThickness = rightThickness;
            this.bottomThickness = bottomThickness;
            this.leftThickness = leftThickness;
            return this;
        }
        
        public LineBorders radius(double radius) {
            return radius(radius, radius, radius, radius);
        }
        
        public LineBorders radius(double topLeft, double topRight, double bottomRight, double bottomLeft) {
            this.topLeftRadius = topLeft;
            this.topRightRadius = topRight;
            this.bottomRightRadius = bottomRight;
            this.bottomLeftRadius = bottomLeft;
            return this;
        }
        
        public LineBorders title(String title) {
            this.title = title;
            return this;
        }
        
        public Borders build() {
            BorderStroke borderStroke = new BorderStroke(
                    topColor, rightColor, bottomColor, leftColor, 
                    strokeStyle, strokeStyle, strokeStyle, strokeStyle,  
                    new CornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius, false), 
                    new BorderWidths(topThickness, rightThickness, bottomThickness, leftThickness),
                    null);
            
            BorderStroke outerPadding = new EmptyBorders(parent)
                .padding(topPadding, rightPadding, bottomPadding, leftPadding)
                .buildStroke();
            
            BorderStroke innerPadding = new EmptyBorders(parent).padding(15).buildStroke();
            
            parent.addBorder(new StrokeBorder(null, outerPadding));
            parent.addBorder(new StrokeBorder(title, borderStroke));
            parent.addBorder(new StrokeBorder(null, innerPadding));
            
            return parent;
        }
        
        public Node buildAll() {
            build();
            return parent.build();
        }
    }
    
    
    
    public static interface Border {
        public Node wrap(Node n);
    }
    
    
    
    
    
    
    
    
    
    
    // --- Border implementations
    
    private static class StrokeBorder implements Border {
        private static final int TITLE_PADDING = 3;
        
        private final String title;
        private final BorderStroke[] borderStrokes;
        
        public StrokeBorder(String title, BorderStroke... borderStrokes) {
            this.title = title;
            this.borderStrokes = borderStrokes;
        }

        @Override public Node wrap(final Node n) {
            StackPane pane = new StackPane() {
                Label titleLabel;
                
                {
                    // add in the node we are wrapping
                    getChildren().add(n);
                    
                    
                    // if the title string is set, then also add in the title label
                    if (title != null) {
                        titleLabel = new Label(title);
                        updateTitleLabelFill();
                        
                        // when the scene changes, we should update the title
                        // label fill (although realistically, this only ever 
                        // happens on startup).
                        n.sceneProperty().addListener(new InvalidationListener() {
                            @Override public void invalidated(Observable o) {
                                updateTitleLabelFill();
                            }
                        });
    
                        // give the text a bit of space on the left...
                        titleLabel.setPadding(new Insets(0, 0, 0, TITLE_PADDING));
                        getChildren().add(titleLabel);
                    }
                }
                
                @Override protected void layoutChildren() {
                    super.layoutChildren();
                    
                    // layout the title label
                    if (titleLabel != null) {
                        final double labelHeight = titleLabel.prefHeight(-1);
                        final double labelWidth = titleLabel.prefWidth(labelHeight) + TITLE_PADDING;
                        titleLabel.resize(labelWidth, labelHeight);
                        titleLabel.relocate(TITLE_PADDING * 2, -labelHeight / 2.0 - 1);
                    }
                }
                
                private void updateTitleLabelFill() {
                    final Scene s = n.getScene();
                    
                    if (s == null) {
                        BackgroundFill fill = new BackgroundFill(Color.TRANSPARENT, null, null);
                        titleLabel.setBackground(new Background(fill));
                    } else {
                        updateTitleLabelFillFromScene(s);
                        s.fillProperty().addListener(new InvalidationListener() {
                            @Override public void invalidated(Observable arg0) {
                                updateTitleLabelFillFromScene(s);
                            }
                        });
                    }
                }
                
                private void updateTitleLabelFillFromScene(Scene s) {
                    s.snapshot(new Callback<SnapshotResult, Void>() {
                        @Override public Void call(SnapshotResult result) {
                            // determine the bounds of the scene and the location
                            // of the titleLabel node
                            Bounds b = titleLabel.localToScene(titleLabel.getBoundsInLocal());
                            int minX = (int) Math.max(0, b.getMinX());
                            int minY = (int) Math.max(0, b.getMinY());
                            //int maxX = (int) Math.min(s.getWidth(), b.getMaxX());
                            //int maxY = (int) Math.max(s.getHeight(), b.getMaxY());
                            
                            // for now we just pick out one color (hoping there isn't a gradient)
                            Color c = result.getImage().getPixelReader().getColor(minX, minY);
                            
                            // with that color we can set the background fill 
                            // of the titleLabel to perfectly blend in
                            BackgroundFill fill = new BackgroundFill(c, null, null);
                            titleLabel.setBackground(new Background(fill));
                            
                            return null;
                        }
                    }, null);
                }
            };
            
            pane.setBorder(new javafx.scene.layout.Border(borderStrokes));
            return pane;
        }
    }
}
