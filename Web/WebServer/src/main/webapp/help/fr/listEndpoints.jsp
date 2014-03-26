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

<%@ page errorPage="/common/dracError.jsp" %>

<%
/* OpenDRAC Web GUI */
%>

<span class="gen">
	<p>Les "points finaux" identifient les points d'acc�s de r�seau qui peuvent �tre dynamiquement reli�s ensemble pour fournir la largeur de bande entre deux points dans le r�seau. La fonctionnalit� "<i>de points finaux de liste</i>" fournit un m�canisme pour regarder tous les points finaux accessibles dans votre profil d'utilisateur.</p>
  <p>Le "<i>filtre de point final</i>" fournit un m�canisme pour limiter l'ensemble de points finaux retourn�s pendant cette question.
  <ul>
    <li>
    Le "<i>groupe de membre"</i> fournit les capacit�s au filtre bas� sur un groupe d'utilisateur sp�cifique dans votre profil d'utilisateur. L'indication du l'"tout le membre groupe" l'option renverra une liste de tous les points finaux accessibles de votre profil. 
    </li>
    <br />  
    <li>
    La "<i>couche de point final</i>" fournit les capacit�s au filtre bas� sur la couche de service fournie par le point final. la "<i>couche 2</i>" peut �tre indiqu�e pour regarder des points finaux fournissant des services d'Ethernet. l'"<i>couche 1</i>" peut �tre indiqu�e pour regarder des points finaux fournissant des services de SONET/SDH.
    </li>
  </ul>
  </p>
</span>
