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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateTimeCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;

/**
 * Display system users. Mostly for debug but displays a list of users in the
 * system, currently its read only and offers no add/edit/delete capabilities.
 * 
 * @author pitman
 */
public final class UserPanel {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final List<String> columns = Arrays.asList(new String[] { "UserName",
	    "TimeZone", "Mail", "Phone", "PostalAddress", "Personal Description",
	    "CommonName", "GivenName", "SurName", "Title", "Category",
	    "OrgDescription", "OrgName", "OrgUnitName", "Owner", "SeeAlso",
	    "CreatedByGroupName", "MemberResourceGroup", "MemberUserGroup",
	    "MemberUserIds", "LastModified", "Creation", "InvalidAttempts",
	    "LastLogin", "InvalidAttempts", "AuthenticationState",
	    "AuthenticationType", "ExpirationDate", "LastPasswordChanged",
	    "Password", "WSDLCredential", "LastAuthenticationStateChange",
	    "DormantPeriod", "InactivityPeriod", "LocalPasswordPolicy",
	    "LockedClientIPs", "LockoutPeriod", "MaxInvalidLoginAttempts" });

	// These column numbers (from columns) are date fields and should be displayed
	// as such.
	private final int[] DATE_COLS = new int[] { 20, 21, 27, 28, 31 };
	private final TablePanel tablePanel = new TablePanel(
	    new DracTableModel<Object>(null, columns), null, null);
	private final OpenDracDesktop desktop;

	public UserPanel(OpenDracDesktop d) {
		desktop = d;
	}

	public JPanel buildUserPanel() {
		JButton retrieveButton = new JButton("Retrieve");
		retrieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				desktop.showProgressDialog("Retrieving users...");
				getAndDisplayUsers();
			}
		});

		JPanel controlsPanel = new JPanel(new BorderLayout(1, 1));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(tablePanel.getExportButton(desktop, null));
		buttonPanel.add(retrieveButton);
		controlsPanel.add(buttonPanel, BorderLayout.EAST);
		tablePanel.addButton(controlsPanel);
		return tablePanel.getWrappedPanel("Users");
	}

	private Long calToLong(Calendar c) {
		if (c == null) {
			return null;
		}
		return Long.valueOf(c.getTimeInMillis());
	}

	private void displayUsers(List<UserProfile> users) throws Exception {
		((DracTableModel<Object>) tablePanel.getTable().getModel()).clearTable();
		if (users == null) {
			return;
		}

		List<List<Object>> rows = new ArrayList<List<Object>>();
		List<Object> row = null;

		try {
			desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);

			for (UserProfile u : users) {
				row = new ArrayList<Object>();
				try {
					row.add(u.getUserID());

					TimeZone tz = TimeZone
					    .getTimeZone(u.getPreferences().getTimeZoneId());
					row.add(DateFormatter.getTimeZoneDisplayName(tz));

					row.add(u.getPersonalData().getAddress().getMail());
					row.add(u.getPersonalData().getAddress().getPhone());
					row.add(u.getPersonalData().getAddress().getPostalAddress());
					row.add(u.getPersonalData().getDescription());
					row.add(u.getPersonalData().getName().getCommonName());
					row.add(u.getPersonalData().getName().getGivenName());
					row.add(u.getPersonalData().getName().getSurName());
					row.add(u.getPersonalData().getTitle());
					row.add(u.getOrganizationData().getCategory());
					row.add(u.getOrganizationData().getDescription());
					row.add(u.getOrganizationData().getOrgName());
					row.add(u.getOrganizationData().getOrgUnitName());
					row.add(u.getOrganizationData().getOwner());
					row.add(u.getOrganizationData().getSeeAlso());
					row.add(u.getMembershipData().getCreatedByGroupName());
					row.add(u.getMembershipData().getMemberResourceGroupName());
					row.add(u.getMembershipData().getMemberUserGroupName());
					row.add(u.getMembershipData().getMemberUserID());

					row.add(calToLong(u.getLastModifiedDate()));

					row.add(calToLong(u.getCreationDate()));

					row.add(Integer.valueOf(u.getAuthenticationData().getAuditData()
					    .getNumberOfInvalidAttempts()));
					row.add(u.getAuthenticationData().getAuditData()
					    .getLastLoginAddressList());
					row.add(u.getAuthenticationData().getAuditData()
					    .getLocationOfInvalidAttempts());
					row.add(u.getAuthenticationData().getAuthenticationState());
					row.add(u.getAuthenticationData().getAuthenticationType());

					row.add(calToLong(u.getAuthenticationData().getInternalAccountData()
					    .getExpirationDate()));

					row.add(calToLong(u.getAuthenticationData().getInternalAccountData()
					    .getLastPasswordChanged()));

					{
						String pw = u.getAuthenticationData().getInternalAccountData()
						    .getUserPassword();
						row.add(CryptoWrapper.INSTANCE.encrypt(pw));
					}
					{
						String pw = u.getAuthenticationData().getWSDLCredential();
						row.add(pw == null || "".equals(pw) ? "" : CryptoWrapper
						    .INSTANCE.encrypt(pw));
					}

					row.add(calToLong(u.getAuthenticationData()
					    .getLastAuthenticationStateChange()));

					row.add(u.getAuthenticationData().getUserAccountPolicy()
					    .getDormantPeriod());
					row.add(u.getAuthenticationData().getUserAccountPolicy()
					    .getInactivityPeriod());
					row.add(u.getAuthenticationData().getUserAccountPolicy()
					    .getLocalPasswordPolicy());
					row.add(u.getAuthenticationData().getUserAccountPolicy()
					    .getLockedClientIPs());
					row.add(u.getAuthenticationData().getUserAccountPolicy()
					    .getLockoutPeriod());
					row.add(u.getAuthenticationData().getUserAccountPolicy()
					    .getMaxInvalidLoginAttempts());

					rows.add(row);

				}
				catch (Exception e) {
					log.error("error adding user to table " + u, e);
				}
			}

			((DracTableModel<Object>) tablePanel.getTable().getModel()).setData(rows);

			for (int col : DATE_COLS) {
				TableColumn column = tablePanel.getTable().getTableHeader()
				    .getColumnModel().getColumn(col);
				if (column != null) {					
					column.setCellRenderer(new DateTimeCellRenderer(desktop.locale,
					    desktop.getTimeZonePreference()));
				}
			}

			tablePanel.updateTable();

		}
		finally {
			desktop.hideProgressDialog();
			desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
		}
	}

	private void getAndDisplayUsers() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					List<UserProfile> users = desktop.getNRBHandle().getUserProfileList(
					    desktop.getLoginToken());
					displayUsers(users);
				}
				catch (Exception e) {
					log.error("OpenDracDesktop::getUsers failed.", e);
				}
			}
		}, "OpenDracDesktop getUsers()");
		t.setDaemon(true);
		t.start();
	}

}
