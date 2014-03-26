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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.util;

import java.awt.EventQueue;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MultiTasker {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public interface DoneListener {
    /*
     * The list passed back through the doneListener is the full result list.
     */
    void done(int completeStatus);

  }

  public interface MultiTaskerExecutorI {
    List<String> doTask(Map<String, Object> workerData, Map<String, String> taskData, List<String> list)
        throws Exception;
  }

  class MultiTaskerCancelListener implements Progress.CancelListener {
    @Override
    public void cancel() {
      MultiTasker.this.cancel();
    }
  }

  class MyWorker extends SwingWorker {
    MultiTasker multitasker;
    List<String> listChunk = null;
    String userWorkerClassName;

    public MyWorker(String userWorkerClassName, MultiTasker multitasker) {
      super();
      this.userWorkerClassName = userWorkerClassName;
      this.multitasker = multitasker;

      multitasker.reg();
    }

    @Override
    public Object construct() {
      try {
        boolean done = false;

        Class<?> cls = Class.forName(userWorkerClassName);
        Constructor<?> ct = cls.getConstructor();
        MultiTaskerExecutorI taskObj = (MultiTaskerExecutorI) ct.newInstance();

        do {
          if (!multitasker.isDone()) {
            listChunk = multitasker.getListChunk();

            multitasker.update(taskObj.doTask(workerData, taskData, listChunk));

            if (multitasker.isDone()) {
              done = true;
            }
          }
          else {
            done = true;
          }
        }
        while (!done);

        return null;
      }

      catch (Exception ex) {
        return ex;
      }
    }

    @Override
    public void finished() {
      Object o = getValue();

      if (o != null && o instanceof Exception) {
        multitasker.setCompletionStatus(ERROR);

        multitasker.dereg();

        Exception e = (Exception) o;
        log.error("MultiTasker: Error(s) occurred for this operation: ", e);

        // this is to avoid popping up as many error dialogs as there are
        // workers; all above
        // conditions post an error
        if (multitasker.errorFlag()) {
          return;
        }
      }
      else {
        multitasker.dereg();
      }
    }
  }

  public static final int SUCCESS = 0;
  public static final int CANCELED = 1;
  public static final int ERROR = 2;

  private List<String> masterList;
  private int chunkSize = 1;
  private int maxWorkers = 1;
  private int reqdWorkers;
  private int current;
  private int workers;
  private Progress progress;
  private boolean cancelled;
  private DoneListener doneListener;
  private boolean workerError;
  private List<String> listToReturnToDoneListener;
  private int completionStatus = SUCCESS;
  private Map<String, Object> workerData;
  private Map<String, String> taskData;
  private MultiTaskerCancelListener cancelListener = new MultiTaskerCancelListener();

  private final int masterListSize;
  private String multiTaskerExecutorClassName;
  private boolean stopOnError; // will stop on error

  public static final String AUTH = "AUTH";
  public static final String EXECUTORCLASSNAME = "EXECUTORCLASSNAME";
  public static final String STOPONERROR = "STOPONERROR";
  public static final String MAXWORKERS = "MAXWORKERS";
  public static final String CHUNKSIZE = "CHUNKSIZE";
  public static final String FRAME = "FRAME";
  public static final String DONELISTENER = "DONELISTENER";

  public MultiTasker(Map<String, Object> workerData, Map<String, String> taskData, List<String> masterList) {
    this.workerData = workerData;
    this.taskData = taskData;
    this.masterList = masterList;
    this.masterListSize = masterList.size();

    if (workerData == null || taskData == null) {
      dereg();
      return;
    }

    multiTaskerExecutorClassName = (String) workerData.get(EXECUTORCLASSNAME);
    this.stopOnError = Boolean.parseBoolean((String) workerData.get(STOPONERROR));
    java.awt.Frame frame = (java.awt.Frame) workerData.get(FRAME);
    this.doneListener = (DoneListener) workerData.get(DONELISTENER);

    if (multiTaskerExecutorClassName == null || multiTaskerExecutorClassName.length() == 0 || masterList == null
        || masterList.size() == 0) {
      dereg();
      return;
    }

    try {
      if (workerData.containsKey(MAXWORKERS)) {
        this.maxWorkers = Integer.parseInt((String) workerData.get(MAXWORKERS));
      }
    }
    catch (Exception e) {

    }

    try {
      if (workerData.containsKey(CHUNKSIZE)) {
        this.chunkSize = Integer.parseInt((String) workerData.get(CHUNKSIZE));
      }
    }
    catch (Exception e) {
      //
    }

    current = 0;
    workers = 0;
    String elementType = null;

    if (taskData != null) {
      elementType = taskData.get("purgeStep");
    }

    reqdWorkers = masterListSize / chunkSize + (masterListSize % chunkSize > 0 ? 1 : 0);

    if (reqdWorkers >= maxWorkers) {
      reqdWorkers = maxWorkers;
    }

    progress = new Progress(elementType, cancelListener, masterListSize, frame, reqdWorkers * chunkSize);

    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        progress.show();
      }
    });
  }

  public void doit() {
    MyWorker[] workers = new MyWorker[reqdWorkers];

    for (int i = 0; i < reqdWorkers; i++) {
      workers[i] = new MyWorker(multiTaskerExecutorClassName, this);
    }

    for (int i = 0; i < reqdWorkers; i++) {
      workers[i].start();
    }
  }

  private void cancel() {
    synchronized (this) {
      current = masterListSize;
      cancelled = true;
      setCompletionStatus(CANCELED);
    }
  }

  private void dereg() {
    workers--;

    if (workers == 0) {
      progress.done(cancelled);

      if (doneListener != null) {
        doneListener.done(completionStatus);
      }

      // Clean Up !!!
      cancelListener = null;
      progress = null;
      workerData = null;
      taskData = null;
      masterList = null;
      listToReturnToDoneListener = null;
      doneListener = null;
    }
  }

  private boolean errorFlag() {
    synchronized (this) {
      boolean b = workerError;
      workerError = true;

      if (stopOnError) {
        current = masterListSize;
      }

      return b;
    }
  }

  private List<String> getListChunk() {
    synchronized (this) {
      List<String> chunk = new ArrayList<String>();
      int j;
      for (j = current; j < current + chunkSize && j < masterListSize; j++) {
        chunk.add(masterList.get(j));
      }
      current = j == masterListSize ? masterListSize : current + chunk.size();
      return chunk;
    }
  }

  private boolean isDone() {
    synchronized (this) {
      return current == masterListSize;
    }
  }

  private void reg() {
    workers++;
  }

  private void setCompletionStatus(int status) {
    completionStatus = status;
  }

  private void update(List<String> list) {
    progress.update(list);

    if (listToReturnToDoneListener != null) {
      listToReturnToDoneListener.addAll(list);
    }
  }

}
