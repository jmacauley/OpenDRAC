package com.nortel.appcore.app.drac.client.lpcpadminconsole;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.common.types.GraphData;

public class GetGraphDataWorker extends SwingWorker<Object, Object> {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private GraphData graphData;
	private final OpenDracDesktop dracDesktop;

	public GetGraphDataWorker() {
		this.dracDesktop = null;
	}

	public GetGraphDataWorker(OpenDracDesktop dracDesktop) {
		this.dracDesktop = dracDesktop;
	}

	@Override
	public Object doInBackground() {
		try {
			graphData = new ServerOperation().getGraphData();
		}
		catch (Exception e) {
			log.error("DracDesktop::getAndDisplayServerGraph error", e);
			return e;
		}

		return null;
	}

	@Override
	protected void done() {
		try {
			if (get() == null) {
				if (dracDesktop != null){
					dracDesktop.hideProgressDialog();
				}
				NetworkGraph.INSTANCE.setGraphData(graphData);
			}
		}
		catch (Exception e) {
			log.error("TDracDesktop::getAndDisplayServerGraph error", e);
		}
	}

}