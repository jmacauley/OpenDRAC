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

package com.nortel.appcore.app.drac.database;

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author
 */
public class DbBatchWithResultsAdapter implements DbBatchWithResultsI {
  private final Logger log = LoggerFactory.getLogger(getClass());
	int batchCount;

	public DbBatchWithResultsAdapter(int batchCount) {
		this.batchCount = batchCount;
	}

	@Override
	public PreparedStatement buildPreparedStatement(PreparedStatement statement)
	    throws Exception {
		return statement;
	}

	@Override
	public void setUpdateCount(int[] results) throws Exception {
		if (batchCount != results.length) {
			log.error(
			    "Not all batch records have been processed. Check JDBC driver settings. batchCount="
			        + batchCount + " results=" + results.length, new Exception(
			        "Stack trace back"));
		}
	}

}