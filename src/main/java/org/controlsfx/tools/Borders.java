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

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

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
            parent.addBorder(new StrokeBorder(buildStroke()));
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
        
        public Borders build() {
            Color inner = raised ? shadowColor : highlightColor;
            Color outer = raised ? highlightColor : shadowColor;
            BorderStroke innerStroke = new BorderStroke(inner, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
            BorderStroke outerStroke = new BorderStroke(outer, BorderStrokeStyle.SOLID, null, new BorderWidths(1), new Insets(1));
            parent.addBorder(new StrokeBorder(innerStroke, outerStroke));
            return parent;
        }
        
        public Node buildAll() {
            build();
            return parent.build();
        }
    }
    
    public class LineBorders {
        private final Borders parent;
        
        private BorderStrokeStyle strokeStyle = BorderStrokeStyle.SOLID;
        
        private Color topColor = DEFAULT_BORDER_COLOR;
        private Color rightColor = DEFAULT_BORDER_COLOR;
        private Color bottomColor = DEFAULT_BORDER_COLOR;
        private Color leftColor = DEFAULT_BORDER_COLOR;
        
        private double topPadding = 20;
        private double rightPadding = 20;
        private double bottomPadding = 20;
        private double leftPadding = 20;
        
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
        
        public Borders build() {
            BorderStroke borderStroke = new BorderStroke(
                    topColor, rightColor, bottomColor, leftColor, 
                    strokeStyle, strokeStyle, strokeStyle, strokeStyle,  
                    new CornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius, false), 
                    new BorderWidths(topThickness, rightThickness, bottomThickness, leftThickness),
                    null);
            
            BorderStroke emptyStroke = new EmptyBorders(parent)
                .padding(topPadding, rightPadding, bottomPadding, leftPadding)
                .buildStroke();
            
            parent.addBorder(new StrokeBorder(emptyStroke, borderStroke));
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
        private final BorderStroke[] borderStrokes;
        
        public StrokeBorder(BorderStroke... borderStrokes) {
            this.borderStrokes = borderStrokes;
        }

        @Override public Node wrap(Node n) {
            StackPane stack = new StackPane(n);
            stack.setBorder(new javafx.scene.layout.Border(borderStrokes));
//            stack.setPadding(new Insets(5));
            return stack;
        }
    }
}
