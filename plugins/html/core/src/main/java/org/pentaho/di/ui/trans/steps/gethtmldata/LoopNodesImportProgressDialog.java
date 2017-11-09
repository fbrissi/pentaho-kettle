/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.gethtmldata;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.gethtmldata.GetHTMLDataMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.utils.JsoupUtils;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out loop nodes for an XML file
 *
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
public class LoopNodesImportProgressDialog {
    private static Class<?> PKG = GetHTMLDataMeta.class; // for i18n purposes, needed by Translator2!!

    private Shell shell;

    private GetHTMLDataMeta meta;

    private String[] Xpaths;

    private String filename;
    private String xml;
    private String url;
    private String encoding;

    private ArrayList<String> listpath;

    private int nr;

    /**
     * Creates a new dialog that will handle the wait while we're finding out loop nodes for an XML file
     */
    public LoopNodesImportProgressDialog(Shell shell, GetHTMLDataMeta meta, String filename, String encoding) {
        this.shell = shell;
        this.meta = meta;
        this.Xpaths = null;
        this.filename = filename;
        this.encoding = encoding;
        this.listpath = new ArrayList<String>();
        this.nr = 0;
        this.xml = null;
        this.url = null;
    }

    public LoopNodesImportProgressDialog(Shell shell, GetHTMLDataMeta meta, String xmlSource, boolean useUrl) {
        this.shell = shell;
        this.meta = meta;
        this.Xpaths = null;
        this.filename = null;
        this.encoding = null;
        this.listpath = new ArrayList<String>();
        this.nr = 0;
        if (useUrl) {
            this.xml = null;
            this.url = xmlSource;
        } else {
            this.xml = xmlSource;
            this.url = null;
        }
    }

    public String[] open() {
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    Xpaths = doScan(monitor);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new InvocationTargetException(e, BaseMessages.getString(PKG,
                            "GetHTMLDateLoopNodesImportProgressDialog.Exception.ErrorScanningFile", filename, e.toString()));
                }
            }
        };

        try {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            pmd.run(true, true, op);
        } catch (InvocationTargetException e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG,
                    "GetHTMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Title"), BaseMessages.getString(PKG,
                    "GetHTMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Message"), e);
        } catch (InterruptedException e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG,
                    "GetHTMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Title"), BaseMessages.getString(PKG,
                    "GetHTMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Message"), e);
        }

        return Xpaths;
    }

    @SuppressWarnings("unchecked")
    private String[] doScan(IProgressMonitor monitor) throws Exception {
        monitor.beginTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.ScanningFile",
                filename), 1);

        monitor.worked(1);
        if (monitor.isCanceled()) {
            return null;
        }
        monitor.worked(1);
        monitor
                .beginTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.ReadingDocument"), 1);
        if (monitor.isCanceled()) {
            return null;
        }
        InputStream is = null;
        try {
            Document document = null;
            if (!Utils.isEmpty(filename)) {
                is = KettleVFS.getInputStream(filename);
                document = Jsoup.parse(is, encoding, KettleVFS.getFriendlyURI(filename));
            } else {
                if (!Utils.isEmpty(xml)) {
                    document = Jsoup.parse(xml);
                } else {
                    document = Jsoup.parse(new URL(url), Integer.MAX_VALUE);
                }
            }
            monitor.worked(1);
            monitor.beginTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.DocumentOpened"),
                    1);
            monitor.worked(1);
            monitor.beginTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.ReadingNode"), 1);

            if (monitor.isCanceled()) {
                return null;
            }
            Elements nodes = document.getElementsByTag(document.root().nodeName());
            monitor.worked(1);
            monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.FetchNodes"));

            if (monitor.isCanceled()) {
                return null;
            }
            for (Node node : nodes) {
                if (monitor.isCanceled()) {
                    return null;
                }
                if (!listpath.contains(JsoupUtils.getXPath(node))) {
                    nr++;
                    monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.FetchNodes",
                            String.valueOf(nr)));
                    monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.AddingNode",
                            JsoupUtils.getXPath(node)));
                    listpath.add(JsoupUtils.getXPath(node));
                    addLoopXPath(node, monitor);
                }
            }
            monitor.worked(1);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) { /* Ignore */
            }
        }
        String[] list_xpath = listpath.toArray(new String[listpath.size()]);

        monitor.setTaskName(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.NodesReturned"));

        monitor.done();

        return list_xpath;

    }

    private void addLoopXPath(Node node, IProgressMonitor monitor) {
        Element ce = (Element) node;
        monitor.worked(1);
        // List child
        for (int j = 0; j < ce.childNodeSize(); j++) {
            if (monitor.isCanceled()) {
                return;
            }
            Node cnode = ce.childNode(j);

            if (!Utils.isEmpty(cnode.nodeName())) {
                Element cce = (Element) cnode;
                if (!listpath.contains(JsoupUtils.getXPath(cnode))) {
                    nr++;
                    monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.FetchNodes",
                            String.valueOf(nr)));
                    monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.AddingNode",
                            JsoupUtils.getXPath(cnode)));
                    listpath.add(JsoupUtils.getXPath(cnode));
                }
                // let's get child nodes
                if (cce.childNodeSize() > 1) {
                    addLoopXPath(cnode, monitor);
                }
            }
        }
    }

}
