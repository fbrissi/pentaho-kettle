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
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.gethtmldata.GetHTMLDataField;
import org.pentaho.di.trans.steps.gethtmldata.GetHTMLDataMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.utils.JsoupUtils;
import us.codecraft.xsoup.Xsoup;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out loop nodes for an XML file
 *
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
public class HTMLInputFieldsImportProgressDialog {
    private static Class<?> PKG = GetHTMLDataMeta.class; // for i18n purposes, needed by Translator2!!

    private static String VALUE_NAME = "Name";
    private static String VALUE_PATH = "Path";
    private static String VALUE_ELEMENT = "Element";
    private static String VALUE_RESULT = "result";
    private static String VALUE_TYPE = "Type";
    private static String VALUE_FORMAT = "Format";

    private Shell shell;

    private GetHTMLDataMeta meta;

    private String filename;
    private String encoding;

    private int nr;

    private String loopXPath;
    private HashSet<String> list;

    private List<RowMetaAndData> fieldsList;
    private RowMetaAndData[] fields;

    private String xml;
    private String url;

    /**
     * Creates a new dialog that will handle the wait while we're finding out loop nodes for an XML file
     */
    public HTMLInputFieldsImportProgressDialog(Shell shell, GetHTMLDataMeta meta, String filename, String encoding,
                                               String loopXPath) {
        this.shell = shell;
        this.meta = meta;
        this.fields = null;
        this.filename = filename;
        this.encoding = encoding;
        this.nr = 0;
        this.loopXPath = loopXPath;
        this.list = new HashSet<String>();
        this.fieldsList = new ArrayList<RowMetaAndData>();
    }

    public HTMLInputFieldsImportProgressDialog(Shell shell, GetHTMLDataMeta meta, String xmlSource, boolean useUrl,
                                               String loopXPath) {
        this.shell = shell;
        this.meta = meta;
        this.fields = null;
        this.filename = null;
        this.encoding = null;
        this.nr = 0;
        this.loopXPath = loopXPath;
        this.list = new HashSet<String>();
        this.fieldsList = new ArrayList<RowMetaAndData>();
        if (useUrl) {
            this.xml = null;
            this.url = xmlSource;
        } else {
            this.xml = xmlSource;
            this.url = null;
        }
    }

    public RowMetaAndData[] open() {
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    fields = doScan(monitor);
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

        return fields;
    }

    @SuppressWarnings("unchecked")
    private RowMetaAndData[] doScan(IProgressMonitor monitor) throws Exception {
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
            Elements nodes = Xsoup.compile(this.loopXPath).evaluate(document).getElements();
            monitor.worked(1);
            monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.FetchNodes"));

            if (monitor.isCanceled()) {
                return null;
            }
            for (Element node : nodes) {
                if (monitor.isCanceled()) {
                    return null;
                }

                nr++;
                monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.FetchNodes", String
                        .valueOf(nr)));
                monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.FetchNodes",
                        JsoupUtils.getXPath(node)));
                setNodeField(node, monitor);
                childNode(node, monitor);

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

        RowMetaAndData[] listFields = fieldsList.toArray(new RowMetaAndData[fieldsList.size()]);

        monitor.setTaskName(BaseMessages.getString(PKG, "GetHTMLDateLoopNodesImportProgressDialog.Task.NodesReturned"));

        monitor.done();

        return listFields;

    }

    @SuppressWarnings("unchecked")
    private void setNodeField(Node node, IProgressMonitor monitor) {
        Element e = (Element) node;
        // get all attributes
        List<Attribute> lista = e.attributes().asList();
        for (int i = 0; i < lista.size(); i++) {
            setAttributeField(e, lista.get(i), monitor);
        }

        // Get Node Name
        String nodename = node.nodeName();
        String nodenametxt = cleanString(JsoupUtils.getXPath(node));

        if (!Utils.isEmpty(nodenametxt) && !list.contains(nodenametxt)) {
            nr++;
            monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDataXMLInputFieldsImportProgressDialog.Task.FetchFields",
                    String.valueOf(nr)));
            monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDataXMLInputFieldsImportProgressDialog.Task.AddingField",
                    nodename));

            RowMetaAndData row = new RowMetaAndData();
            row.addValue(VALUE_NAME, Value.VALUE_TYPE_STRING, nodename);
            row.addValue(VALUE_PATH, Value.VALUE_TYPE_STRING, nodenametxt);
            row.addValue(VALUE_ELEMENT, Value.VALUE_TYPE_STRING, GetHTMLDataField.ElementTypeDesc[0]);
            row.addValue(VALUE_RESULT, Value.VALUE_TYPE_STRING, GetHTMLDataField.ResultTypeDesc[0]);

            // Get Node value
            String valueNode = e.text();

            // Try to get the Type

            if (IsDate(valueNode)) {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Date");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, "yyyy/MM/dd");
            } else if (IsInteger(valueNode)) {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Integer");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else if (IsNumber(valueNode)) {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Number");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "String");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            }
            fieldsList.add(row);
            list.add(nodenametxt);

        } // end if
    }

    private void setAttributeField(Element element, Attribute attribute, IProgressMonitor monitor) {
        // Get Attribute Name
        String attributname = attribute.getKey();
        String attributnametxt = cleanString(JsoupUtils.getXPath(element, attribute));
        if (!Utils.isEmpty(attributnametxt) && !list.contains(JsoupUtils.getXPath(element, attribute))) {
            nr++;
            monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDataXMLInputFieldsImportProgressDialog.Task.FetchFields",
                    String.valueOf(nr)));
            monitor.subTask(BaseMessages.getString(PKG, "GetHTMLDataXMLInputFieldsImportProgressDialog.Task.AddingField",
                    attributname));

            RowMetaAndData row = new RowMetaAndData();
            row.addValue(VALUE_NAME, Value.VALUE_TYPE_STRING, attributname);
            row.addValue(VALUE_PATH, Value.VALUE_TYPE_STRING, attributnametxt);
            row.addValue(VALUE_ELEMENT, Value.VALUE_TYPE_STRING, GetHTMLDataField.ElementTypeDesc[1]);
            row.addValue(VALUE_RESULT, Value.VALUE_TYPE_STRING, GetHTMLDataField.ResultTypeDesc[0]);

            // Get attribute value
            String valueAttr = attribute.getValue();

            // Try to get the Type

            if (IsDate(valueAttr)) {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Date");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, "yyyy/MM/dd");
            } else if (IsInteger(valueAttr)) {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Integer");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else if (IsNumber(valueAttr)) {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Number");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else {
                row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "String");
                row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            }
            list.add(JsoupUtils.getXPath(element, attribute));
        } // end if

    }

    private String cleanString(String inputstring) {
        String retval = inputstring;
        retval = retval.replace(this.loopXPath, "");
        while (retval.startsWith(GetHTMLDataMeta.N0DE_SEPARATOR)) {
            retval = retval.substring(1, retval.length());
        }

        return retval;
    }

    private boolean IsDate(String str) {
        // TODO: What about other dates? Maybe something for a CRQ
        try {
            SimpleDateFormat fdate = new SimpleDateFormat("yyyy/MM/dd");
            fdate.setLenient(false);
            fdate.parse(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean IsInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean IsNumber(String str) {
        try {
            Float.parseFloat(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean childNode(Node node, IProgressMonitor monitor) {
        boolean rc = false; // true: we found child nodes
        Node ce = node;
        // List child
        for (int j = 0; j < ce.childNodeSize(); j++) {
            Node cnode = ce.childNode(j);
            if (!Utils.isEmpty(cnode.nodeName())) {
                Element cce = (Element) cnode;
                if (cce.childNodeSize() > 1) {
                    if (childNode(cnode, monitor) == false) {
                        // We do not have child nodes ...
                        setNodeField(cnode, monitor);
                        rc = true;
                    }
                } else {
                    setNodeField(cnode, monitor);
                    rc = true;
                }
            }
        }
        return rc;
    }

}
