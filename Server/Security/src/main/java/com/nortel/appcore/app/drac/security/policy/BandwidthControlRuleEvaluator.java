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

package com.nortel.appcore.app.drac.security.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.security.policy.types.BandwidthControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;

public final class BandwidthControlRuleEvaluator {
  
  private static final Logger log = LoggerFactory.getLogger(BandwidthControlRuleEvaluator.class);
	public static final long INVALID_DURATION = -1;
	private static final int TO_MILSECOND = 1000;

	private BandwidthControlRuleEvaluator() {
	}

	public static void evaluate(BandwidthControlRule rule, long duration,
	    int rate, long fromTime, long toTime, UserGroupName billingGroup,
	    String sourceEndpoint, String resourceGroupSourceEndpoint,
	    String targetEndpoint, String resourceGroupTargetEndpoint)
	    throws Exception {
		log.debug("BandwidthControlRuleEvaluator: rule " + rule + " duration "
		    + duration + " rate " + rate + " fromTime " + fromTime + " toTime "
		    + toTime + " billingGroup " + billingGroup + " sourceEndpoint "
		    + sourceEndpoint + " resourceGroupSourceEndpoint "
		    + resourceGroupSourceEndpoint + " targetEndpoint " + targetEndpoint
		    + " resourceGroupTargetEndpoint " + resourceGroupTargetEndpoint);
		/***************************************/
		/* evaluating maximum service size */
		/***************************************/
		if (rule.getMaximumServiceSize() != null
		    && rule.getMaximumServiceSize().intValue() < rate) {
			log.debug("Policy check failed on maximumServiceSize: "
			    + rule.getMaximumServiceSize().intValue() + " vs entered rate of: "
			    + rate);

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_EXCEED_BW_SIZE, new Object[] { rate,
			        rule.getMaximumServiceSize().intValue() });
		}

		/***************************************/
		/* evaluating maximum service duration */
		/***************************************/

		if (duration == INVALID_DURATION) {
			// WP: Not sure about this, if duration is -1 what does it mean?
			return;
		}

		if (rule.getMaximumServiceDuration() != null
		    && rule.getMaximumServiceDuration().intValue() < duration
		        / TO_MILSECOND) {
			log.debug("Policy check failed on maximumServiceDuration: "
			    + rule.getMaximumServiceDuration().intValue()
			    + " vs entered duration of: " + duration / TO_MILSECOND);

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_EXCEED_BW_DURATION, new Object[] {
			        duration / TO_MILSECOND,
			        rule.getMaximumServiceDuration().intValue() });
		}

		if (rule.getMaximumServiceBandwidth() != null
		    && rule.getMaximumServiceBandwidth() < duration * rate / TO_MILSECOND) {
			log.debug("Policy check failed on maximumServiceBandwidth: "
			    + rule.getMaximumServiceBandwidth().intValue()
			    + " vs entered bandwidth of: " + duration * rate / TO_MILSECOND);

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_EXCEED_BW_SERVICE_SIZE, new Object[] {
			        duration * rate / TO_MILSECOND,
			        rule.getMaximumServiceBandwidth().intValue() });
		}

		if (rule.getMaximumAggregateServiceSize() == null) {
			// No further checking required.
			return;
		}

		double sum;
		double currentBandwidth = duration * rate / TO_MILSECOND;

		if (billingGroup != null) {

			try {
				sum = DbLightPath.INSTANCE
				    .getTotalOverLappingServiceRatesByBillingGroup(fromTime, toTime,
				        billingGroup);
			}
			catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				throw ex;
			}

			log.debug("Policy checking on maximumAggretateServiceBandwidth of "
			    + billingGroup + ": "
			    + rule.getMaximumAggregateServiceSize().intValue()
			    + " vs total service bandwidth of: " + (sum + currentBandwidth));

			if (rule.getMaximumAggregateServiceSize() < sum + currentBandwidth) {
				log.debug("Policy check: failed ");
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_EXCEED_BW_AGG_SERVICE_SIZE,
				    new Object[] { sum + currentBandwidth,
				        rule.getMaximumAggregateServiceSize().intValue() });
			}

		}

		if (sourceEndpoint != null && resourceGroupSourceEndpoint != null) {
			try {
				sum = DbLightPath.INSTANCE
				    .getTotalOverLappingServiceRatesByResourceGroup(fromTime, toTime,
				        sourceEndpoint, resourceGroupSourceEndpoint, true);
			}
			catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				throw ex;
			}

			log.debug("Policy checking on maximumAggretateServiceBandwidth of "
			    + sourceEndpoint + ": "
			    + rule.getMaximumAggregateServiceSize().intValue()
			    + " vs service bandwidth of: " + (sum + currentBandwidth));

			if (rule.getMaximumAggregateServiceSize() < sum + currentBandwidth) {
				log.debug("Policy check: failed");
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_EXCEED_BW_AGG_SERVICE_SIZE,
				    new Object[] { sum + currentBandwidth,
				        rule.getMaximumAggregateServiceSize().intValue() });
			}
		}

		if (targetEndpoint != null && resourceGroupTargetEndpoint != null) {
			try {
				sum = DbLightPath.INSTANCE
				    .getTotalOverLappingServiceRatesByResourceGroup(fromTime, toTime,
				        targetEndpoint, resourceGroupTargetEndpoint, false);
			}
			catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				throw ex;
			}
			log.debug("Policy checking on maximumAggretateServiceBandwidth of "
			    + targetEndpoint + ": "
			    + rule.getMaximumAggregateServiceSize().intValue()
			    + " vs service bandwidth of: " + (sum + currentBandwidth));

			if (rule.getMaximumAggregateServiceSize() < sum + currentBandwidth) {
				log.debug("Policy check: failed");
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_EXCEED_BW_AGG_SERVICE_SIZE,
				    new Object[] { sum + currentBandwidth,
				        rule.getMaximumAggregateServiceSize().intValue() });
			}
		}

	}
}
