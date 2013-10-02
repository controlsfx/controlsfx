package org.controlsfx.control.spreadsheet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;

/**
 * When instantiating a {@link SpreadsheetCell}, its SpreadsheetCellType will condition which value the cell can accept, and which
 * {@link SpreadsheetCellEditor} it will use.
 * 
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
	 * @param converter the converter to use
	 */
	public SpreadsheetCellType(StringConverter<T> converter) {
		this.converter = converter;
	}

	/**
	 * Creates a cell that hold a <T> at the specified position, with the
	 * specified row/column span.
	 * 
	 * @param <T>
	 *            the type value class
	 * @param row
	 *            row number
	 * @param column
	 *            column number
	 * @param rs
	 *            rowSpan (1 is normal)
	 * @param cs
	 *            ColumnSpan (1 is normal)
	 * @param value
	 *            the <T> to display
	 * @return
	 */
	public abstract SpreadsheetCell<T> createCell(final int row,
			final int column, final int rowSpan, final int columnSpan,
			final T value);

	/**
	 * Gets this type String converter.
	 * @return the converter instance
	 */
	public StringConverter<T> getConverter() {
		return converter;
	}

	/**
	 * Gets this type editor.
	 * @return the editor instance
	 */
	public abstract SpreadsheetCellEditor<T> getEditor();

	/**
	 * Copies the value of a cell to another (copy/paste operations).
	 * @param from the source cell
	 * @param to the destination cell
	 */
	protected abstract void copy(SpreadsheetCell<?> from, SpreadsheetCell<T> to);
	

	/**
	 * The Object type instance.
	 */
	public static final SpreadsheetCellType<Object> OBJECT = new ObjectType();

	/**
	 * The Object type base class.
	 */
	public static class ObjectType extends SpreadsheetCellType<Object> {
		protected transient SpreadsheetCellEditor<Object> editor = null;

		public ObjectType() {
			this(
			new StringConverter<Object>() {
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

		@Override
		public String toString() {
			return "object";
		}

		protected void copy(SpreadsheetCell<?> from, SpreadsheetCell<Object> to) {
			to.setItem(from.getText());
		}

		@Override
		public SpreadsheetCell<Object> createCell(final int row,
				final int column, final int rowSpan, final int columnSpan,
				final Object value) {
			SpreadsheetCell<Object> cell = new SpreadsheetCell<Object>(row,
					column, rowSpan, columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		public SpreadsheetCellEditor<Object> getEditor() {
			if (editor == null) {
				editor = SpreadsheetCellEditor.createObjectEditor();
			}
			return editor;
		}
	};
	
	/**
	 * The String type instance.
	 */
	public static final SpreadsheetCellType<String> STRING = new StringType();
	/**
	 * The String type base class.
	 */
	public static class StringType extends SpreadsheetCellType<String> {
		protected transient SpreadsheetCellEditor<String> editor = null;

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

		protected void copy(SpreadsheetCell<?> from, SpreadsheetCell<String> to) {
			to.setItem((String) from.getText());
		}

		@Override
		public SpreadsheetCell<String> createCell(final int row,
				final int column, final int rowSpan, final int columnSpan,
				final String value) {
			SpreadsheetCell<String> cell = new SpreadsheetCell<String>(row,
					column, rowSpan, columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		@Override
		public SpreadsheetCellEditor<String> getEditor() {
			if (editor == null) {
				editor = SpreadsheetCellEditor.createTextEditor();
			}
			return editor;
		}
	};
	
	/**
	 * The Double type instance.
	 */
	public static final SpreadsheetCellType<Double> DOUBLE = new DoubleType();
	/**
	 * The Double type base class.
	 */
	public static class DoubleType extends SpreadsheetCellType<Double> {
		protected transient SpreadsheetCellEditor<Double> editor = null;

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

		@Override
		public String toString() {
			return "double";
		}

		@Override
		public SpreadsheetCell<Double> createCell(final int row,
				final int column, final int rowSpan, final int columnSpan,
				final Double value) {
			SpreadsheetCell<Double> cell = new SpreadsheetCell<Double>(row,
					column, rowSpan, columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		@Override
		public SpreadsheetCellEditor<Double> getEditor() {
			if (editor == null) {
				editor = SpreadsheetCellEditor.createDoubleEditor();
			}
			return editor;
		}

		@Override
		protected void copy(SpreadsheetCell<?> from, SpreadsheetCell<Double> to) {
			try {
				Double temp = converter.fromString(from.getText());
				to.setItem(temp);
			} catch (Exception e) {

			}
		}

	};
	
	
	public static final SpreadsheetCellType<String> LIST(final List<String> items) {
		return new ListType(items);
	}
	
	/**
	 * Creates a list type from a list of string values.
	 * @param items the list of acceptable values
	 * @return the cell type instance
	 */
	public static class ListType extends SpreadsheetCellType<String> {
		protected final List<String> items;
		protected transient SpreadsheetCellEditor<String> editor = null;

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
		public SpreadsheetCell<String> createCell(final int row,
				final int column, final int rowSpan, final int columnSpan,
				String item) {
			SpreadsheetCell<String> cell = new SpreadsheetCell<String>(row,
					column, rowSpan, columnSpan, this);
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
		public SpreadsheetCellEditor<String> getEditor() {
			if (editor == null) {
				editor = SpreadsheetCellEditor.createListEditor(items);
			}
			return editor;
		}

		@Override
		protected void copy(SpreadsheetCell<?> from, SpreadsheetCell<String> to) {
			String value = from.getText();
			if (items.contains(value)) {
				to.setItem(value);
			}
		}
	}
	
	/**
	 * The Date type instance.
	 */
	public static final SpreadsheetCellType<LocalDate> DATE = new DateType();
	/**
	 * The Date type base class.
	 */
	public static class DateType extends SpreadsheetCellType<LocalDate> {
		protected transient SpreadsheetCellEditor<LocalDate> editor = null;

		public DateType() {
			this("dd/MM/yyyy");
		}

		public DateType(final String format) {
			this( new StringConverter<LocalDate>() {
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
			} );
		}

		public DateType(StringConverter<LocalDate> converter) {
			super(converter);
		}

		@Override
		public String toString() {
			return "date";
		}

		@Override
		public SpreadsheetCell<LocalDate> createCell(final int row,
				final int column, final int rowSpan, final int columnSpan,
				final LocalDate value) {
			SpreadsheetCell<LocalDate> cell = new SpreadsheetCell<LocalDate>(
					row, column, rowSpan, columnSpan, this);
			cell.setItem(value);
			return cell;
		}

		@Override
		public SpreadsheetCellEditor<LocalDate> getEditor() {
			if (editor == null) {
				editor = SpreadsheetCellEditor.createDateEditor();
			}
			return editor;
		}

		@Override
		protected void copy(SpreadsheetCell<?> from,
				SpreadsheetCell<LocalDate> to) {
			try {
				LocalDate temp = converter.fromString(from.getText());
				if (temp != null) {
					to.setItem(temp);
				}
			} catch (Exception e) {

			}
		}
	}

}