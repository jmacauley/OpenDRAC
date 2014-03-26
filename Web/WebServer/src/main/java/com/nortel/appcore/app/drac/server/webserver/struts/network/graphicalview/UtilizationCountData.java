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

package com.nortel.appcore.app.drac.server.webserver.struts.network.graphicalview;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;
import de.laures.cewolf.tooltips.XYToolTipGenerator;

public final class UtilizationCountData implements DatasetProducer,
    Serializable, XYToolTipGenerator {
	// These values would normally not be hard coded but produced by
	// some kind of data source like a database or a file

	private static final long serialVersionUID = 0;
	private final TimeSeries ts;
	private TimeSeries ts2; // series with 100% usage, whenever ts has a value not
	                        // equal to 0%

	/**
	 * Constructor
	 * 
	 * @param startTime
	 * @param data
	 * @param fill
	 *          boolean if a second series should be generated and filled to 100%
	 *          to show port is blocked
	 * @param legend1
	 * @param legend2
	 */
	public UtilizationCountData(long startTime, double[] data, boolean fill,
	    String legend1, String legend2) {
		this.ts = new TimeSeries(legend1);
		if (fill) {
			this.ts2 = new TimeSeries(legend2);
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTime);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		for (int i = 0; i < data.length; i++) {
			Minute minute = new Minute(cal.getTime());
			if (data[i] != 0.0 || i == 0 || i == data.length - 1) {

				if (fill) {
					if ((i == 0 || i == data.length - 1) && data[i] == 0) {
						ts2.addOrUpdate(minute, 0);
					}
					else {
						ts2.addOrUpdate(minute, 100);
					}
				}

				ts.addOrUpdate(minute, data[i]);
			}
			cal.add(Calendar.MINUTE, 1);
		}
	}

	/**
	 * Returns a link target for a special data item.
	 */
	public String generateLink(Object data, int series, Object category) {
		return null;
	}

	@Override
	public String generateToolTip(org.jfree.data.xy.XYDataset data, int series,
	    int item) {
		TimeSeriesCollection tsc = (TimeSeriesCollection) data;
		TimeSeries ts = tsc.getSeries(0);
		TimeSeriesDataItem tsdi = ts.getDataItem(item);
		Minute min = (Minute) tsdi.getPeriod();
		return tsdi.getValue() + "% " + min.getHour().getHour() + ":"
		    + (min.getMinute() < 10 ? "0" : "") + min.getMinute();
	}

	/**
	 * Returns a unique ID for this DatasetProducer
	 */
	@Override
	public String getProducerId() {
		return "UtilizationCountData DatasetProducer";
	}

	/**
	 * This producer's data is invalidated after 5 seconds. By this method the
	 * producer can influence Cewolf's caching behaviour the way it wants to.
	 */
	@Override
	public boolean hasExpired(Map params, Date since) {
		// 
		return System.currentTimeMillis() - since.getTime() > 5000;
	}

	/**
	 * Produces some random data.
	 */
	@Override
	public Object produceDataset(Map params) throws DatasetProduceException {

		TimeSeriesCollection tsc = new TimeSeriesCollection();
		tsc.addSeries(ts);
		if (ts2 != null) {
			tsc.addSeries(ts2);
		}
		return tsc;
	}
}
