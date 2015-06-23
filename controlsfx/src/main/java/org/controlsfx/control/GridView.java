/**
 * Copyright (c) 2013, 2015, ControlsFX
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import org.controlsfx.control.cell.ColorGridCell;

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
 * <br>
 * <img src="gridView.png" alt="Screenshot of GridView">
 * 
 * <p>To create this GridView was simple. Note that the majority of the code below
 * is related to randomly creating the colours to be represented:
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
public class GridView<T> extends ControlsFXControl {

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

    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(GridView.class, "gridview.css");
    }
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- horizontal cell spacing
    /**
     * Property for specifying how much spacing there is between each cell
     * in a row (i.e. how much horizontal spacing there is).
     */
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
                    return "horizontalCellSpacing"; //$NON-NLS-1$
                }
            };
        }
        return horizontalCellSpacing;
    }
    private DoubleProperty horizontalCellSpacing;
    
    /**
     * Sets the amount of horizontal spacing there should be between cells in
     * the same row.
     * @param value The amount of spacing to use.
     */
    public final void setHorizontalCellSpacing(double value) {
        horizontalCellSpacingProperty().set(value);
    }
    
    /**
     * Returns the amount of horizontal spacing there is between cells in
     * the same row.
     */
    public final double getHorizontalCellSpacing() {
        return horizontalCellSpacing == null ? 12.0 : horizontalCellSpacing.get();
    }


    
    // --- vertical cell spacing
    /**
     * Property for specifying how much spacing there is between each cell
     * in a column (i.e. how much vertical spacing there is).
     */
    private DoubleProperty verticalCellSpacing;
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
                    return "verticalCellSpacing"; //$NON-NLS-1$
                }
            };
        }
        return verticalCellSpacing;
    }
    
    /**
     * Sets the amount of vertical spacing there should be between cells in
     * the same column.
     * @param value The amount of spacing to use.
     */
    public final void setVerticalCellSpacing(double value) {
        verticalCellSpacingProperty().set(value);
    }

    /**
     * Returns the amount of vertical spacing there is between cells in
     * the same column.
     */
    public final double getVerticalCellSpacing() {
        return verticalCellSpacing == null ? 12.0 : verticalCellSpacing.get();
    }

    
    
    // --- cell width
    /**
     * Property representing the width that all cells should be.
     */
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
                    return "cellWidth"; //$NON-NLS-1$
                }
            };
        }
        return cellWidth;
    }
    private DoubleProperty cellWidth;

    /**
     * Sets the width that all cells should be.
     */
    public final void setCellWidth(double value) {
        cellWidthProperty().set(value);
    }

    /**
     * Returns the width that all cells should be.
     */
    public final double getCellWidth() {
        return cellWidth == null ? 64.0 : cellWidth.get();
    }

    
    // --- cell height
    /**
     * Property representing the height that all cells should be.
     */
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
                    return "cellHeight"; //$NON-NLS-1$
                }
            };
        }
        return cellHeight;
    }
    private DoubleProperty cellHeight;

    /**
     * Sets the height that all cells should be.
     */
    public final void setCellHeight(double value) {
        cellHeightProperty().set(value);
    }

    /**
     * Returns the height that all cells should be.
     */
    public final double getCellHeight() {
        return cellHeight == null ? 64.0 : cellHeight.get();
    }

    
    // I've removed this functionality until there is a clear need for it.
    // To re-enable it, there is code in GridRowSkin that has been commented
    // out that must be re-enabled.
    // Don't forget also to enable the styleable property further down in this
    // class.
//    // --- horizontal alignment
//    private ObjectProperty<HPos> horizontalAlignment;
//    public final ObjectProperty<HPos> horizontalAlignmentProperty() {
//        if (horizontalAlignment == null) {
//            horizontalAlignment = new StyleableObjectProperty<HPos>(HPos.CENTER) {
//                @Override public CssMetaData<GridView<?>,HPos> getCssMetaData() {
//                    return GridView.StyleableProperties.HORIZONTAL_ALIGNMENT;
//                }
//                
//                @Override public Object getBean() {
//                    return GridView.this;
//                }
//
//                @Override public String getName() {
//                    return "horizontalAlignment";
//                }
//            };
//        }
//        return horizontalAlignment;
//    }
//
//    public final void setHorizontalAlignment(HPos value) {
//        horizontalAlignmentProperty().set(value);
//    }
//
//    public final HPos getHorizontalAlignment() {
//        return horizontalAlignment == null ? HPos.CENTER : horizontalAlignment.get();
//    }

    
    // --- cell factory
    /**
     * Property representing the cell factory that is currently set in this
     * GridView, or null if no cell factory has been set (in which case the 
     * default cell factory provided by the GridView skin will be used). The cell
     * factory is used for instantiating enough GridCell instances for the 
     * visible area of the GridView. Refer to the GridView class documentation
     * for more information and examples.
     */
    public final ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<>(this, "cellFactory"); //$NON-NLS-1$
        }
        return cellFactory;
    }
    private ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactory;

    /**
     * Sets the cell factory to use to create {@link GridCell} instances to 
     * show in the GridView.
     */
    public final void setCellFactory(Callback<GridView<T>, GridCell<T>> value) {
        cellFactoryProperty().set(value);
    }

    /**
     * Returns the cell factory that will be used to create {@link GridCell} 
     * instances to show in the GridView.
     */
    public final Callback<GridView<T>, GridCell<T>> getCellFactory() {
        return cellFactory == null ? null : cellFactory.get();
    }

    
    // --- items
    /**
     * The items to be displayed in the GridView (as rendered via {@link GridCell}
     * instances). For example, if the {@link ColorGridCell} were being used
     * (as in the case at the top of this class documentation), this items list
     * would be populated with {@link Color} values. It is important to 
     * appreciate that the items list is used for the data, not the rendering.
     * What is meant by this is that the items list should contain Color values,
     * not the {@link Node nodes} that represent the Color. The actual rendering
     * should be left up to the {@link #cellFactoryProperty() cell factory},
     * where it will take the Color value and create / update the display as
     * necessary. 
     */
    public final ObjectProperty<ObservableList<T>> itemsProperty() {
        if (items == null) {
            items = new SimpleObjectProperty<>(this, "items"); //$NON-NLS-1$
        }
        return items;
    }
    private ObjectProperty<ObservableList<T>> items;
    
    /**
     * Sets a new {@link ObservableList} as the items list underlying GridView.
     * The old items list will be discarded.
     */
    public final void setItems(ObservableList<T> value) {
        itemsProperty().set(value);
    }

    /**
     * Returns the currently-in-use items list that is being used by the
     * GridView.
     */
    public final ObservableList<T> getItems() {
        return items == null ? null : items.get();
    }

    
    
    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "grid-view"; //$NON-NLS-1$

    /** @treatAsPrivate */
    private static class StyleableProperties {
        private static final CssMetaData<GridView<?>,Number> HORIZONTAL_CELL_SPACING = 
            new CssMetaData<GridView<?>,Number>("-fx-horizontal-cell-spacing", StyleConverter.getSizeConverter(), 12d) { //$NON-NLS-1$

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getHorizontalCellSpacing();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.horizontalCellSpacing == null || !n.horizontalCellSpacing.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.horizontalCellSpacingProperty();
            }
        };
        
        private static final CssMetaData<GridView<?>,Number> VERTICAL_CELL_SPACING = 
            new CssMetaData<GridView<?>,Number>("-fx-vertical-cell-spacing", StyleConverter.getSizeConverter(), 12d) { //$NON-NLS-1$

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getVerticalCellSpacing();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.verticalCellSpacing == null || !n.verticalCellSpacing.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.verticalCellSpacingProperty();
            }
        };
        
        private static final CssMetaData<GridView<?>,Number> CELL_WIDTH = 
            new CssMetaData<GridView<?>,Number>("-fx-cell-width", StyleConverter.getSizeConverter(), 64d) { //$NON-NLS-1$

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getCellWidth();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.cellWidth == null || !n.cellWidth.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.cellWidthProperty();
            }
        };
        
        private static final CssMetaData<GridView<?>,Number> CELL_HEIGHT = 
            new CssMetaData<GridView<?>,Number>("-fx-cell-height", StyleConverter.getSizeConverter(), 64d) { //$NON-NLS-1$

            @Override public Double getInitialValue(GridView<?> node) {
                return node.getCellHeight();
            }

            @Override public boolean isSettable(GridView<?> n) {
                return n.cellHeight == null || !n.cellHeight.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Number> getStyleableProperty(GridView<?> n) {
                return (StyleableProperty<Number>)n.cellHeightProperty();
            }
        };
        
//        private static final CssMetaData<GridView<?>,HPos> HORIZONTAL_ALIGNMENT = 
//            new CssMetaData<GridView<?>,HPos>("-fx-horizontal_alignment",
//                new EnumConverter<HPos>(HPos.class), 
//                HPos.CENTER) {
//
//            @Override public HPos getInitialValue(GridView node) {
//                return node.getHorizontalAlignment();
//            }
//
//            @Override public boolean isSettable(GridView n) {
//                return n.horizontalAlignment == null || !n.horizontalAlignment.isBound();
//            }
//
//            @Override public StyleableProperty<HPos> getStyleableProperty(GridView n) {
//                return (StyleableProperty<HPos>)n.horizontalAlignmentProperty();
//            }
//        };
            
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(HORIZONTAL_CELL_SPACING);
            styleables.add(VERTICAL_CELL_SPACING);
            styleables.add(CELL_WIDTH);
            styleables.add(CELL_HEIGHT);
//            styleables.add(HORIZONTAL_ALIGNMENT);
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
