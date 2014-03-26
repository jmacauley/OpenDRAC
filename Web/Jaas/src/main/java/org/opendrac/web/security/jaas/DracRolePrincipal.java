/**
 * Copyright (c) 2010, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */

package org.opendrac.web.security.jaas;

/**
 *
 * @author hacksaw
 */
import java.io.Serializable;
import java.security.Principal;

/**
 * <p>The identity of a remote client of the DRAC remote API.</p>
 *
 * <p>Principals such as this <code>DracPrincipal</code>
 * may be associated with a particular <code>Subject</code>
 * to augment that <code>Subject</code> with an additional
 * identity.  Refer to the {@link javax.security.auth.Subject}
 * class for more information on how to achieve this.
 * Authorization decisions can then be based upon
 * the Principals associated with a <code>Subject</code>.
 *
 * @see java.security.Principal
 * @see javax.security.auth.Subject
 */
public class DracRolePrincipal implements  Principal, Serializable {
    private static final long serialVersionUID = 1L;
	public static final String ROLE_FENIUS = "FeniusUserGroup";
	public static final String ROLE_NSA = "NSAUserGroup";

    /**
     * @serial The user's name representing the identity of this
     * <code>DracPrincipal</code> object associated with the DRAC Remote API
     * object.
     * @see #getName()
     */
    private String name;

    /**
     * <p>Creates a DracRolePrincipal for a given identity.</p>
     *
     * @param name the DRAC Remote API name for this identity.
     *
     * @exception NullPointerException if the <code>name</code> is
     * <code>null</code>.
     */
    public DracRolePrincipal(String name) {
        if (name == null) {
            throw new NullPointerException("DracRolePrincipal: illegal null input");
        }

        this.name = name;
    }

    /**
     * Returns the name of this principal.
     *
     * <p>
     *
     * @return the name of this <code>DracRolePrincipal</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of this <code>DracRolePrincipal</code>.
     *
     * <p>
     *
     * @return a string representation of this <code>DracRolePrincipal</code>.
     */
    public String toString() {
        return ("DracRolePrincipal: name=\"" + name + "\"");
    }

    /**
     * Compares the specified Object with this <code>DracRolePrincipal</code>
     * for equality.  Returns true if the given object is also a
     * <code>DracRolePrincipal</code> and the two DracPrincipals
     * have the same name.
     *
     * <p>
     *
     * @param o Object to be compared for equality with this
     * <code>DracRolePrincipal</code>.
     *
     * @return true if the specified Object is equal to this
     * <code>DracRolePrincipal</code>.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this  == o) {
            return true;
        }

        if (!(o instanceof  DracRolePrincipal)) {
            return false;
        }

        DracRolePrincipal that = (DracRolePrincipal) o;

        return (this .getName().equals(that.getName()));
    }

    /**
     * Returns a hash code for this <code>DracRolePrincipal</code>.
     *
     * <p>
     *
     * @return a hash code for this <code>DracRolePrincipal</code>.
     */
    public int hashCode() {
        return name.hashCode();
    }
}

