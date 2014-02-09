package org.controlsfx.tools;

import javafx.scene.shape.Rectangle;

/**
 * Usability functions for shapes.
 */
public class Shapes {

    /**
     * Returns a {@link Rectangle} where the following values are identical to the specified one: <br>
     *  - {@link Rectangle#xProperty() x} <br>
     *  - {@link Rectangle#yProperty() y} <br>
     *  - {@link Rectangle#widthProperty() width} <br>
     *  - {@link Rectangle#heightProperty() height} <br>
     * 
     * @param original the rectangle to copy
     * @return the copy of the rectangle
     */
    public static Rectangle copy(Rectangle original) {
        Rectangle copy = new Rectangle(original.getX(), original.getY(), original.getWidth(), original.getHeight());
        return copy;
    }

}
