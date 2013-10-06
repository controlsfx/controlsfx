package org.controlsfx.control.spreadsheet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;

/**
 * When instantiating a {@link SpreadsheetCell}, its SpreadsheetCellType will
 * condition which value the cell can accept, and which
 * {@link SpreadsheetCellEditor} it will use. <br/>
 * 
 * <h3>Example</h3> You can create several types which are using the same
 * editor. Suppose you want to handle Double values. You will implement the
 * {@link #createEditor(SpreadsheetView)} method and use the
 * {@link SpreadsheetCellEditor#createDoubleEditor(SpreadsheetView)}. <br/>
 * 
 * Then for each type you will provide your own policy in
 * {@link #convertValue(String)}, which most of the time will use your
 * {@link #converter}. If you only want to accept values between 0 and 10:
 * 
 * <pre>
 *  converter = new DoubleStringConverter() {
 * 				Override
 * 				public String toString(Double item) {
 * 					if (item == null || Double.isNaN(item)) {
 * 						return "";
 * 					} else {
 * 						return super.toString(item);
 * 					}
 * 				}
 * 
 * 				Override
 * 				public Double fromString(String str) {
 * 					if (str == null || str.isEmpty() || "NaN".equals(str)) {
 * 						return Double.NaN;
 * 					} else {
 * 						return super.fromString(str);
 * 					}
 * 				}
 * 			});
 * 			
 * Override
 * public Double convertValue(String value) {
 * 	try {
 * 			Double computedValue = converter.fromString(value);
 * 			if(computedValue >=0 && computedValue <=10)
 * 				return computedValue;
 * 			else
 * 				return null;
 * 		} catch (Exception e) {
 * 			return null;
 * 		}
 * }
 * </pre>
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 * @see SpreadsheetCell
 */
public abstract class SpreadsheetCellType<T> {
	/** An instance of converter from string to cell type. */
	protected transient StringConverter<T> converter;

	/**
	 * Default constructor.
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
	public abstract SpreadsheetCell createCell(final int row, final int column,
			final int rowSpan, final int columnSpan, final T value);

	/**
	 * Creates an editor for this type of cells.
	 * @param view the spreadsheet that will own this editor
	 * @return the editor instance
	 */
	public abstract SpreadsheetCellEditor<T> createEditor(SpreadsheetView view);

	/**
	 * Return a string representation of the given item for the
	 * {@link SpreadsheetView} to display using the converter.
	 * 
	 * @param item
	 * @return a string representation of the given item.
	 */
	public abstract String toString(T item);

	/**
	 * Copies the value of a cell to another (copy/paste operations).
	 * 
	 * @param from
	 *            the source cell
	 * @param to
	 *            the destination cell
	 */
	protected abstract void copy(SpreadsheetCell<T> from, SpreadsheetCell<T> to);

	/**
	 * This method will be called when a commit is happening.<br/>
	 * You will then compute the value of the editor in order to determine if
	 * the current value is valid.
	 * 
	 * @return null if not valid or the correct value otherwise.
	 */
	public abstract T convertValue(String value);

	/**
	 * The {@link SpreadsheetCell} {@link Object} type instance.
	 */
	public static final SpreadsheetCellType<Object> OBJECT = new ObjectType();

	/**
	 * The {@link SpreadsheetCell} {@link Object} type base class.
	 */
	public static class ObjectType extends SpreadsheetCellType<Object> {

		public ObjectType() {
			this(new StringConverter<Object>() {
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

		public ObjectType(StringConverter<Object> converter) {
			super(converter);
		}

		@Override public String toString() {
			return "object";
		}

		protected void copy(SpreadsheetCell<Object> from, SpreadsheetCell<Object> to) {
			to.setItem(from.getText());
		}

		@Override
		public SpreadsheetCell<Object> createCell(final int row, final int column,
				final int rowSpan, final int columnSpan, final Object value) {
			SpreadsheetCell<Object> cell = new SpreadsheetCell<>(row, column, rowSpan,columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		@Override public SpreadsheetCellEditor<Object> createEditor(SpreadsheetView view) {
			return new SpreadsheetCellEditor.ObjectEditor(view);
		}

		@Override public Object convertValue(String value) {
			return converter.fromString(value);
		}

		@Override public String toString(Object item) {
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

		@Override public String toString() {
			return "string";
		}

		protected void copy(SpreadsheetCell<String> from, SpreadsheetCell<String> to) {
			to.setItem(from.getText());
		}

		@Override
		public SpreadsheetCell<String> createCell(final int row, final int column,
				final int rowSpan, final int columnSpan, final String value) {
			SpreadsheetCell<String> cell = new SpreadsheetCell<>(row, column, rowSpan, columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		@Override public SpreadsheetCellEditor<String> createEditor(SpreadsheetView view) {
			return new SpreadsheetCellEditor.StringEditor(view);
		}

		@Override public String convertValue(String value) {
			return value;
		}

		@Override public String toString(String item) {
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
			this(new DoubleStringConverter() {
				@Override
				public String toString(Double item) {
					if (item == null || Double.isNaN(item)) {
						return "";
					} else {
						return super.toString(item);
					}
				}

				@Override
				public Double fromString(String str) {
					if (str == null || str.isEmpty() || "NaN".equals(str)) {
						return Double.NaN;
					} else {
						return super.fromString(str);
					}
				}
			});
		}

		public DoubleType(DoubleStringConverter converter) {
			super(converter);
		}

		@Override public String toString() {
			return "double";
		}

		@Override
		public SpreadsheetCell<Double> createCell(final int row, final int column,
				final int rowSpan, final int columnSpan, final Double value) {
			SpreadsheetCell<Double> cell = new SpreadsheetCell<>(row, column, rowSpan,columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		@Override public SpreadsheetCellEditor<Double> createEditor(SpreadsheetView view) {
			return new SpreadsheetCellEditor.DoubleEditor(view);
		}

		@Override protected void copy(SpreadsheetCell<Double> from, SpreadsheetCell<Double> to) {
			try {
				Double temp = converter.fromString(from.getText());
				to.setItem(temp);
			} catch (Exception e) {

			}
		}

		@Override public Double convertValue(String value) {
			try {
				return converter.fromString(value);
			} catch (Exception e) {
				return null;
			}
		}

		@Override public String toString(Double item) {
			return converter.toString(item);
		}
	};

	/**
	 * Creates a {@link ListType}.
	 * @param items the list items
	 * @return the instance
	 */
	public static final SpreadsheetCellType<String> LIST(
			final List<String> items) {
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

		@Override public String toString() {
			return "list";
		}

		@Override
		public SpreadsheetCell<String> createCell(final int row, final int column,
				final int rowSpan, final int columnSpan, String item) {
			SpreadsheetCell<String> cell = new SpreadsheetCell<>(row, column, rowSpan, columnSpan, this);
			if (items != null && items.size() > 0) {
				if (item != null && items.contains(item)) {
					cell.setItem(item);
				} else {
					cell.setItem(items.get(0));
				}
			}
			return cell;
		}

		@Override public SpreadsheetCellEditor<String> createEditor(SpreadsheetView view) {
			return new SpreadsheetCellEditor.ListEditor(view, items);
		}

		@Override protected void copy(SpreadsheetCell<String> from, SpreadsheetCell<String> to) {
			String value = from.getText();
			if (items.contains(value)) {
				to.setItem(value);
			}
		}

		@Override public String convertValue(String value) {
			return converter.fromString(value);
		}

		@Override public String toString(String item) {
			return converter.toString(item);
		}

	}

	/**
	 * The {@link SpreadsheetCell} {@link java.time.LocalDate} type instance.
	 */
	public static final SpreadsheetCellType<LocalDate> DATE = new DateType();

	/**
	 * The {@link SpreadsheetCell} {@link java.time.LocalDate} type base class.
	 */
	public static class DateType extends SpreadsheetCellType<LocalDate> {

		public DateType() {
			this("dd/MM/yyyy");
		}

		/**
		 * Creates a new DateType.
		 * @param format the date format
		 */
		public DateType(final String format) {
			this(new StringConverter<LocalDate>() {
				@Override
				public String toString(LocalDate item) {
					return item.format(DateTimeFormatter.ofPattern(format));
				}

				@Override
				public LocalDate fromString(String str) {
					try {
						return LocalDate.parse(str,
								DateTimeFormatter.ofPattern(format));
					} catch (Exception e) {
						return null;
					}
				}
			});
		}

		public DateType(StringConverter<LocalDate> converter) {
			super(converter);
		}

		@Override public String toString() {
			return "date";
		}

		@Override
		public SpreadsheetCell<LocalDate> createCell(final int row, final int column,
				final int rowSpan, final int columnSpan, final LocalDate value) {
			SpreadsheetCell<LocalDate> cell = new SpreadsheetCell<>(row, column, rowSpan, columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		@Override public SpreadsheetCellEditor<LocalDate> createEditor(SpreadsheetView view) {
			return new SpreadsheetCellEditor.DateEditor(view);
		}

		@Override protected void copy(SpreadsheetCell<LocalDate> from, SpreadsheetCell<LocalDate> to) {
			try {
				LocalDate temp = converter.fromString(from.getText());
				if (temp != null) {
					to.setItem(temp);
				}
			} catch (Exception e) {

			}
		}

		@Override
		public LocalDate convertValue(String value) {
			try {
				return converter.fromString(value);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public String toString(LocalDate item) {
			return converter.toString(item);
		}
	}
}