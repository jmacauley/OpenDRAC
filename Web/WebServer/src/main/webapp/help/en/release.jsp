<%--

    <pre>
    The owner of the original code is Ciena Corporation.

    Portions created by the original owner are Copyright (C) 2004-2010
    the original owner. All Rights Reserved.

    Portions created by other contributors are Copyright (C) the contributor.
    All Rights Reserved.

    Contributor(s):
      (Contributors insert name & email here)

    This file is part of DRAC (Dynamic Resource Allocation Controller).

    DRAC is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    DRAC is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
    Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program.  If not, see <http://www.gnu.org/licenses/>.
    </pre>

--%>

<style>
<!--
 /* Font Definitions */
 @font-face
	{font-family:Wingdings;
	panose-1:5 0 0 0 0 0 0 0 0 0;}
@font-face
	{font-family:SimSun;
	panose-1:2 1 6 0 3 1 1 1 1 1;}
@font-face
	{font-family:"\@SimSun";
	panose-1:2 1 6 0 3 1 1 1 1 1;}
 /* Style Definitions */
 p.MsoNormal, li.MsoNormal, div.MsoNormal
	{margin:0in;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Arial";}
@page Section1
	{size:8.5in 11.0in;
	margin:1.0in 1.25in 1.0in 1.25in;}
div.Section1
	{page:Section1;}
 /* List Definitions */
 ol
	{margin-bottom:0in;}
ul
	{margin-bottom:0in;}
-->
</style>

<div class=Section1>

	<p class=MsoNormal align=center style='text-align:center'>
		<u>Open Dynamic Resource Allocation Controller</u>
	</p>

	<p class=MsoNormal align=center style='text-align:center'>
		<u>Version 4.1.0</u>
	</p>

	<p class=MsoNormal align=center style='text-align:center'>
		<u>Release Notes</u>
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		This release of OpenDRAC is alpha software.  As of the writing of this document, the following are a list of known limitations with this release of the software.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		Server
	</p>

	<ul style='margin-top:0in' type=disc>
		<li class=MsoNormal>
			VCAT connections are not yet supported in this alpha release.
		</li>
		<li class=MsoNormal>
			Data changed directly on Network Elements (outside of OpenDRAC) are not always picked up by the OpenDRAC server.
		</li>
		<ul style='margin-top:0in' type=circle>
			<li class=MsoNormal>
				To work around this issue, it is currently necessary to restart the OpenDRAC server.
			</li>
		</ul>
		<li class=MsoNormal>
			If the server-based control plane (lpcp/controller) dies, the OpenDRAC server does not automatically restart it.  It will be necessary to restart the OpenDRAC server.
		</li>
		<li class=MsoNormal>
			If VT connections are provisioned on network elements managed by OpenDRAC, the VT connections will be detected.
		</li>
		<li class=MsoNormal>
			The L2SS card is not supported.
		</li>
	</ul>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		Web Client
	</p>

	<ul style='margin-top:0in' type=disc>
		<li class=MsoNormal>
			Editing the MTU value for an Ethernet endpoint does not change the value on the Network Element.
		</li>
		<li class=MsoNormal>
			Endpoint utilization always shows the current utilization and not for the specified time.
		</li>
		<li class=MsoNormal>
			Not all pages in the web client have been implemented in this release.  No functions from the “Service” or “Security” menu have been implemented except for the “Secure Session” function.
		</li>
		<li class=MsoNormal>
			Internationalization support is not complete.  The default language is US English.  All languages settings except for French default to US English.  The French language setting is not complete, and has been generated using Babelfish.  Times and calendar
			functions are not internationalized.
		</li>
	</ul>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		Administration Client
	</p>

	<ul style='margin-top:0in' type=disc>
		<li class=MsoNormal>
			It is possible that after a OpenDRAC server restarts, that any previously connected administration client will continually display the message “Waiting for server to restart”.  If this situation occurs, a restart of the administration client will clear up
			the problem.
		</li>
		<li class=MsoNormal>
			User authentication in the administration client currently uses A-Select to verify user credentials. 
		</li>
		<li class=MsoNormal>
			User authentication only verifies that a userid and password combination is valid for the selected A-Select server.  It does not verify that the user authenticated possesses sufficient admin privileges to start the administration client.  As a result,
			any user known to A-Select can log into the administration client and have the same privileges as an admin user.
		</li>
		<li class=MsoNormal>
			The create schedule dialog does not currently permit creation of recurring schedules.
		</li>
		<li class=MsoNormal>
			The Event Browser tab is not implemented in this release.
		</li>
		<li class=MsoNormal>
			The Performance Monitoring tab is not implemented in this release.
		</li>
		<li class=MsoNormal>
			The Service Alarms tab is not implemented in this release.
		</li>
	</ul>

	<p class=MsoNormal style='margin-left:.25in'>
		&nbsp;
	</p>

</div>
