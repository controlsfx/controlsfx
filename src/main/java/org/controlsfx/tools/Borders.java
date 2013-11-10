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

import javax.swing.BorderFactory;

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

/**
 * A utility class that allows you to wrap JavaFX {@link Node Nodes} with a border,
 * in a way somewhat analogous to the Swing {@link BorderFactory} (although with
 * less options as a lot of what the Swing BorderFactory offers resulted in 
 * ugly borders!).
 * 
 * <p>The Borders class provides a fluent API for specifying the properties of
 * each border. It is possible to create multiple borders around a Node simply
 * by continuing to call additional methods before you call the final 
 * {@link Borders#build()} method. To use the Borders class, you simply call
 * {@link Borders#wrap(Node)}, passing in the Node you wish to wrap the border(s)
 * around.
 */
public final class Borders {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    private static final Color DEFAULT_BORDER_COLOR = Color.DARKGRAY;
    
    
    
    /**************************************************************************
     * 
     * Internal fields
     * 
     **************************************************************************/
    
    private final Node node;
    private final List<Border> borders;
    
    
    
    /**************************************************************************
     * 
     * Fluent API entry method(s)
     * 
     **************************************************************************/
    
    public static Borders wrap(Node n) {
        return new Borders(n);
    }
    
    
    
    /**************************************************************************
     * 
     * Private Constructor
     * 
     **************************************************************************/
    
    private Borders(Node n) { 
        this.node = n;
        this.borders = new ArrayList<>();
    }
    
    
    
    /**************************************************************************
     * 
     * Fluent API
     * 
     **************************************************************************/
    
    /**
     * Often times it is useful to have a bit of whitespace around a Node, to 
     * separate it from what it is next to. Call this method to begin building
     * a border that will wrap the node with a given amount of whitespace 
     * (which can vary between the top, right, bottom, and left sides).
     */
    public EmptyBorders emptyBorder() {
        return new EmptyBorders(this);
    }
    
    /**
     * The etched border look is essentially equivalent to the {@link #lineBorder()}
     * look, except rather than one line, there are two. What is commonly done in
     * this circumstance is that one of the lines is a very light colour (commonly
     * white), which gives a nice etched look. Refer to the API in {@link EtchedBorders}
     * for more information.
     */
    public EtchedBorders etchedBorder() {
        return new EtchedBorders(this);
    }
    
    /**
     * Creates a nice, simple border around the node. Note that there are many
     * configuration options in {@link LineBorders}, so explore it carefully.
     */
    public LineBorders lineBorder() {
        return new LineBorders(this);
    }
    
    /**
     * Allows for developers to develop custom {@link Border} implementations,
     * and to wrap them around a Node. Note that of course this is mostly 
     * redundant (as you could just call {@link Border#wrap(Node)} directly).
     * The only benefit is if you're creating a compound border consisting of 
     * multiple borders, and you want your custom border included as part of 
     * this.
     */
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
    
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
    
    /**
     * A fluent API that is only indirectly instantiable via the {@link Borders} 
     * fluent API, and which allows for an {@link Borders#emptyBorder() empty border}
     * to be wrapped around a given Node.
     */
    public class EmptyBorders {
        private final Borders parent;
        
        private double top;
        private double right;
        private double bottom;
        private double left;
        
        // private on purpose - this class is not directly instantiable.
        private EmptyBorders(Borders parent) { 
            this.parent = parent;
        }
        
        /**
         * Specifies that the wrapped Node should have the given padding around
         * all four sides of itself.
         */
        public EmptyBorders padding(double padding) {
            return padding(padding, padding, padding, padding);
        }

        /**
         * Specifies that the wrapped Node should be wrapped with the given
         * padding for each of its four sides, going in the order top, right,
         * bottom, and finally left.
         */
        public EmptyBorders padding(double top, double right, double bottom, double left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
            return this;
        }
        
        /**
         * Builds the {@link Border} and {@link Borders#addBorder(Border) adds it}
         * to the list of borders to wrap around the given Node (which will be
         * constructed and returned when {@link Borders#build()} is called.
         */
        public Borders build() {
            parent.addBorder(new StrokeBorder(null, buildStroke()));
            return parent;
        }
        
        /**
         * A convenience method, this is equivalent to calling 
         * {@link #build()} followed by {@link Borders#build()}. In other words,
         * calling this will return the original Node wrapped in all its borders 
         * specified.
         */
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
    
    /**
     * A fluent API that is only indirectly instantiable via the {@link Borders} 
     * fluent API, and which allows for an {@link Borders#etchedBorder() etched border}
     * to be wrapped around a given Node.
     */
    public class EtchedBorders {
        private final Borders parent;
        
        private String title;
        private boolean raised = false;
        
        private Color highlightColor = DEFAULT_BORDER_COLOR;
        private Color shadowColor = Color.WHITE;
        
        // private on purpose - this class is not directly instantiable.
        private EtchedBorders(Borders parent) { 
            this.parent = parent;
        }
        
        /**
         * Specifies the highlight colour to use in the etched border.
         */
        public EtchedBorders highlight(Color highlight) {
            this.highlightColor = highlight;
            return this;
        }
        
        /**
         * Specifies the shadow colour to use in the etched border.
         */
        public EtchedBorders shadow(Color shadow) {
            this.shadowColor = shadow;
            return this;
        }
        
        /**
         * Specifies the order in which the highlight and shadow colours are
         * placed. A raised etched border has the shadow colour on the outside
         * of the border, whereas a non-raised (or lowered) etched border has 
         * the shadow colour on the inside of the border.
         */
        public EtchedBorders raised() {
            raised = true;
            return this;
        }
        
        /**
         * If desired, this specifies the title text to show in this border.
         */
        public EtchedBorders title(String title) {
            this.title = title;
            return this;
        }
        
        /**
         * Builds the {@link Border} and {@link Borders#addBorder(Border) adds it}
         * to the list of borders to wrap around the given Node (which will be
         * constructed and returned when {@link Borders#build()} is called.
         */
        public Borders build() {
            Color inner = raised ? shadowColor : highlightColor;
            Color outer = raised ? highlightColor : shadowColor;
            BorderStroke innerStroke = new BorderStroke(inner, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
            BorderStroke outerStroke = new BorderStroke(outer, BorderStrokeStyle.SOLID, null, new BorderWidths(1), new Insets(1));
            parent.addBorder(new StrokeBorder(title, innerStroke, outerStroke));
            return parent;
        }
        
        /**
         * A convenience method, this is equivalent to calling 
         * {@link #build()} followed by {@link Borders#build()}. In other words,
         * calling this will return the original Node wrapped in all its borders 
         * specified.
         */
        public Node buildAll() {
            build();
            return parent.build();
        }
    }
    
    /**
     * A fluent API that is only indirectly instantiable via the {@link Borders} 
     * fluent API, and which allows for a {@link Borders#lineBorder() line border}
     * to be wrapped around a given Node.
     */
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
        
        // private on purpose - this class is not directly instantiable.
        private LineBorders(Borders parent) { 
            this.parent = parent;
        }
        
        /**
         * Specifies the colour to use for all four sides of this border.
         */
        public LineBorders color(Color color) {
            return color(color, color, color, color);
        }

        /**
         * Specifies that the wrapped Node should be wrapped with the given
         * colours for each of its four sides, going in the order top, right,
         * bottom, and finally left.
         */
        public LineBorders color(Color topColor, Color rightColor, Color bottomColor, Color leftColor) {
            this.topColor = topColor;
            this.rightColor = rightColor;
            this.bottomColor = bottomColor;
            this.leftColor = leftColor;
            return this;
        }
        
        /**
         * Specifies which {@link BorderStrokeStyle} to use for this line border.
         * By default this is {@link BorderStrokeStyle#SOLID}, but you can use
         * any other style (such as {@link BorderStrokeStyle#DASHED}, 
         * {@link BorderStrokeStyle#DOTTED}, or a custom style built using
         * {@link BorderStrokeStyle#BorderStrokeStyle(javafx.scene.shape.StrokeType, javafx.scene.shape.StrokeLineJoin, javafx.scene.shape.StrokeLineCap, double, double, List)}.
         */
        public LineBorders strokeStyle(BorderStrokeStyle strokeStyle) {
            this.strokeStyle = strokeStyle;
            return this;
        }
        
        /**
         * Specifies the thickness of the line to use on all four sides of this
         * border.
         */
        public LineBorders thickness(double thickness) {
            return thickness(thickness, thickness, thickness, thickness);
        }
        
        /**
         * Specifies that the wrapped Node should be wrapped with the given
         * line thickness for each of its four sides, going in the order top, right,
         * bottom, and finally left.
         */
        public LineBorders thickness(double topThickness, double rightThickness, double bottomThickness, double leftThickness) {
            this.topThickness = topThickness;
            this.rightThickness = rightThickness;
            this.bottomThickness = bottomThickness;
            this.leftThickness = leftThickness;
            return this;
        }
        
        /**
         * Specifies the radius of the four corners of the line of this border.
         */
        public LineBorders radius(double radius) {
            return radius(radius, radius, radius, radius);
        }
        
        /**
         * Specifies that the line wrapping the node should have corner radii
         * as specified, with each radius being independently configured, going 
         * in the order top-left, top-right, bottom-right, and finally bottom-left.
         */
        public LineBorders radius(double topLeft, double topRight, double bottomRight, double bottomLeft) {
            this.topLeftRadius = topLeft;
            this.topRightRadius = topRight;
            this.bottomRightRadius = bottomRight;
            this.bottomLeftRadius = bottomLeft;
            return this;
        }
        
        /**
         * If desired, this specifies the title text to show in this border.
         */
        public LineBorders title(String title) {
            this.title = title;
            return this;
        }
        
        /**
         * Builds the {@link Border} and {@link Borders#addBorder(Border) adds it}
         * to the list of borders to wrap around the given Node (which will be
         * constructed and returned when {@link Borders#build()} is called.
         */
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
        
        /**
         * A convenience method, this is equivalent to calling 
         * {@link #build()} followed by {@link Borders#build()}. In other words,
         * calling this will return the original Node wrapped in all its borders 
         * specified.
         */
        public Node buildAll() {
            build();
            return parent.build();
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Support interfaces
     * 
     **************************************************************************/
    
    /**
     * The public interface used by the {@link Borders} API to wrap nodes with
     * zero or more Border implementations. ControlsFX ships with a few 
     * Border implementations (current {@link EmptyBorders}, {@link LineBorders},
     * and {@link EtchedBorders}). As noted in {@link Borders#addBorder(Border)},
     * this interface is relatively pointless, unless you plan to wrap a node
     * with multiple borders and you want to use a custom {@link Border} 
     * implementation for at least one border. In this case, you can simply 
     * call {@link Borders#addBorder(Border)} with your custom border, when 
     * appropriate.
     */
    public static interface Border {
        
        /**
         * Given a {@link Node}, this method should return a Node that contains
         * the original Node and also has wrapped it with an appropriate border.
         */
        public Node wrap(Node n);
    }
    
    
    
    /**************************************************************************
     * 
     * Private support classes
     * 
     **************************************************************************/
    
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
