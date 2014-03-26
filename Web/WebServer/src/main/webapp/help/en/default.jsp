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

<%@ page errorPage="/common/dracError.jsp"%>

<style>
<!--
 /* Font Definitions */
 @font-face
	{font-family:Wingdings;
	panose-1:5 0 0 0 0 0 0 0 0 0;}
@font-face
	{font-family:Tahoma;
	panose-1:2 11 6 4 3 5 4 4 2 4;}
 /* Style Definitions */
 p.MsoNormal, li.MsoNormal, div.MsoNormal
	{margin:0in;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Arial";}
h1
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:.3in;
	text-indent:-.3in;
	page-break-after:avoid;
	font-size:15.0pt;
	font-family:Arial;
	font-weight:bold;}
h2
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:.4in;
	text-indent:-.4in;
	page-break-after:avoid;
	font-size:14.0pt;
	font-family:"Arial";
	font-weight:bold;}
h3
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:.5in;
	text-indent:-.5in;
	page-break-after:avoid;
	font-size:12.0pt;
	font-family:Arial;
	font-weight:bold;}
h4
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:.6in;
	text-indent:-.6in;
	page-break-after:avoid;
	font-size:14.0pt;
	font-family:"Arial";
	font-weight:bold;}
h5
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:.7in;
	text-indent:-.7in;
	font-size:13.0pt;
	font-family:"Arial";
	font-weight:bold;
	font-style:italic;}
h6
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:.8in;
	text-indent:-.8in;
	font-size:11.0pt;
	font-family:"Arial";
	font-weight:bold;}
p.MsoHeading7, li.MsoHeading7, div.MsoHeading7
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:.9in;
	text-indent:-.9in;
	font-size:12.0pt;
	font-family:"Arial";}
p.MsoHeading8, li.MsoHeading8, div.MsoHeading8
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:1.0in;
	text-indent:-1.0in;
	font-size:12.0pt;
	font-family:"Arial";
	font-style:italic;}
p.MsoHeading9, li.MsoHeading9, div.MsoHeading9
	{margin-top:12.0pt;
	margin-right:0in;
	margin-bottom:3.0pt;
	margin-left:1.1in;
	text-indent:-1.1in;
	font-size:11.0pt;
	font-family:Arial;}
p.MsoToc1, li.MsoToc1, div.MsoToc1
	{margin:0in;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Arial";}
p.MsoToc2, li.MsoToc2, div.MsoToc2
	{margin-top:0in;
	margin-right:0in;
	margin-bottom:0in;
	margin-left:12.0pt;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Arial";}
p.MsoToc3, li.MsoToc3, div.MsoToc3
	{margin-top:0in;
	margin-right:0in;
	margin-bottom:0in;
	margin-left:24.0pt;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Arial";}
p.MsoHeader, li.MsoHeader, div.MsoHeader
	{margin:0in;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Arial";}
p.MsoFooter, li.MsoFooter, div.MsoFooter
	{margin:0in;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Arial";}
p.MsoCaption, li.MsoCaption, div.MsoCaption
	{margin:0in;
	margin-bottom:.0001pt;
	font-size:10.0pt;
	font-family:"Arial";
	font-weight:bold;}
a:link, span.MsoHyperlink
	{color:blue;
	text-decoration:underline;}
a:visited, span.MsoHyperlinkFollowed
	{color:purple;
	text-decoration:underline;}
p.MsoDocumentMap, li.MsoDocumentMap, div.MsoDocumentMap
	{margin:0in;
	margin-bottom:.0001pt;
	background:navy;
	font-size:10.0pt;
	font-family:Tahoma;}
p
	{margin-right:0in;
	margin-left:0in;
	font-size:12.0pt;
	font-family:"Arial";}
 /* Page Definitions */
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

<h1>
	OpenDRAC Web GUI Guide
</h1>
<div class=Section1>

	<br />
	<h2>
		Contents
	</h2>

	<p class=MsoNormal style='text-autospace:none'>
		<span style='font-size:8.0pt;
color:black'>&nbsp;</span>
	</p>

	<p class=MsoToc1>
		<span class=MsoHyperlink><a href="#_Toc129405016">1<span style='color:windowtext;text-decoration:none'>      </span>Synopsis<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>2</span></a></span>
	</p>

	<p class=MsoToc1>
		<span class=MsoHyperlink><a href="#_Toc129405017">2<span style='color:windowtext;text-decoration:none'>      </span>Audience<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>2</span></a></span>
	</p>

	<p class=MsoToc1>
		<span class=MsoHyperlink><a href="#_Toc129405018">3<span style='color:windowtext;text-decoration:none'>      </span>Features Supported<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>2</span></a></span>
	</p>

	<p class=MsoToc1>
		<span class=MsoHyperlink><a href="#_Toc129405019">4<span style='color:windowtext;text-decoration:none'>      </span>User Interface<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>3</span></a></span>
	</p>

	<p class=MsoToc2>
		<span class=MsoHyperlink><a href="#_Toc129405020">4.1<span style='color:windowtext;text-decoration:none'>       </span>User Login<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>3</span></a></span>
	</p>

	<p class=MsoToc2>
		<span class=MsoHyperlink><a href="#_Toc129405021">4.2<span style='color:windowtext;text-decoration:none'>       </span>Server Status<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>5</span></a></span>
	</p>

	<p class=MsoToc2>
		<span class=MsoHyperlink><a href="#_Toc129405022">4.3<span style='color:windowtext;text-decoration:none'>       </span>Schedules<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>6</span></a></span>
	</p>

	<p class=MsoToc3>
		<span class=MsoHyperlink><a href="#_Toc129405023">4.3.1<span style='color:windowtext;text-decoration:none'>        </span>List Schedules<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>6</span></a></span>
	</p>

	<p class=MsoToc3>
		<span class=MsoHyperlink><a href="#_Toc129405024">4.3.2<span style='color:windowtext;text-decoration:none'>        </span>Create Schedule<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>10</span></a></span>
	</p>

	<p class=MsoToc2>
		<span class=MsoHyperlink><a href="#_Toc129405025">4.4<span style='color:windowtext;text-decoration:none'>       </span>End Point<span style='color:windowtext;display:none;text-decoration:none'> </span><span
				style='color:windowtext;display:none;text-decoration:none'>13</span></a></span>
	</p>

	<p class=MsoToc3>
		<span class=MsoHyperlink><a href="#_Toc129405026">4.4.1<span style='color:windowtext;text-decoration:none'>        </span>List EndPoints<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>13</span></a></span>
	</p>

	<p class=MsoToc3>
		<span class=MsoHyperlink><a href="#_Toc129405027">4.4.2<span style='color:windowtext;text-decoration:none'>        </span>Utilization<span style='color:windowtext;display:none;text-decoration:none'>. </span><span
				style='color:windowtext;display:none;text-decoration:none'>16</span></a></span>
	</p>

	<p class=MsoToc2>
		<span class=MsoHyperlink><a href="#_Toc129405028">4.5<span style='color:windowtext;text-decoration:none'>       </span>Logout<span style='color:windowtext;display:none;text-decoration:none'> </span><span
				style='color:windowtext;display:none;text-decoration:none'>18</span></a></span>
	</p>

	<p class=MsoNormal>
		<a name="_Toc129405016">&nbsp;</a>
	</p>

	<h1>
		<span style='font-family:"Arial"'>1<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>Synopsis</span></span>
	</h1>

	<p class=MsoNormal style='margin-left:.5in;text-autospace:none'>
		OpenDRAC (open dynamic resource allocation controller) is a bandwidth controller and scheduling resource manager for grid computing applications. OpenDRAC is implemented as middleware layered between the application and the network elements.
	</p>

	<p class=MsoNormal style='margin-left:.5in;text-autospace:none'>
		&nbsp;
	</p>

	<h1>
		<a name="_Toc129405017"><span style='font-family:"Arial"'>2<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>Audience</span></span></a>
	</h1>

	<p class=MsoNormal>
		            This document is intended for all users of OpenDRAC web graphical user interface.
	</p>

	<p class=MsoNormal style='margin-left:.3in'>
		&nbsp;
	</p>

	<h1>
		<a name="_Toc129405018"><span style='font-family:"Arial"'>3<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>Features Supported</span></span></a>
	</h1>

	<p class=MsoNormal>
		           The following are the features supported by OpenDRAC in the Alpha release.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<ul style='margin-top:0in' type=disc>
		<li class=MsoNormal>
			The OpenDRAC server status displays the version, status, OpenDRAC build date and the web server details
		</li>
		<li class=MsoNormal>
			Create single or recurring schedules using point-to-point GE or SDH services (C-CAT WAN types only).
		</li>
		<li class=MsoNormal>
			View the schedules created in the network.
		</li>
		<li class=MsoNormal>
			Delete the schedule.
		</li>
		<li class=MsoNormal>
			View the layer 1 and layer 2 endpoints.
		</li>
		<li class=MsoNormal>
			Edit layer 2 endpoint property. Maximum transmission unit is the editable parameter for the layer2 end point.
		</li>
		<li class=MsoNormal>
			View utilization for endpoints at a particular time.
		</li>
		<li class=MsoNormal>
			Web GUI is supported in Internet Explorer, Firefox and Safari.
		</li>
	</ul>

	<p class=MsoNormal style='margin-left:.25in'>
		&nbsp;
	</p>

	<h1>
		<a name="_Toc129405019"><span style='font-family:"Arial"'>4<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>User Interface</span></span></a>
	</h1>

	<p class=MsoNormal style='text-indent:.3in'>
		This section guides through the user interface screens.
	</p>

	<h2>
		<a name="_Toc129405020">4.1<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span><span dir=LTR>User Login</span></a>
	</h2>

	<p class=MsoNormal style='margin-left:.4in'>
		OpenDRAC is secured by the authentication system .User will be asked to enter the userID and password. Once the user provides the correct credentials, access is provided to the application. User may be asked to re-authenticate if the user session expires. A
		welcome page is displayed on successful authentication (Figure 2).
	</p>

	<p class=MsoNormal style='margin-left:.4in'>
		Server time is displayed as part of the header on every screen. The status of the user login is displayed at the right side corner.
	</p>

	<p class=MsoNormal style='margin-left:.4in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image001.jpg">
	</p>

	<p class=MsoCaption>
		Figure 1: Login screen
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<b><span style='font-size:
18.0pt'><img align="center" src="/help/en/images/image002.jpg"></span></b>
	</p>

	<p class=MsoCaption>
		Figure 2 : Welcome screen
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<h2>
		<a name="_Toc129405021">4.2<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span><span dir=LTR>Server Status</span></a>
	</h2>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.4in'>
		The server status displays the following details:
	</p>

	<p class=MsoNormal style='margin-left:.4in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.9in;text-indent:-.25in'>
		<a name="_Toc128295637"></a><a name="_Toc128292244"></a><a name="_Toc128295638"></a><a name="_Toc128292245"><span style='font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>OpenDRAC
				Status. This is an indication of the OpenDRAC network resource broker. The status of the server which can be ‘Running’ or ‘down’.</span></a> The normal status of this attribute is “Running”
	</p>

	<p class=MsoNormal style='margin-left:.9in;text-indent:-.25in'>
		<span style='font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>OpenDRAC version-The current OpenDRAC software version running on server version</span>.
	</p>

	<p class=MsoNormal style='margin-left:.9in;text-indent:-.25in'>
		<span style='font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>OpenDRAC build date – Build date of the OpenDRAC server software.</span>
	</p>

	<p class=MsoNormal style='margin-left:.9in;text-indent:-.25in'>
		<span style='font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>Web server: The version of the web server being used to provide client access to OpenDRAC.</span>
	</p>

	<p class=MsoNormal style='margin-left:.9in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image003.jpg">
	</p>

	<p class=MsoCaption>
		Figure 3 : Server status
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<h2>
		<a name="_Toc129405022">4.3<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span><span dir=LTR>Schedules</span></a>
	</h2>

	<h3>
		<a name="_Toc129405023"><span style='font-family:"Arial"'>4.3.1<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>List Schedules</span></span></a>
	</h3>

	<p class=MsoNormal style='margin-left:.5in'>
		<span class=gen>&nbsp;</span>
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		Path: Schedule -&gt; List schedules.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		The first screen allows the user to query for the schedules.<i> <span class=gen>&quot;Query Schedule&quot;</span></i><span class=gen> provides information on the schedules created in the network. <i>&quot;Schedule Filter</i>&quot; functionality provides
			a mechanism to view all schedules created/accessible within your user profile and within the specified period.</span>
	</p>

	<p class=MsoNormal style='margin-left:1.0in;text-indent:-.25in'>
		<span class=gen><span style='font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span></span><span dir=LTR><span class=gen>&quot;<i>Member Group</i>&quot; provides the ability to filter based on a
				specific user group in your user profile. Specifying the &quot;All Member Groups&quot; option will return a list of all endpoints accessible from your profile.</span></span>
	</p>

	<p class=MsoNormal style='margin-left:1.0in;text-indent:-.25in'>
		<span class=gen><span style='font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span></span><span dir=LTR><span class=gen>&quot;<i>Start Time</i>&quot; specifies the start time for the query.</span></span>
	</p>

	<p class=MsoNormal style='margin-left:1.0in;text-indent:-.25in'>
		<span class=gen><span style='font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span></span><span dir=LTR><span class=gen>&quot;<i>End Time</i>&quot; specifies the end time for the query.</span></span>
	</p>

	<span style='font-size:12.0pt;font-family:"Arial"'><br clear=all style='page-break-before:always'> </span>

	<p class=MsoNormal style='margin-left:1.0in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:27.0pt;text-indent:-27.0pt;page-break-after:
avoid'>
		<span class=gen><img align="center" src="/help/en/images/image004.jpg"></span>
	</p>

	<p class=MsoCaption>
		Figure 4 : Query schedules
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<span class=gen>Result will be the list of schedules created within the above specified duration. </span>
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<span class=gen>&nbsp;</span>
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<span class=gen><img align="center" src="/help/en/images/image005.jpg"></span>
	</p>

	<p class=MsoCaption>
		Figure 5 : List Schedules
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p style='margin-left:.25in'>
		<i>&quot;List Schedule&quot;</i> lists all the schedules created within the duration specified in the query Schedule filter. This also allows the deletion of the schedule.
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Status</i>&quot; an indication of the status of the schedule. The newly created
			schedule is &quot;Active&quot;. When all the services within a schedule expires, the status of the schedule turns &quot;Inactive&quot;. </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Schedule ID</i>&quot; specifies the unique ID generated for the schedule. </span>
	</p>

	<p class=MsoNormal style='text-indent:.5in'>
		On clicking the schedule ID, the details of the schedule can be obtained.
	</p>

	<p class=MsoNormal style='text-indent:.5in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image006.jpg">
	</p>

	<p class=MsoCaption>
		Figure 6 : Schedule Details
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p style='margin-left:.25in'>
		<i>&quot;Schedule Details&quot;</i> provides the detailed description of the schedule specified.
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Schedule Name</i>&quot; refers to the name of the schedule provided by the user. </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Source TNA</i>&quot; refers to the source end point of the schedule. </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Destination TNA</i>&quot; refers to the destination end point of the schedule. </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Start Date</i>&quot; indicates the start date of the schedule </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>End Date</i>&quot; indicates the end date of the schedule </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Rate</i>&quot; identifies the bandwidth rate of the schedule </span>
	</p>

	<p style='margin-left:.25in'>
		<i>&quot;Recurrence Details&quot;</i> provides the information of the recurring schedules.
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Type</i>&quot; could be daily, weekly, monthly and yearly </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Day</i>&quot; could be any day between 1 and 31 and is displayed only for “Monthly”
			or “Yearly” recurring schedules.</span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Month</i>&quot; could be any month in the year .It is displayed only for yearly
			recurring schedules.</span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>WeekDays</i>&quot; refers to the day in the week that services are provisioned </span>
	</p>

	<p style='margin-left:.25in'>
		<i>&quot;Service Details&quot;</i> provides the information of the services provisioned.
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Service ID</i>&quot; refers to the unique service ID generated within the schedule </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Status</i>&quot; :</span>
	</p>

	<p class=MsoNormal style='margin-left:.75in'>
		“Active”  - for a newly created service.
	</p>

	<p class=MsoNormal style='margin-left:.75in'>
		“Expired” - once the service is provisioned.
	</p>

	<p class=MsoNormal style='margin-left:.75in'>
		“Inactive”   - If the schedule is deleted, the status of the service turns      Inactive.
	</p>

	<p class=MsoNormal>
		<i>      &quot;Call Details&quot;</i> provides the information of the calls within the service.
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Call ID</i>&quot; refers to the unique call ID generated within the service. </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Status</i>&quot; : </span>
	</p>

	<p class=MsoNormal style='margin-left:.75in'>
		“Active&quot; for a newly created call.
	</p>

	<p class=MsoNormal style='margin-left:.75in'>
		“Expired” –once the call is completed
	</p>

	<p class=MsoNormal style='margin-left:.75in'>
		“Cancelled” when the schedule is deleted.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<h3>
		<a name="_Toc129405024"><span style='font-family:"Arial"'>4.3.2<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>Create Schedule</span></span></a><span
			style='font-family:"Arial"'> </span>
	</h3>

	<p class=MsoNormal style='margin-left:.5in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		Path: Schedule -&gt; Create schedule.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“Create Schedule” allows the user to create schedules. The schedule could be a single or a recurring schedule.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image007.jpg">
	</p>

	<p class=MsoCaption>
		Figure 7 : Create Schedule
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“<i>Schedule Name</i>” is the optional name with which the user would like to refer to the schedule.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“<i>Rate</i>” is the rate at which the schedule is created.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		‘<i>Source Layer</i>” refers to the layer of the source end point. “<span class=gen>Endpoints&quot; identify network access points that can be dynamically interconnected to provide bandwidth between two points within the network. The &quot;<i>List
				Endpoints</i>&quot; functionality provides a mechanism to view all endpoints accessible within your user profile.</span>
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“<i>Destination Layer</i>” refers to the layer of the destination endpoint.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“<i>Source TNA</i>” refers to the source end point for the call creation
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“<i>Destination TNA</i>” refers to the destination end point for the call creation.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“<i>Utilization</i>” refers to the utilization factor of the endpoint at the current time.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<i>“Start Time”</i> is the starting time for the schedule
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<i>“End Time“ </i>is the end time for the schedule
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<i>“Start Date” </i>is the starting time for the schedule.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<i>“End Date” </i> is the end date for the schedule
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<b>“Advanced Options”</b> are the optional parameters that the user can provide for creation of the schedule.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“E-mail ID” is the Email ID for the notification about the schedules created
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“SRLG”, “Cost”, “Metric” and “Hop” are the routing parameters.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<b>“Recurrence “ </b>
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		The recurrence allows the user to create the recurring schedules. Below are the following options:
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<i>“Daily” </i>refers to the daily schedules
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		“Weekly” refers to the recurring schedules created for different days in a week.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<i>“Monthly” </i>refers to the monthly schedule created. Any number between 1-31 could be specified for a monthly schedule.
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		<i>“Yearly” </i>refers to the schedule that recurs on a particular day of the month, yearly.
	</p>

	<p class=MsoCaption style='margin-left:.5in;text-indent:.5in;page-break-after:
avoid'>
		&nbsp;
	</p>

	<p class=MsoCaption style='margin-left:.5in;page-break-after:avoid'>
		<span style='font-size:12.0pt;font-weight:normal'>The recurrence screen is shown below:</span>
	</p>

	<p class=MsoCaption style='margin-left:.5in;page-break-after:avoid'>
		<span style='font-size:12.0pt;font-weight:normal'>&nbsp;</span>
	</p>

	<p class=MsoCaption style='margin-left:.5in;page-break-after:avoid'>
		<img align="center" src="/help/en/images/image008.jpg">
	</p>

	<p class=MsoCaption style='text-indent:.5in'>
		Figure 8 : Recurrence screen
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in;page-break-after:avoid'>
		On successful creation of the schedule, the following success message and schedule details are displayed.
	</p>

	<p class=MsoNormal style='margin-left:.5in;page-break-after:avoid'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in;page-break-after:avoid'>
		<img align="center" src="/help/en/images/image009.jpg">
	</p>

	<p class=MsoCaption>
		Figure 9 : Schedule success screen
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<h2>
		<a name="_Toc129405025">4.4<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span><span dir=LTR>End Point</span></a>
	</h2>

	<h3>
		<a name="_Toc129405026"><span style='font-family:"Arial"'>4.4.1<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>List EndPoints</span></span></a><span
			style='font-family:"Arial"'> </span>
	</h3>

	<p class=MsoNormal style='margin-left:.5in'>
		&nbsp;
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		Path: Schedule -&gt; List EndPoints
	</p>

	<p style='margin-left:.5in'>
		&quot;Endpoints&quot; identify network access points that can be dynamically interconnected to provide bandwidth between two points within the network. The &quot;<i>List Endpoints</i>&quot; functionality provides a mechanism to view all endpoints
		accessible within your user profile.
	</p>

	<p style='margin-left:.5in'>
		The &quot;<i>Endpoint Filter</i>&quot; provides a mechanism to restrict the set of endpoints returned during this query.
	</p>

	<p class=MsoNormal style='margin-left:1.0in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Member Group</i>&quot; provides the ability to filter based on a specific user group
			in your user profile. Specifying the &quot;All Member Groups&quot; option will return a list of all endpoints accessible from your profile. </span>
	</p>

	<p class=MsoNormal style='margin-left:1.0in;text-indent:-.25in'>
		<span style='font-size:10.0pt;font-family:Symbol'>·<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR>&quot;<i>Endpoint Layer</i>&quot; provides the ability to filter based on the layer of
			service provided by the endpoint. &quot;<i>Layer 2</i>&quot; can be specified to view endpoints providing Ethernet services. &quot;<i>Layer 1</i>&quot; can be specified to view endpoints providing SONET/SDH services. </span>
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image010.jpg">
	</p>

	<p class=MsoCaption>
		Figure 10 : EndPoint filter
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		The end point details can be seen by clicking on the details link.
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image011.jpg">
	</p>

	<p class=MsoCaption>
		Figure 11 : List Endpoints screen
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='text-indent:.5in'>
		The end point attribute can be edited by clicking on the edit button.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image012.jpg">
	</p>

	<p class=MsoCaption>
		Figure 12 : Edit EndPoint screen
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal>
		Once edited, click on the edit button to save the changes.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<h3>
		<a name="_Toc129405027"><span style='font-family:"Arial"'>4.4.2<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span dir=LTR><span style='font-family:"Arial"'>Utilization</span></span></a><span
			style='font-family:"Arial"'> </span>
	</h3>

	<p class=MsoNormal style='margin-left:.5in'>
		Path: network-&gt;Utilization
		<br>
		<br>
	</p>

	<p class=MsoNormal style='margin-left:.5in'>
		This screen displays the % of utilization for the selected endpoint. This is based on the number of packets transmitted across the endpoint at a particular point.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image013.jpg">
	</p>

	<p class=MsoCaption>
		Figure 13 : View Utilization screen
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<h2>
		<a name="_Toc129405028">4.5<span style='font:7.0pt "Arial"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span><span dir=LTR>Logout</span></a>
	</h2>

	<p class=MsoNormal style='margin-left:.4in'>
		User can logout by clicking on the “logout” button on the right side corner. User will be asked if he wants to logout. On clicking “Yes”, user will be successfully logged out of the system.
	</p>

	<p class=MsoNormal>
		&nbsp;
	</p>

	<p class=MsoNormal style='page-break-after:avoid'>
		<img align="center" src="/help/en/images/image014.jpg">
	</p>

	<p class=MsoCaption>
		Figure 14 : Logout screen
	</p>

</div>
