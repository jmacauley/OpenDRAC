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

<%
    boolean csrfTokenMatch = false;
    String csrfTokenFromSession = (String) session.getAttribute("CSRFToken");
    String csrfTokenFromFromData = (String) request.getParameter("CSRFToken");

    //Prior to login, the session has not been given a csrf token. 
    //Ensure that a session token if present before attempting to compare it 
    //to request data
    if (csrfTokenFromSession != null)
    {
        // Set a variable in case the page wants to do additional processing on the condition:
        if (csrfTokenFromFromData != null && csrfTokenFromSession.equals(csrfTokenFromFromData))
            csrfTokenMatch = true;
        
        if (!csrfTokenMatch)
        {
            throw new Exception("Cross-site request forgery detected.");
        }
    }
%>
