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

package com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.common;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.NETWORK_MONITORING_SERVICE_NS;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.NETWORK_MONITORING_SERVICE_V3_0_NS;

import java.util.Map;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.opendrac.drac.server.ws.networkmonitoringservice.EndpointsV3;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.Endpoints;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.request.QueryEndpointRequest;

public final class NetworkMonitoringHelper {
	private final ServiceUtilities serviceUtil = new ServiceUtilities();
	private String namespace = NETWORK_MONITORING_SERVICE_NS;

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String parseQueryEndpointRequestOMElement(OMElement inputMsg, String nsPrefix) throws InvalidInputException {
		OMElement selectedNode = null;
		String tna = null;
		// tna
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix, QueryEndpointRequest.TNA)) != null) {
			tna = selectedNode.getText().trim();
			if (tna == null) {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_TNA;
				throw new InvalidInputException(DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}

		return tna;
	}

	public OMElement prepareQueryEndpointResponseOMElement(EndPointType endpoint, String nsPrefix) {
		if ("xmlns".equals(nsPrefix)) {
			nsPrefix = "";
		}
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(namespace, nsPrefix);
		OMElement response = fac.createOMElement(RequestResponseConstants.RAW_QUERY_ENDPOINT_RESPONSE, ns);
		OMElement isFound = fac.createOMElement(RequestResponseConstants.RAW_IS_MATCHING_RECORD_FOUND, ns, response);
		if (endpoint == null) {
			isFound.setText("false");
			return response;
		}
		isFound.setText("true");
		OMElement endPoint = fac.createOMElement(RequestResponseConstants.RAW_ENDPOINT, ns, response);
		// tna
		OMElement tna = fac.createOMElement(RequestResponseConstants.RAW_TNA, ns, endPoint);
		tna.setText(endpoint.getName());
		// type
		OMElement type = fac.createOMElement(RequestResponseConstants.RAW_TYPE, ns, endPoint);
		type.setText(endpoint.getType());
		// speed
		OMElement speed = fac.createOMElement(RequestResponseConstants.RAW_SPEED, ns, endPoint);
		speed.setText(Integer.toString(endpoint.getDataRate()));
		// mtu
		OMElement mtu = fac.createOMElement(RequestResponseConstants.RAW_MTU, ns, endPoint);
		mtu.setText(Integer.toString(endpoint.getMtu()));
		// physicalAddress
		OMElement physicalAddress = fac.createOMElement(RequestResponseConstants.RAW_PHYSICAL_ADDRESS, ns, endPoint);
		physicalAddress.setText(endpoint.getPhysAddr());
		// status
		OMElement state = fac.createOMElement(RequestResponseConstants.RAW_STATUS, ns, endPoint);
		state.setText(endpoint.getState());
		// layer
		OMElement layer = fac.createOMElement(RequestResponseConstants.RAW_LAYER, ns, endPoint);
		layer.setText(endpoint.getLayer());
		// id
		OMElement id = fac.createOMElement(RequestResponseConstants.RAW_ID, ns, endPoint);
		id.setText(endpoint.getId());
		// cost
		OMElement cost = fac.createOMElement(RequestResponseConstants.RAW_COST, ns, endPoint);
		cost.setText(Integer.toString(endpoint.getCost()));
		// metric
		OMElement metric = fac.createOMElement(RequestResponseConstants.RAW_METRIC, ns, endPoint);
		metric.setText(Integer.toString(endpoint.getMetric()));
		// srlg
		OMElement srlg = fac.createOMElement(RequestResponseConstants.RAW_SRLG_BUCKET_NUMBER, ns, endPoint);
		srlg.setText(endpoint.getSrlg());
		// constrain
		OMElement constrain = fac.createOMElement(RequestResponseConstants.RAW_CONSTRAIN, ns, endPoint);
		constrain.setText(endpoint.getConstrain().toString());
		// channel
		OMElement channel = fac.createOMElement(RequestResponseConstants.RAW_CHANNEL, ns, endPoint);
		channel.setText(Integer.toString(endpoint.getChannelNumber()));
		// signallingType
		OMElement sigType = fac.createOMElement(RequestResponseConstants.RAW_SIGNALLING_TYPE, ns, endPoint);
		sigType.setText(endpoint.getSignalingType());

		Map<String, String> attributes = endpoint.getAttributes();
		// Iterator<String> attrItr = temp.keySet().iterator();
		//
		// HashMap<String, String> = new HashMap<String, String>();
		// while (attrItr.hasNext())
		// {
		// String key = attrItr.next();
		// Object value = temp.get(key);
		// if (value instanceof AttributeType)
		// {
		// attributes.put(key, ((AttributeType) value).getValue());
		// }
		// }

		// passControlFrame
		OMElement passControlFrame = fac.createOMElement(RequestResponseConstants.RAW_PASS_CONTROL_FRAME, ns, endPoint);
		passControlFrame.setText(attributes.get(FacilityConstants.PASSCTRL_ATTR));

		// txConditioning
		OMElement txConditioning = fac.createOMElement(RequestResponseConstants.RAW_TX_CONDITIONING, ns, endPoint);
		txConditioning.setText(attributes.get(FacilityConstants.TXCOND_ATTR));

		// sts
		OMElement sts = fac.createOMElement(RequestResponseConstants.RAW_STS, ns, endPoint);
		sts.setText(attributes.get(FacilityConstants.STS_ATTR));

		// vcat
		OMElement vcat = fac.createOMElement(RequestResponseConstants.RAW_VCAT, ns, endPoint);
		vcat.setText(attributes.get(FacilityConstants.VCAT_ATTR));

		// autoNegotiation
		OMElement autoNegotiation = fac.createOMElement(RequestResponseConstants.RAW_AUTO_NEGOTIATION, ns, endPoint);
		autoNegotiation.setText(attributes.get(FacilityConstants.AN_ATTR));

		// autoNegotiationStatus
		OMElement autoNegotiationStatus = fac.createOMElement(RequestResponseConstants.RAW_AUTO_NEGOTIATION_STATUS, ns,
		    endPoint);
		autoNegotiationStatus.setText(attributes.get(FacilityConstants.ANSTATUS_ATTR));

		// advertisedDuplex
		OMElement advertisedDuplex = fac.createOMElement(RequestResponseConstants.RAW_ADVERTISED_DUPLEX, ns, endPoint);
		advertisedDuplex.setText(attributes.get(FacilityConstants.ANETHDPX_ATTR));

		// flowControl
		OMElement flowControl = fac.createOMElement(RequestResponseConstants.RAW_FLOW_CONTROL, ns, endPoint);
		flowControl.setText(attributes.get(FacilityConstants.FLOWCTRL_ATTR));

		// controlPauseTx
		OMElement controlPauseTx = fac.createOMElement(RequestResponseConstants.RAW_CONTROL_PAUSE_TX, ns, endPoint);
		controlPauseTx.setText(attributes.get(FacilityConstants.PAUSETX_ATTR));

		// controlPauseRx
		OMElement controlPauseRx = fac.createOMElement(RequestResponseConstants.RAW_CONTROL_PAUSE_RX, ns, endPoint);
		controlPauseRx.setText(attributes.get(FacilityConstants.PAUSERX_ATTR));

		// negotiatedDuplex
		OMElement negotiatedDuplex = fac.createOMElement(RequestResponseConstants.RAW_NEGOTIATED_DUPLEX, ns, endPoint);
		negotiatedDuplex.setText(attributes.get(FacilityConstants.NETHDPX_ATTR));

		// negotiatedSpeed
		OMElement negotiatedSpeed = fac.createOMElement(RequestResponseConstants.RAW_NEGOTIATED_SPEED, ns, endPoint);
		negotiatedSpeed.setText(attributes.get(FacilityConstants.NSPEED_ATTR));

		// negotiatedPauseTx
		OMElement negotiatedPauseTx = fac.createOMElement(RequestResponseConstants.RAW_NEGOTIATED_PAUSE_TX, ns, endPoint);
		negotiatedPauseTx.setText(attributes.get(FacilityConstants.NPAUSETX_ATTR));

		// negotiatedPauseRx
		OMElement negotiatedPauseRx = fac.createOMElement(RequestResponseConstants.RAW_NEGOTIATED_PAUSE_RX, ns, endPoint);
		negotiatedPauseRx.setText(attributes.get(FacilityConstants.NPAUSERX_ATTR));

		// linkPartnerDuplex
		OMElement linkPartnerDuplex = fac.createOMElement(RequestResponseConstants.RAW_LINK_PARTNER_DUPLEX, ns, endPoint);
		linkPartnerDuplex.setText(attributes.get(FacilityConstants.LPETHDPX_ATTR));

		// linkPartnerSpeed
		OMElement linkPartnerSpeed = fac.createOMElement(RequestResponseConstants.RAW_LINK_PARTNER_SPEED, ns, endPoint);
		linkPartnerSpeed.setText(attributes.get(FacilityConstants.LPSPEED_ATTR));

		// linkPartnerFlowControl
		OMElement linkPartnerFlowControl = fac.createOMElement(RequestResponseConstants.RAW_LINK_PARTNER_FLOW_CONTROL, ns,
		    endPoint);
		linkPartnerFlowControl.setText(attributes.get(FacilityConstants.LPFLOWCTRL_ATTR));

		// interfaceType
		OMElement interfaceType = fac.createOMElement(RequestResponseConstants.RAW_INTERFACE_TYPE, ns, endPoint);
		interfaceType.setText(attributes.get(FacilityConstants.IFTYPE_ATTR));

		// policing
		OMElement policing = fac.createOMElement(RequestResponseConstants.RAW_POLICING, ns, endPoint);
		policing.setText(attributes.get(FacilityConstants.POLICING_ATTR));

		// encapsulationType
		OMElement encapsulationType = fac.createOMElement(RequestResponseConstants.RAW_ENCAPSULATION_TYPE, ns, endPoint);
		encapsulationType.setText(attributes.get(FacilityConstants.ETYPE_ATTR));

		// priorityMode
		OMElement priorityMode = fac.createOMElement(RequestResponseConstants.RAW_PRIORITY_MODE, ns, endPoint);
		priorityMode.setText(attributes.get(FacilityConstants.PRIORITYMODE_ATTR));

		// bandwidthThreshold
		OMElement bandwidthThreshold = fac.createOMElement(RequestResponseConstants.RAW_BANDWIDTH_THRESHOLD, ns, endPoint);
		bandwidthThreshold.setText(attributes.get(FacilityConstants.BWTHRESHOLD_ATTR));

		// remainedBandwidth
		OMElement remainedBandwidth = fac.createOMElement(RequestResponseConstants.RAW_REMAINED_BANDWIDTH, ns, endPoint);
		remainedBandwidth.setText(attributes.get(FacilityConstants.BWREMAIN_ATTR));

		// bandwidthUtilization
		OMElement bandwidthUtilization = fac.createOMElement(RequestResponseConstants.RAW_BANDWIDTH_UTILIZATION, ns,
		    endPoint);
		bandwidthUtilization.setText(attributes.get(FacilityConstants.BWUTL_ATTR));

		// lagId
		OMElement lagId = fac.createOMElement(RequestResponseConstants.RAW_LAG_ID, ns, endPoint);
		lagId.setText(attributes.get(FacilityConstants.LADID_ATTR));

		// vlanId
		// OMElement vlanId =
		// fac.createOMElement(RequestResponseConstants.RAW_VLANID, ns, endPoint);
		// vlanId.setText(attributes.get(EndPointType.VLANID_ATTR));

		// site
		OMElement site = fac.createOMElement(RequestResponseConstants.RAW_SITEID, ns, endPoint);
		site.setText(attributes.get(FacilityConstants.SITE_ATTR));

		// wavelength
		OMElement wavelength = fac.createOMElement(RequestResponseConstants.RAW_WAVELENGTH, ns, endPoint);
		wavelength.setText(attributes.get(FacilityConstants.WAVELENGTH_ATTR));

		OMElement domain = fac.createOMElement(RequestResponseConstants.RAW_DOMAIN, ns, endPoint);
		// TODO: Uncomment-out the next line when the domain attribute is available
		// in the EndpointType
		// domain.setText(attributes.get(EndPointType.DOMAIN_ATTR));
		domain.setText("");

		// userLabel
		OMElement userLabel = fac.createOMElement(RequestResponseConstants.FACLABEL_ATTR, ns, endPoint);
		userLabel.setText(attributes.get(FacilityConstants.FACLABEL_ATTR));
		
		
	// remaining bandwidth
			OMElement remainingBandwith = fac.createOMElement(RequestResponseConstants.RAW_REMAINED_BANDWIDTH, ns, endPoint);
			remainingBandwith.setText(attributes.get(FacilityConstants.BWREMAIN_ATTR));
		return response;
	}

	public Endpoints endpointsFactory(OMElement inputMsg, String nsPrefix, LoginToken loginToken) throws Exception {
		if (namespace.equals(NETWORK_MONITORING_SERVICE_V3_0_NS)) {
			return new EndpointsV3(inputMsg, nsPrefix, namespace, loginToken);
		}
		else {
			return new Endpoints(inputMsg, nsPrefix, namespace, loginToken);
		}
	}
}
