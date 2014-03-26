/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
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

package org.opendrac.nsi.nrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.opendrac.nsi.util.ExceptionCodes;

/**
 *
 * @author hacksaw
 */
public class NrmTerminate {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void terminate(String nsaIdentifier, String scheduleId) throws ServiceException {

        /*
         * Get an active login token.  This will throw a ServiceException if
         * there is an issue.
         */
        NrmManager nrmManager = NrmManager.getInstance();
        NrmLoginManager nrmLoginManager = nrmManager.getNrmLoginManager(nsaIdentifier);
        LoginToken token = nrmLoginManager.getToken();

        RequestHandler rh = RequestHandler.INSTANCE;
		try {
            logger.info("terminate: invoking OpenDRAC cancelSchedule() " + scheduleId);
            rh.cancelSchedule(token, scheduleId);
            logger.info("terminate: successfully cancelled " + scheduleId);
		}
        catch (Exception ex) {
			logger.error("OpenDRAC terminate operation failed", ex);
			throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMException", "OpenDRAC terminate request failed: " + ex.getMessage());
        }
    }

}
