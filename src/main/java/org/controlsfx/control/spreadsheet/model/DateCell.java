/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.controlsfx.control.spreadsheet.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * Specialization of the DataCell class.
 * It holds a String.
 */
public class DateCell extends DataCell<LocalDate> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1711498694430990374L;

	private LocalDate value;

	public DateCell(int r, int c, int rs, int cs) {
		super(r, c, rs, cs);
		this.type = CellType.DATE;
		this.setCellValue(LocalDate.now());
	}

	@Override
	public void setCellValue(LocalDate value) {
		this.value = value;
		this.str = value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	@Override
	public LocalDate getCellValue() {
		return value;
	}

	@Override
	public void match(DataCell<?> cell) {
		setStr(cell.getStr());
	}

}
