/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.client.lpcpadminconsole.common;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * A TableModel that uses List<> not Vector and supports generics. I was getting
 * tired of the pmd warnings suggesting we should not be using Vector anymore.
 * 
 * @author pitman
 */
public final class DracTableModel<T> extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<List<T>> table = new ArrayList<List<T>>();
	private final List<String> cols;

	public DracTableModel(List<List<T>> t, List<String> columnIdentifiers) {
		cols = columnIdentifiers;
		setData(t);
	}

	public void clearTable() {
		setData(null);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// Humm would be nice if we could indicate all columns are of type <X>. Not
		// sure how to do that.
		return Object.class;
	}

	@Override
	public int getColumnCount() {
		return cols.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < cols.size()) {
			return cols.get(columnIndex);
		}
		return super.getColumnName(columnIndex);
	}

	@Override
	public int getRowCount() {
		return table.size();
	}

	@Override
	public T getValueAt(int rowIndex, int columnIndex) {
		List<T> row = table.get(rowIndex);
		return row.get(columnIndex);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	// public void setData(List<List<X>> t, List<String> columnIdentifiers)
	// {
	// table = t == null ? new ArrayList<List<X>>() : t;
	// cols = columnIdentifiers;
	// sanitize();
	// fireTableStructureChanged();
	// }

	public void setData(List<List<T>> t) {
		table = t == null ? new ArrayList<List<T>>() : t;
		fireTableStructureChanged();
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		List<T> row = table.get(rowIndex);
		// To satisty the method signature (extends AbstractTableModel), aValue must
		// be Object.
		// But, this class defines the backing data store using generic type T.
		// Have to perform unsafe cast of Object to T here...
		row.set(columnIndex, (T) aValue);

		fireTableCellUpdated(rowIndex, columnIndex);

	}
	
}
