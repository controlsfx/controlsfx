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
package org.controlsfx.control.spreadsheet;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * When instantiating a {@link SpreadsheetCell}, its SpreadsheetCellType will
 * specify which values the cell can accept as user input, and which
 * {@link SpreadsheetCellEditor} it will use to receive user input.
 * 
 * FIXME Modify description <h3>Example</h3> You can create several types which
 * are using the same editor. Suppose you want to handle Double values. You will
 * implement the {@link #createEditor(SpreadsheetView)} method and use the
 * {@link SpreadsheetCellEditor#createDoubleEditor(SpreadsheetView)}. <br/>
 * 
 * Then for each type you will provide your own policy in
 * {@link #convertValue(String)}, which most of the time will use your
 * {@link #converter}. If you only want to accept values between 0 and 10:
 * 
 * <pre>
 * converter = new DoubleStringConverter() {
 *     &#64;Override public String toString(Double item) {
 *         if (item == null || Double.isNaN(item)) {
 *             return "";
 *         } else {
 *             return super.toString(item);
 *         }
 *    }
 * 
 *     &#64;Override public Double fromString(String str) {
 *         if (str == null || str.isEmpty() || "NaN".equals(str)) {
 *             return Double.NaN;
 *         } else {
 *             return super.fromString(str);
 *         }
 *     }
 * });
 * 			
 * &#64;Override public Double convertValue(String value) {
 *     try {
 *         Double computedValue = converter.fromString(value);
 *         if (computedValue >=0 && computedValue <=10) {
 *             return computedValue;
 *         } else {
 *             return null;
 *     } catch (Exception e) {
 *         return null;
 *     }
 * }
 * </pre>
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 * @see SpreadsheetCell
 */
public abstract class SpreadsheetCellType<T> {
    /** An instance of converter from string to cell type. */
    protected StringConverter<T> converter;

    /**
     * Default constructor.
     */
    public SpreadsheetCellType(){
        
    }
    /**
     * Constructor with the StringConverter directly provided.
     * 
     * @param converter
     *            The converter to use
     */
    public SpreadsheetCellType(StringConverter<T> converter) {
        this.converter = converter;
    }

    /**
     * Creates a cell that hold a <T> at the specified position, with the
     * specified row/column span.
     * 
     * @param row
     *            row number
     * @param column
     *            column number
     * @param rowSpan
     *            rowSpan (1 is normal)
     * @param columnSpan
     *            ColumnSpan (1 is normal)
     * @param value
     *            the value to display
     * @return a {@link SpreadsheetCell}
     */
    public abstract SpreadsheetCell createCell(final int row, final int column, final int rowSpan,
            final int columnSpan, final T value);

    /**
     * Creates an editor for this type of cells.
     * 
     * @param view
     *            the spreadsheet that will own this editor
     * @return the editor instance
     */
    public abstract SpreadsheetCellEditor createEditor(SpreadsheetView view);

    /**
     * Return a string representation of the given item for the
     * {@link SpreadsheetView} to display using the inner
     * {@link SpreadsheetCellType#converter} and the specified format.
     * 
     * @param object
     * @param format
     * @return a string representation of the given item.
     */
    public String toString(T object, String format) {
        return toString(object);
    }

    /**
     * Return a string representation of the given item for the
     * {@link SpreadsheetView} to display using the inner
     * {@link SpreadsheetCellType#converter}.
     * 
     * @param object
     * @return a string representation of the given item.
     */
    public abstract String toString(T object);

    /**
     * Verify that the upcoming value can be set to the current cell. This is
     * the first level of verification to prevent affecting a text to a double
     * or a double to a date. For closer verification, use
     * {@link #isError(Object)}.
     * 
     * @param value
     *            the value to test
     * @return true if it matches.
     */
    public abstract boolean match(Object value);

    /**
     * Returns true if the value is an error regarding the specification of its
     * type.
     * 
     * @param value
     * @return true if the value is an error.
     */
    public boolean isError(Object value) {
        return false;
    }

    /**
     * This method will be called when a commit is happening.<br/>
     * This method will try to convert the value, be sure to call
     * {@link #match(Object)} before to see if this method will succeed.
     * 
     * @param value
     * @return null if not valid or the correct value otherwise.
     */
    public abstract T convertValue(Object value);

    /**
     * The {@link SpreadsheetCell} {@link Object} type instance.
     */
    public static final SpreadsheetCellType<Object> OBJECT = new ObjectType();

    /**
     * The {@link SpreadsheetCell} {@link Object} type base class.
     */
    public static class ObjectType extends SpreadsheetCellType<Object> {

        public ObjectType() {
            this(new StringConverterWithFormat<Object>() {
                @Override
                public Object fromString(String arg0) {
                    return arg0;
                }

                @Override
                public String toString(Object arg0) {
                    return arg0 == null ? "" : arg0.toString();
                }
            });
        }

        public ObjectType(StringConverterWithFormat<Object> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "object";
        }

        @Override
        public boolean match(Object value) {
            return true;
        }

        @Override
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final Object value) {
            SpreadsheetCell cell = new SpreadsheetCell(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.ObjectEditor(view);
        }

        @Override
        public Object convertValue(Object value) {
            return value;
        }

        @Override
        public String toString(Object item) {
            return converter.toString(item);
        }

    };

    /**
     * The {@link SpreadsheetCell} {@link String} type instance.
     */
    public static final SpreadsheetCellType<String> STRING = new StringType();

    /**
     * The {@link SpreadsheetCell} {@link String} type base class.
     */
    public static class StringType extends SpreadsheetCellType<String> {

        public StringType() {
            this(new DefaultStringConverter());
        }

        public StringType(StringConverter<String> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "string";
        }

        @Override
        public boolean match(Object value) {
            return true;
        }

        @Override
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final String value) {
            SpreadsheetCell cell = new SpreadsheetCell(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.StringEditor(view);
        }

        @Override
        public String convertValue(Object value) {
            String convertedValue = converter.fromString(value == null ? null : value.toString());
            if (convertedValue == null || convertedValue.equals("")) {
                return null;
            }
            return convertedValue;
        }

        @Override
        public String toString(String item) {
            return converter.toString(item);
        }

    };

    /**
     * The {@link SpreadsheetCell} {@link Double} type instance.
     */
    public static final SpreadsheetCellType<Double> DOUBLE = new DoubleType();

    /**
     * The {@link SpreadsheetCell} {@link Double} type base class.
     */
    public static class DoubleType extends SpreadsheetCellType<Double> {

        public DoubleType() {

            this(new StringConverterWithFormat<Double>(new DoubleStringConverter()) {
                @Override
                public String toString(Double item) {
                    return toStringFormat(item, "");
                }

                @Override
                public Double fromString(String str) {
                    if (str == null || str.isEmpty() || "NaN".equals(str)) {
                        return Double.NaN;
                    } else {
                        return myConverter.fromString(str);
                    }
                }

                @Override
                public String toStringFormat(Double item, String format) {
                    try {
                        if (item == null || Double.isNaN(item)) {
                            return "";
                        } else {
                            return new DecimalFormat(format).format(item);
                        }
                    } catch (Exception ex) {
                        return myConverter.toString(item);
                    }
                }
            });
        }

        public DoubleType(StringConverter<Double> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "double";
        }

        @Override
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final Double value) {
            SpreadsheetCell cell = new SpreadsheetCell(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.DoubleEditor(view);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof Double)
                return true;
            else {
                try {
                    converter.fromString(value == null ? null : value.toString());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public Double convertValue(Object value) {
            if (value instanceof Double)
                return (Double) value;
            else {
                try {
                    return converter.fromString(value == null ? null : value.toString());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public String toString(Double item) {
            return converter.toString(item);
        }

        @Override
        public String toString(Double item, String format) {
            return ((StringConverterWithFormat<Double>) converter).toStringFormat(item, format);
        }
    };

    /**
     * The {@link SpreadsheetCell} {@link Integer} type instance.
     */
    public static final SpreadsheetCellType<Integer> INTEGER = new IntegerType();

    /**
     * The {@link SpreadsheetCell} {@link Integer} type base class.
     */
    public static class IntegerType extends SpreadsheetCellType<Integer> {

        public IntegerType() {
            this(new IntegerStringConverter() {
                @Override
                public String toString(Integer item) {
                    if (item == null || Double.isNaN(item)) {
                        return "";
                    } else {
                        return super.toString(item);
                    }
                }

                @Override
                public Integer fromString(String str) {
                    if (str == null || str.isEmpty() || "NaN".equals(str)) {
                        return null;
                    } else {
                        // We try to integrate Double if possible by truncating
                        // them
                        try {
                            Double temp = Double.parseDouble(str);
                            return temp.intValue();
                        } catch (Exception e) {
                            return super.fromString(str);
                        }
                    }
                }
            });
        }

        public IntegerType(IntegerStringConverter converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "Integer";
        }

        @Override
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final Integer value) {
            SpreadsheetCell cell = new SpreadsheetCell(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.IntegerEditor(view);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof Integer)
                return true;
            else {
                try {
                    converter.fromString(value == null ? null : value.toString());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public Integer convertValue(Object value) {
            if (value instanceof Integer)
                return (Integer) value;
            else {
                try {
                    return converter.fromString(value == null ? null : value.toString());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public String toString(Integer item) {
            return converter.toString(item);
        }
    };

    /**
     * Creates a {@link ListType}.
     * 
     * @param items
     *            the list items
     * @return the instance
     */
    public static final SpreadsheetCellType<String> LIST(final List<String> items) {
        return new ListType(items);
    }

    /**
     * The {@link SpreadsheetCell} {@link List} type base class.
     */
    public static class ListType extends SpreadsheetCellType<String> {
        protected final List<String> items;

        public ListType(final List<String> items) {
            super(new DefaultStringConverter() {
                @Override
                public String fromString(String str) {
                    if (str != null && items.contains(str)) {
                        return str;
                    } else {
                        return null;
                    }
                }

            });
            this.items = items;
        }

        @Override
        public String toString() {
            return "list";
        }

        @Override
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                String item) {
            SpreadsheetCell cell = new SpreadsheetCell(row, column, rowSpan, columnSpan, this);
            if (items != null && items.size() > 0) {
                if (item != null && items.contains(item)) {
                    cell.setItem(item);
                } else {
                    cell.setItem(items.get(0));
                }
            }
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.ListEditor<String>(view, items);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof String && items.contains(value))
                return true;
            else
                return items.contains(value == null ? null : value.toString());
        }

        @Override
        public String convertValue(Object value) {
            return converter.fromString(value == null ? null : value.toString());
        }

        @Override
        public String toString(String item) {
            return converter.toString(item);
        }
    }

    /**
     * The {@link SpreadsheetCell} {@link LocalDate} type instance.
     */
    public static final SpreadsheetCellType<LocalDate> DATE = new DateType();

    /**
     * The {@link SpreadsheetCell} {@link LocalDate} type base class.
     */
    public static class DateType extends SpreadsheetCellType<LocalDate> {

        /**
         * Creates a new DateType.
         */
        public DateType() {
            this(new StringConverterWithFormat<LocalDate>() {
                @Override
                public String toString(LocalDate item) {
                    return toStringFormat(item, "");
                }

                @Override
                public LocalDate fromString(String str) {
                    try {
                        return LocalDate.parse(str);
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                public String toStringFormat(LocalDate item, String format) {
                    if (("").equals(format)) {
                        return item.toString();
                    } else if (item != null) {
                        return item.format(DateTimeFormatter.ofPattern(format));
                    } else {
                        return "";
                    }
                }
            });
        }

        public DateType(StringConverter<LocalDate> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "date";
        }

        @Override
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final LocalDate value) {
            SpreadsheetCell cell = new SpreadsheetCell(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.DateEditor(view, converter);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof LocalDate)
                return true;
            else {
                try {
                    LocalDate temp = converter.fromString(value == null ? null : value.toString());
                    return temp != null;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public LocalDate convertValue(Object value) {
            if (value instanceof LocalDate)
                return (LocalDate) value;
            else {
                try {
                    return converter.fromString(value == null ? null : value.toString());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public String toString(LocalDate item) {
            return converter.toString(item);
        }

        @Override
        public String toString(LocalDate item, String format) {
            return ((StringConverterWithFormat<LocalDate>) converter).toStringFormat(item, format);
        }
    }
}