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
    
    private final Node node;
    private final List<Border> borders;
    
    public static Borders wrap(Node n) {
        return new Borders(n);
    }
    
    private Borders(Node n) { 
        this.node = n;
        this.borders = new ArrayList<>();
    }
    
    public Borders addEmptyBorder(double padding) {
        addEmptyBorder(padding, padding, padding, padding);
        return this;
    }
    
    public Borders addEmptyBorder(double top, double right, double bottom, double left) {
        addBorder(new EmptyBorder(top, right, bottom, left));
        return this;
    }
    
    public Borders addLineBorder(Color color) {
        return addLineBorder(color, 1);
    }
    
    public Borders addLineBorder(Color color, int thickness) {
        return addLineBorder(color, thickness, false);
    }
    
    public Borders addLineBorder(Color color, int thickness, boolean rounded) {
        addBorder(new LineBorder(color, thickness, rounded));
        return this;
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
    
    
    public static interface Border {
        public Node wrap(Node n);
    }
    
    private static class EmptyBorder implements Border {
        private final double top;
        private final double right;
        private final double bottom;
        private final double left;
        
        public EmptyBorder(double top, double right, double bottom, double left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }

        @Override public Node wrap(Node n) {
            StackPane stack = new StackPane(n);
            stack.setPadding(new Insets(top, right, bottom, left));
            return stack;
        }
    }
    
    private static class LineBorder implements Border {
        private final Color color;
        private final int thickness;
        private final boolean rounded;
        
        public LineBorder(Color color, int thickness, boolean rounded) {
            this.color = color;
            this.thickness = thickness;
            this.rounded = rounded;
        }

        @Override public Node wrap(Node n) {
            StackPane stack = new StackPane(n);
            
            BorderStroke borderStroke = new BorderStroke(color, 
                                                         BorderStrokeStyle.SOLID, 
                                                         rounded ? new CornerRadii(3) : null, 
                                                         new BorderWidths(thickness));
            stack.setBorder(new javafx.scene.layout.Border(borderStroke));
            stack.setPadding(new Insets(5));
            return stack;
        }
    }
}
