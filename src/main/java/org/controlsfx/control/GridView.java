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
package org.controlsfx.control;

import impl.org.controlsfx.skin.GridViewSkin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.controlsfx.control.cell.ColorGridCell;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.HPos;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import com.sun.javafx.css.converters.EnumConverter;

/**
 * A GridView is a virtualised control for displaying {@link #getItems()} in a
 * visual, scrollable, grid-like fashion. In other words, whereas a ListView 
 * shows one {@link ListCell} per row, in a GridView there will be zero or more
 * {@link GridCell} instances on a single row.
 * 
 * <p> This approach means that the number of GridCell instances
 * instantiated will be a significantly smaller number than the number of 
 * items in the GridView items list, as only enough GridCells are created for
 * the visible area of the GridView. This helps to improve performance and 
 * reduce memory consumption. 
 * 
 * <p>Because each {@link GridCell} extends from {@link Cell}, the same approach
 * of cell factories that is taken in other UI controls is also taken in GridView.
 * This has two main benefits: 
 * 
 * <ol>
 *   <li>GridCells are created on demand and without user involvement,
 *   <li>GridCells can be arbitrarily complex. A simple GridCell may just have 
 *   its {@link GridCell#textProperty() text property} set, whereas a more complex
 *   GridCell can have an arbitrarily complex scenegraph set inside its
 *   {@link GridCell#graphicProperty() graphic property} (as it accepts any Node).
 * </ol>
 *
 * <h3>Examples</h3>
 * <p>The following screenshot shows the GridView with the {@link ColorGridCell}
 * being used:
 * 
 * <br/>
 * <img src="gridView.png"/>
 * 
 * <p>To create this GridView was simple. Note that the major of the code below
 * is related to randomly creating colours to be represented:
 * 
 * <pre>
 * {@code
 * GridView<Color> myGrid = new GridView<>(list);
 * myGrid.setCellFactory(new Callback<GridView<Color>, GridCell<Color>>() {
 *     public GridCell<Color> call(GridView<Color> gridView) {
 *         return new ColorGridCell();
 *     }
 * });
 * Random r = new Random(System.currentTimeMillis());
 * for(int i = 0; i < 500; i++) {
 *     list.add(new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.0));
 * }
 * }</pre>
 *
 * @see GridCell
 */
public class GridView<T> extends Control {

    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a default, empty GridView control.
     */
    public GridView() {
        this(FXCollections.<T> observableArrayList());
    }
    
    /**
     * Creates a default GridView control with the provided items prepopulated.
     * 
     * @param items The items to display inside the GridView.
     */
    public GridView(ObservableList<T> items) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setItems(items);
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new GridViewSkin<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return GridView.class.getResource("gridview.css").toExternalForm();
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- horizontal cell spacing
    private DoubleProperty horizontalCellSpacing;
    public void setHorizontalCellSpacing(double value) {
        horizontalCellSpacingProperty().set(value);
    }

    public double getHorizontalCellSpacing() {
        return horizontalCellSpacing == null ? 12.0 : horizontalCellSpacing.get();
    }

    public final DoubleProperty horizontalCellSpacingProperty() {
        if (horizontalCellSpacing == null) {
            horizontalCellSpacing = new StyleableDoubleProperty(12) {
                @Override public CssMetaData<GridView<?>, Number> getCssMetaData() {
                    return GridView.StyleableProperties.HORIZONTAL_CELL_SPACING;
                }
                
                @Override public Object getBean() {
                    return GridView.this;
                }

                @Override public String getName() {
                    return "horizontalCellSpacing";
                }
            };
        }
        return horizontalCellSpacing;
    }

    
    // --- vertical cell spacing
    private DoubleProperty verticalCellSpacing;
    public void setVerticalCellSpacing(double value) {
        verticalCellSpacingProperty().set(value);
    }

    public double getVerticalCellSpacing() {
        return verticalCellSpacing == null ? 12.0 : verticalCellSpacing.get();
    }

    public final DoubleProperty verticalCellSpacingProperty() {
        if (verticalCellSpacing == null) {
            verticalCellSpacing = new StyleableDoubleProperty(12) {
                @Override public CssMetaData<GridView<?>, Number> getCssMetaData() {
                    return GridView.StyleableProperties.VERTICAL_CELL_SPACING;
                }
                
                @Override public Object getBean() {
                    return GridView.this;
                }

                @Override public String getName() {
                    return "verticalCellSpacing";
                }
            };
        }
        return verticalCellSpacing;
    }

    
    // --- cell width
    private DoubleProperty cellWidth;
    public final DoubleProperty cellWidthProperty() {
        if (cellWidth == null) {
            cellWidth = new StyleableDoubleProperty(64) {
                @Override public CssMetaData<GridView<?>, Number> getCssMetaData() {
                    return GridView.StyleableProperties.CELL_WIDTH;
                }
                
                @Override public Object getBean() {
                    return GridView.this;
                }

                @Override public String getName() {
                    return "cellWidth";
                }
            };
        }
        return cellWidth;
    }

    public void setCellWidth(double value) {
        cellWidthProperty().set(value);
    }

    public double getCellWidth() {
        return cellWidth == null ? 64.0 : cellWidth.get();
    }

    
    // --- cell height
    private DoubleProperty cellHeight;
    public final DoubleProperty cellHeightProperty() {
        if (cellHeight == null) {
            cellHeight = new StyleableDoubleProperty(64) {
                @Override public CssMetaData<GridView<?>, Number> getCssMetaData() {
                    return GridView.StyleableProperties.CELL_HEIGHT;
                }
                
                @Override public Object getBean() {
                    return GridView.this;
                }

                @Override public String getName() {
                    return "cellHeight";
                }
            };
        }
        return cellHeight;
    }

    public void setCellHeight(double value) {
        cellHeightProperty().set(value);
    }

    public double getCellHeight() {
        return cellHeight == null ? 64.0 : cellHeight.get();
    }

    
    // --- horizontal alignment
    private ObjectProperty<HPos> horizontalAlignment;
    public final ObjectProperty<HPos> horizontalAlignmentProperty() {
        if (horizontalAlignment == null) {
            horizontalAlignment = new StyleableObjectProperty<HPos>(HPos.CENTER) {
                @Override public CssMetaData<GridView<?>,HPos> getCssMetaData() {
                    return GridView.StyleableProperties.HORIZONTAL_ALIGNMENT;
                }
                
                @Override public Object getBean() {
                    return GridView.this;
                }

                @Override public String getName() {
                    return "horizontalAlignment";
                }
            };
        }
        return horizontalAlignment;
    }

    public final void setHorizontalAlignment(HPos value) {
        horizontalAlignmentProperty().set(value);
    }

    public final HPos getHorizontalAlignment() {
        return horizontalAlignment == null ? HPos.CENTER : horizontalAlignment.get();
    }

    
    // --- cell factory
    private ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactory;
    public final ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<Callback<GridView<T>, GridCell<T>>>(this, "cellFactory");
        }
        return cellFactory;
    }

    public final void setCellFactory(Callback<GridView<T>, GridCell<T>> value) {
        cellFactoryProperty().set(value);
    }

    public final Callback<GridView<T>, GridCell<T>> getCellFactory() {
        return cellFactory == null ? null : cellFactory.get();
    }

    
    // --- items
    private ObjectProperty<ObservableList<T>> items;
    public final void setItems(ObservableList<T> value) {
        itemsProperty().set(value);
    }

    public final ObservableList<T> getItems() {
        return items == null ? null : items.get();
    }

    public final ObjectProperty<ObservableList<T>> itemsProperty() {
        if (items == null) {
            items = new SimpleObjectProperty<ObservableList<T>>(this, "items");
        }
        return items;
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "grid-view";

    /** @treatAsPrivate */
    private static class StyleableProperties {
        private static final CssMetaData<GridView<?>,Number> HORIZONTAL_CELL_SPACING = 
            new CssMetaData<GridView<?>,Number>("-fx-horizontal-cell-spacing", StyleConverter.getSizeConverter(), 12d) {

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getHorizontalCellSpacing();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.horizontalCellSpacing == null || !n.horizontalCellSpacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.horizontalCellSpacingProperty();
            }
        };
        
        private static final CssMetaData<GridView<?>,Number> VERTICAL_CELL_SPACING = 
            new CssMetaData<GridView<?>,Number>("-fx-vertical-cell-spacing", StyleConverter.getSizeConverter(), 12d) {

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getVerticalCellSpacing();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.verticalCellSpacing == null || !n.verticalCellSpacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.verticalCellSpacingProperty();
            }
        };
        
        private static final CssMetaData<GridView<?>,Number> CELL_WIDTH = 
            new CssMetaData<GridView<?>,Number>("-fx-cell-width", StyleConverter.getSizeConverter(), 64d) {

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getCellWidth();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.cellWidth == null || !n.cellWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.cellWidthProperty();
            }
        };
        
        private static final CssMetaData<GridView<?>,Number> CELL_HEIGHT = 
            new CssMetaData<GridView<?>,Number>("-fx-cell-height", StyleConverter.getSizeConverter(), 64d) {

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getCellHeight();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.cellHeight == null || !n.cellHeight.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.cellHeightProperty();
            }
        };
        
        private static final CssMetaData<GridView<?>,HPos> HORIZONTAL_ALIGNMENT = 
            new CssMetaData<GridView<?>,HPos>("-fx-horizontal_alignment",
                new EnumConverter<HPos>(HPos.class), 
                HPos.CENTER) {

            @Override public HPos getInitialValue(GridView node) {
                return node.getHorizontalAlignment();
            }

            @Override public boolean isSettable(GridView n) {
                return n.horizontalAlignment == null || !n.horizontalAlignment.isBound();
            }

            @Override public StyleableProperty<HPos> getStyleableProperty(GridView n) {
                return (StyleableProperty<HPos>)n.horizontalAlignmentProperty();
            }
        };
            
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<CssMetaData<? extends Styleable, ?>>(Control.getClassCssMetaData());
            styleables.add(HORIZONTAL_CELL_SPACING);
            styleables.add(VERTICAL_CELL_SPACING);
            styleables.add(CELL_WIDTH);
            styleables.add(CELL_HEIGHT);
            styleables.add(HORIZONTAL_ALIGNMENT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}
