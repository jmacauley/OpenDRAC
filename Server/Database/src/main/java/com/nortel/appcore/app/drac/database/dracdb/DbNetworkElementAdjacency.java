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

package com.nortel.appcore.app.drac.database.dracdb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsI;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

public enum DbNetworkElementAdjacency {
  INSTANCE;
  private static final String TABLENAME = "NetworkElementAdjacency";
  private static final String PK = "pk";

  public void add(final List<NetworkElementAdjacency> data) throws Exception {
    String sql = "insert into " + TABLENAME + " ( " + DbNetworkElementAdjacency.PK + ", "
        + DbKeys.NetworkElementAdjacencyColsV2.NEID + ", " + DbKeys.NetworkElementAdjacencyColsV2.PORT + ", "
        + DbKeys.NetworkElementAdjacencyColsV2.TXTAG + ", " + DbKeys.NetworkElementAdjacencyColsV2.RXTAG + ", "
        + DbKeys.NetworkElementAdjacencyColsV2.TYPE + ", " + DbKeys.NetworkElementAdjacencyColsV2.MANUALPROVISION
        + ") values (?, ?, ?, ?, ?, ?, ?);";

    DbBatchWithResultsI dbBatch = new DbBatchWithResultsAdapter(data.size()) {
      @Override
      public PreparedStatement buildPreparedStatement(PreparedStatement stmt) throws Exception {
        for (NetworkElementAdjacency adj : data) {
          stmt.setString(1, encodePK(adj.getNeid(), adj.getPort(), adj.getType()));
          stmt.setString(2, adj.getNeid());
          stmt.setString(3, adj.getPort());
          stmt.setString(4, adj.getTxTag());
          stmt.setString(5, adj.getRxTag());
          stmt.setString(6, adj.getType());
          stmt.setBoolean(7, adj.isManual());

          stmt.addBatch();
        }

        return stmt;
      }
    };

    try {
      DbOperationsManager.INSTANCE.executeDbBatchWithResults(sql, dbBatch);
    }
    catch (SQLException e) {
      // These checks are SQL and vendor specific (so they need to be done
      // here).
      // With manual link creation, user could easily attempt to enter
      // duplicate
      // adjacencies. Give them a friendlier response...

      int errorCode = e.getErrorCode();
      String msg = e.getMessage();
      boolean duplicate = false;

      if (errorCode == 1062) {
        // Error: 1062 SQLSTATE: 23000 (ER_DUP_ENTRY)
        duplicate = true;
      }

      if (msg != null && msg.toUpperCase().contains("DUPLICATE ENTRY")) {
        duplicate = true;
      }

      if (duplicate) {
        throw new Exception(
            "The add adjacency request was rejected because one or more records was found to be a duplicate.");
      }
      else {
        throw e;
      }
    }
  }

  public void delete(final String neid) throws Exception {
    delete(neid, null, null);
  }

  public void delete(final String neid, final String port, final String type) throws Exception {
    String sql;

    if (port != null) {
      sql = "delete from " + TABLENAME + " where " + DbNetworkElementAdjacency.PK + " LIKE ?;";
    }

    else {
      // Full wipe for NE, but do not delete manual links.
      sql = "delete from " + TABLENAME + " where " + DbNetworkElementAdjacency.PK + " LIKE ? and "
          + DbKeys.NetworkElementAdjacencyColsV2.MANUALPROVISION + " = ?;";
    }

    DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
      @Override
      public PreparedStatement buildPreparedStatement(PreparedStatement stmt) throws Exception {
        if (port != null) {
          stmt.setString(1, encodePK(neid, port, type));
        }
        else {
          stmt.setString(1, encodePK(neid, "%", "%"));
          stmt.setBoolean(2, false);
        }

        return stmt;
      }
    };
    DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
  }

  // Used only by integration tests
  public void deleteAll() throws Exception {
    DbOperationsManager.INSTANCE.executeDbOpWithResults("delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
  }

  public void deleteAllNonManual() throws Exception {
    // Remove all non-manual entries
    String sql = "delete from " + TABLENAME + " where " + DbKeys.NetworkElementAdjacencyColsV2.MANUALPROVISION
        + " = false;";
    DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, new DbOpWithResultsAdapter());
  }

  public List<NetworkElementAdjacency> retrieve() throws Exception {
    String sql = "select * from " + TABLENAME;

    DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
      List<NetworkElementAdjacency> result;

      @Override
      public Object getResult() {
        return result;
      }

      @Override
      public void processResults(ResultSet rs) throws Exception {
        result = new ArrayList<NetworkElementAdjacency>();

        while (rs.next()) {
          NetworkElementAdjacency adj = new NetworkElementAdjacency(
              rs.getString(DbKeys.NetworkElementAdjacencyColsV2.NEID),
              rs.getString(DbKeys.NetworkElementAdjacencyColsV2.PORT),
              rs.getString(DbKeys.NetworkElementAdjacencyColsV2.TXTAG),
              rs.getString(DbKeys.NetworkElementAdjacencyColsV2.RXTAG),
              rs.getString(DbKeys.NetworkElementAdjacencyColsV2.TYPE), Boolean.valueOf(
                  rs.getBoolean(DbKeys.NetworkElementAdjacencyColsV2.MANUALPROVISION)).booleanValue());
          result.add(adj);
        }
      }
    };

    DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
    return (List<NetworkElementAdjacency>) dbOp.getResult();
  }

  public void update(final NetworkElementAdjacency adj) throws Exception {
    String sql = "update " + TABLENAME + " SET " + DbKeys.NetworkElementAdjacencyColsV2.TXTAG + " = ? " + " , "
        + DbKeys.NetworkElementAdjacencyColsV2.RXTAG + " = ? " + " where pk LIKE ? ;";

    DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
      @Override
      public PreparedStatement buildPreparedStatement(PreparedStatement stmt) throws Exception {
        stmt.setString(1, adj.getTxTag());
        stmt.setString(2, adj.getRxTag());
        stmt.setString(3, encodePK(adj.getNeid(), adj.getPort(), adj.getType()));
        return stmt;
      }
    };
    DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
  }

  // ne and port is not always unique ... some ports will report adj on mult
  // layers
  private String encodePK(String neid, String port, String type) {
    if (neid != null && port != null && type != null) {
      return neid + "_" + port + "_" + type;
    }

    return null;
  }

  public void deleteAllByNeId(final String neId) throws Exception {
    final DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
      @Override
      public PreparedStatement buildPreparedStatement(PreparedStatement stmt) throws Exception {
          stmt.setString(1, neId);
        return stmt;
      }
    };
    DbOperationsManager.INSTANCE.executeDbOpWithResults("delete from " + TABLENAME + " where neid = ?", dbOp);
  }
}
