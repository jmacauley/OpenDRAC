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

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import org.opendrac.ioc.IocContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pitman
 */
public enum DbOperationsManager {

	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(DbOperationsManager.class);

	private final DataSource dataSource = IocContainer.getBean("dataSource",
	    DataSource.class);

	public void executeDbBatchWithResults(String sqlStatement,
	    DbBatchWithResultsI operation) throws Exception {
		Connection connection = null;
		int[] results = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement(sqlStatement);
			preparedStatement = operation.buildPreparedStatement(preparedStatement);
			results = preparedStatement.executeBatch();
			operation.setUpdateCount(results);
		}
		catch (BatchUpdateException be) {
			/*
			 * Interface Statement::executeBatch If one of the commands in a batch
			 * update fails to execute properly, this method throws a
			 * BatchUpdateException, and a JDBC driver may or may not continue to
			 * process the remaining commands in the batch. However, the driver's
			 * behavior must be consistent with a particular DBMS, either always
			 * continuing to process commands or never continuing to process commands.
			 * If the driver continues processing after a failure, the array returned
			 * by the method BatchUpdateException.getUpdateCounts will contain as many
			 * elements as there are commands in the batch, and at least one of the
			 * elements will be the following:
			 */
			results = be.getUpdateCounts();
			operation.setUpdateCount(results);
			throw be;
		}
		finally {
			close(connection, preparedStatement);
			if (results != null) {
				for (int result : results) {
					if (result < 0 && result != Statement.SUCCESS_NO_INFO) {
						// error on statement
						log.error("One or more of the batched Database operations failed.");
						break;
					}
				}
			}
		}
	}

	public void executeDbOpWithResults(String sqlStatement,
	    DbOpWithResultsI operation) throws Exception {
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement(sqlStatement);
			preparedStatement = operation.buildPreparedStatement(preparedStatement);
			if (preparedStatement.execute()) {
				operation.processResults(preparedStatement.getResultSet());
			}
			operation.setUpdateCount(preparedStatement.getUpdateCount());
		}
		finally {
			close(connection, preparedStatement);
		}
	}

	public void close(Connection connection, PreparedStatement preparedStatement) {
		try {
			if (preparedStatement != null && preparedStatement.getResultSet() != null) {
				preparedStatement.getResultSet().close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			connection.close();
		}
		catch (Exception e) {
			log.warn("Error closing connection:" + e.getMessage());
		}

	}

	public DataSource getDatasource() {
		return dataSource;
	}

}
