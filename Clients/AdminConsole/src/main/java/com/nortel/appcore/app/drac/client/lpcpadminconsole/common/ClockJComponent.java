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
package com.nortel.appcore.app.drac.client.lpcpadminconsole.common;

import java.awt.Graphics;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;

public class ClockJComponent extends JComponent implements Runnable {
  private static final long serialVersionUID = 164640918215457289L;

  private static final SimpleDateFormat DATEFORMATTER = new SimpleDateFormat(DateFormatter.COMMON_DATETIME_FORMAT);
  private String timeZoneId;
  
  private final Point point;

  public ClockJComponent(final Point point) {
    this(TimeZone.getDefault().getID(), point);
  }

  public ClockJComponent(final String timeZoneId, final Point point) {
    super();
    this.timeZoneId = timeZoneId;
    this.point = point;
  }

  @Override
  public void run() {
    while (true) {
      repaint();
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public void paint(Graphics g) {
    g.drawString(getFormattedDisplay(), point.x, point.y);
  }

  public void setTimeZoneId(String tzId) {
    this.timeZoneId = tzId;
  }

  private String getFormattedDisplay() {
    // Friday, November 26, 2010 02:51:10 am GMT+12:00
    Calendar calendar = new GregorianCalendar();
    DATEFORMATTER.setTimeZone(TimeZone.getTimeZone(this.timeZoneId));
    return DATEFORMATTER.format(calendar.getTime());
  }

  public static class MultiTicker implements Runnable {
    private final List<ClockJComponent> clocks;

    public MultiTicker(List<ClockJComponent> clocks) {
      this.clocks = clocks;
    }

    @Override
    public void run() {
      while (true) {
        for (ClockJComponent clock : clocks) {
          clock.repaint();
        }
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
      }
    }
  }

}
