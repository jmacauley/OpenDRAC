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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.plaf.BasicCalendarComboBox;

public final class CalendarComboBox extends JComboBox implements
    ComponentComboPopupObserver {

  private class AbsoluteConvAction implements ActionListener {

    AbsoluteConvAction() {
      // do nothing
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      String command = ((JMenuItem) ae.getSource()).getText();
      Calendar selection = model.getSelection();
      if ("Now".equals(command)) {
        selection.setTime(new Date());
      }
      else if (command.startsWith("Start")) {
        model.setMillis(0);
        model.setSeconds(0);
        selection = model.getSelection();
        if (!command.endsWith("Minute")) {
          model.setMinutes(0);
          selection = model.getSelection();
          if (!command.endsWith("Hour")) {
            model.rollHoursToBoundary(false, false);
            selection = model.getSelection();
            if (!command.endsWith("Day")) {
              selection.set(5, 1);
              if (!command.endsWith("Month")) {
                selection.set(2, 0);
              }
            }
          }
        }
      }
      else {
        model.setMillis(999);
        model.setSeconds(59);
        selection = model.getSelection();
        if (!command.endsWith("Minute")) {
          model.setMinutes(59);
          selection = model.getSelection();
          if (!command.endsWith("Hour")) {
            model.rollHoursToBoundary(true, false);
            selection = model.getSelection();
            if (!command.endsWith("Day")) {
              selection.set(5, DayOfMonthGrid.getDaysInMonth(selection));
              if (!command.endsWith("Month")) {
                selection.set(2, 11);
                selection.set(5, DayOfMonthGrid.getDaysInMonth(selection));
              }
            }
          }
        }
      }
      model.setSelection(selection);
      if (restoreMainPopupAfterConv) {
        showPopup();
      }
      else {
        fireActionEvent();
      }
    }
  }

  private class PopupListener extends MouseAdapter implements PopupMenuListener {

    PopupListener() {
      // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
      fireActionEvent();
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent popupmenuevent) {
      // do nothing
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent popupmenuevent) {
      // do nothing
    }

    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popupConvMenu(false);
      }
    }
  }

  private class RelativeConvAction implements ActionListener {

    RelativeConvAction() {
      // do nothing
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      JMenuItem menuItem = (JMenuItem) ae.getSource();
      int index = convRelMenu.getComponentIndex(menuItem);
      RelativeDate relDate = convRelDates.get(index);
      model.setRelativeDate(relDate);
      if (restoreMainPopupAfterConv) {
        showPopup();
      }
      else {
        fireActionEvent();
      }
    }
  }

  private static final long serialVersionUID = 1L;
  private CalendarComboBoxModel model;
  private SimpleDateFormat dateFormatter;
  private SimpleDateFormat toolTipDateFormatter;
  private AppCommonDateFormat appCommonDateFormat;
  private final JPopupMenu convAbsMenu = new JPopupMenu();
  private final JPopupMenu convRelMenu = new JPopupMenu();
  private final List<RelativeDate> convRelDates = new ArrayList<RelativeDate>();
  private boolean restoreMainPopupAfterConv;

  public CalendarComboBox() {
    this(true, false, false);
  }

  public CalendarComboBox(boolean showTime, boolean showAMPM,
      boolean showRelative) {
    this(new CalendarComboBoxModel(Calendar.getInstance().get(1) - 150,
        Calendar.getInstance().get(1) + 150), showTime, showAMPM, showRelative,
        null);
  }

  public CalendarComboBox(final CalendarComboBoxModel theModel,
      boolean doShowTime, boolean doShowAMPM, boolean doShowRelative,
      String dateFormat) {
    super(theModel);

    dateFormatter = null;
    toolTipDateFormatter = null;
    appCommonDateFormat = null;
    model = theModel;

    theModel.addObserver(new CalendarComboBoxModelObserverI() {

      @Override
      public void update(Date newSelection) {
        updateToolTipText();
        repaint();
      }

    });

    setUI(new BasicCalendarComboBox(this, doShowTime, doShowAMPM,
        doShowRelative, getFont()));

    if (dateFormat != null) {
      dateFormatter = new SimpleDateFormat(dateFormat);
      String toolTipDateFormat = dateFormat;
      if (!toolTipDateFormat.endsWith("Z")) {
        toolTipDateFormat = toolTipDateFormat + "Z";
      }
      toolTipDateFormatter = new SimpleDateFormat(toolTipDateFormat);
    }
    updateToolTipText();
    setRenderer(new DefaultListCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus) {
        if (appCommonDateFormat == null) {
          theModel.setTimeZone(CommonDateFormat.getTimeZone());
        }
        else {
          theModel.setTimeZone(appCommonDateFormat.getTimeZone());
        }
        if (index == -1) {
          return super.getListCellRendererComponent(list,
              formatDate((Date) value), index, isSelected, cellHasFocus);
        }
        return super.getListCellRendererComponent(list, value, index,
            isSelected, cellHasFocus);
      }

    });
    buildAbsoluteConvMenu();
    buildRelativeConvMenu();
    PopupListener mouseAndMenuListener = new PopupListener();
    addMouseListener(mouseAndMenuListener);
    convAbsMenu.addPopupMenuListener(mouseAndMenuListener);
    convRelMenu.addPopupMenuListener(mouseAndMenuListener);
    Component cs[] = getComponents();
    for (Component element : cs) {
      element.addMouseListener(mouseAndMenuListener);
    }

  }

  public static void main(String args[]) {
    final CalendarComboBox comboBox = new CalendarComboBox(true, false, true);
    JButton switchButton = new JButton("Switch Time Zone");
    final AppCommonDateFormat formatter = new AppCommonDateFormat();
    final TimeZone local = TimeZone.getDefault();
    final TimeZone utc = TimeZone.getTimeZone("UTC");
    formatter.setTimeZone(local);
    comboBox.setAppCommonDateFormat(formatter);

    ActionListener switchIt = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (local.equals(formatter.getTimeZone())) {
          formatter.setTimeZone(utc);
        }
        else {
          formatter.setTimeZone(local);
        }
      }

    };
    switchButton.addActionListener(switchIt);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(switchButton);
    JFrame frame = new JFrame("CalendarComboBoxC");
    frame.getContentPane().add(comboBox, "North");
    frame.getContentPane().add(buttonPanel, "South");
    frame.pack();
    frame.setDefaultCloseOperation(3);
    frame.setVisible(true);
  }

  @Override
  public void comboPopupChanged() {
    fireActionEvent();
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension currentDimension = super.getPreferredSize();
    return new Dimension(currentDimension.width + 120, currentDimension.height);
  }

  public Date getSelection() {
    return (Date) model.getSelectedItem();
  }

  public void setAppCommonDateFormat(AppCommonDateFormat formatter) {
    appCommonDateFormat = formatter;
    if (formatter != null) {
      model.setTimeZone(formatter.getTimeZone());
    }
    else {
      model.setTimeZone(CommonDateFormat.getTimeZone());
    }
    repaint();
  }

  @Override
  public void setSelectedItem(Object item) {
    if (item == null) {
      setSelection(new Date());
    }
    else {
      setSelection((Date) item);
    }
  }

  public void setSelection(Date date) {
    if (date instanceof RelativeDate) {
      model.setRelativeDate((RelativeDate) date);
    }
    else {
      Calendar calendar = model.getSelection();
      calendar.setTime(date);
      model.setSelection(calendar);
    }
  }

  public void updateToolTipText() {
    Date selection = getSelection();
    if (selection instanceof RelativeDate) {
      setToolTipText(((RelativeDate) selection).formatRelativeDateForTooltip());
    }
    else if (toolTipDateFormatter != null) {
      toolTipDateFormatter.setTimeZone(model.getTimeZone());
      setToolTipText(toolTipDateFormatter.format(getSelection()));
    }
    else if (appCommonDateFormat != null) {
      String text = appCommonDateFormat.getCommonToolTipFormat(getSelection());
      setToolTipText(text);
    }
    else {
      setToolTipText(CommonDateFormat.getCommonToolTipFormat(getSelection()));
    }
  }

  void popupConvMenu(boolean restorePopup) {
    restoreMainPopupAfterConv = restorePopup;
    if (model.getRelativeDate() == null) {
      convAbsMenu.show(this, 0, getHeight());
    }
    else {
      convRelMenu.show(this, 0, getHeight());
    }
  }

  private void buildAbsoluteConvMenu() {
    AbsoluteConvAction convAction = new AbsoluteConvAction();
    JMenuItem item = new JMenuItem("Now");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("Start of Minute");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("End of Minute");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("Start of Hour");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("End of Hour");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("Start of Day");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("End of Day");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("Start of Month");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("End of Month");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("Start of Year");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
    item = new JMenuItem("End of Year");
    item.addActionListener(convAction);
    convAbsMenu.add(item);
  }

  private void buildRelativeConvMenu() {
    RelativeConvAction convAction = new RelativeConvAction();
    RelativeDate relDate = new RelativeDate(0, 0, false);
    convRelDates.add(relDate);
    relDate = new RelativeDate(1, 0, false);
    convRelDates.add(relDate);
    int fifteen = 15;
    relDate = new RelativeDate(fifteen, 0, false);
    convRelDates.add(relDate);
    int thirty = 30;
    relDate = new RelativeDate(thirty, 0, false);
    convRelDates.add(relDate);
    int fortyFive = 45;
    relDate = new RelativeDate(fortyFive, 0, false);
    convRelDates.add(relDate);
    relDate = new RelativeDate(1, 1, false);
    convRelDates.add(relDate);
    relDate = new RelativeDate(2, 1, false);
    convRelDates.add(relDate);
    int four = 4;
    relDate = new RelativeDate(four, 1, false);
    convRelDates.add(relDate);
    int eight = 8;
    relDate = new RelativeDate(eight, 1, false);
    convRelDates.add(relDate);
    int twelve = 12;
    relDate = new RelativeDate(twelve, 1, false);
    convRelDates.add(relDate);
    int sixteen = 16;
    relDate = new RelativeDate(sixteen, 1, false);
    convRelDates.add(relDate);
    relDate = new RelativeDate(1, 2, false);
    convRelDates.add(relDate);
    int seven = 7;
    relDate = new RelativeDate(seven, 2, false);
    convRelDates.add(relDate);
    int forteen = 14;
    relDate = new RelativeDate(forteen, 2, false);
    convRelDates.add(relDate);
    relDate = new RelativeDate(1, 3, false);
    convRelDates.add(relDate);
    relDate = new RelativeDate(1, 4, false);
    convRelDates.add(relDate);
    for (int i = 0; i < convRelDates.size(); i++) {
      relDate = convRelDates.get(i);
      JMenuItem item = new JMenuItem(relDate.formatRelativeDate());
      convRelMenu.add(item);
      item.addActionListener(convAction);
    }

  }

  private String formatDate(Date date) {
    if (date instanceof RelativeDate) {
      return ((RelativeDate) date).formatRelativeDate();
    }
    if (dateFormatter != null) {
      dateFormatter.setTimeZone(model.getTimeZone());
      return dateFormatter.format(getSelection());
    }
    if (appCommonDateFormat != null) {
      return appCommonDateFormat.getCommonDateTimeFormat(getSelection());
    }
    return CommonDateFormat.getCommonDateTimeFormat(getSelection());
  }

}
