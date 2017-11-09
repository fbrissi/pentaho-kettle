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

package org.pentaho.di.trans.steps.gethtmldata;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import us.codecraft.xsoup.Xsoup;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Read HTML files, parse them and convert them to rows and writes these to one or more output streams.
 *
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
public class GetHTMLData extends BaseStep implements StepInterface {
    private static Class<?> PKG = GetHTMLDataMeta.class; // for i18n purposes, needed by Translator2!!

    private GetHTMLDataMeta meta;
    private GetHTMLDataData data;
    private Object[] prevRow = null; // A pre-allocated spot for the previous row

    public GetHTMLData(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                       Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    protected boolean setDocument(String StringXML, FileObject file, boolean IsInXMLField, boolean readurl)
            throws KettleException {

        this.prevRow = buildEmptyRow(); // pre-allocate previous row

        try {
            if (IsInXMLField) {
                // read string to parse
                data.document = Jsoup.parse(StringXML);
            } else if (readurl) {
                // read url as source
                HttpClient client = HttpClientManager.getInstance().createDefaultClient();
                HttpGet method = new HttpGet(StringXML);
                method.addHeader("Accept-Encoding", "gzip");
                HttpResponse response = client.execute(method);
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                Header contentType = response.getFirstHeader("Content-Type");
                String contentTypeValue = contentType == null ? null : contentType.getValue().indexOf("charset=") < 0 ? null :
                        contentType.getValue().substring(contentType.getValue().indexOf("charset=") + "charset=".length());
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    if (contentEncoding != null) {
                        String acceptEncodingValue = contentEncoding.getValue();
                        if (acceptEncodingValue.contains("gzip")) {

                            GZIPInputStream in = new GZIPInputStream(responseEntity.getContent());

                            data.document = Jsoup.parse(in, contentTypeValue, StringXML);
                        }
                    } else {
                        data.document = Jsoup.parse(responseEntity.getContent(), contentTypeValue, StringXML);
                    }
                }
            } else {
                // get encoding. By default UTF-8
                String encoding = "UTF-8";
                if (!Utils.isEmpty(meta.getEncoding())) {
                    encoding = meta.getEncoding();
                }
                InputStream is = KettleVFS.getInputStream(file);
                try {
                    data.document = Jsoup.parse(is, encoding, file.getURL().toURI().toString());
                } finally {
                    BaseStep.closeQuietly(is);
                }
            }
        } catch (Exception e) {
            if (data.stopPruning) {
                // ignore error when pruning
                return false;
            } else {
                throw new KettleException(e);
            }
        }
        return true;
    }

    /**
     * Build an empty row based on the meta-data.
     *
     * @return empty row built
     */
    private Object[] buildEmptyRow() {
        return RowDataUtil.allocateRowData(data.outputRowMeta.size());
    }

    private void handleMissingFiles() throws KettleException {
        List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();
        if (nonExistantFiles.size() != 0) {
            String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
            logError(BaseMessages.getString(PKG, "GetHTMLData.Log.RequiredFilesTitle"), BaseMessages.getString(PKG,
                    "GetHTMLData.Log.RequiredFiles", message));

            throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.RequiredFilesMissing", message));
        }

        List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
        if (nonAccessibleFiles.size() != 0) {
            String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
            logError(BaseMessages.getString(PKG, "GetHTMLData.Log.RequiredFilesTitle"), BaseMessages.getString(PKG,
                    "GetHTMLData.Log.RequiredNotAccessibleFiles", message));

            throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.RequiredNotAccessibleFilesMissing",
                    message));
        }
    }

    private boolean ReadNextString() {

        try {
            // Grab another row ...
            data.readrow = getRow();

            if (data.readrow == null) {
                // finished processing!

                if (log.isDetailed()) {
                    logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.FinishedProcessing"));
                }
                return false;
            }

            if (first) {
                first = false;

                data.nrReadRow = getInputRowMeta().size();
                data.inputRowMeta = getInputRowMeta();
                data.outputRowMeta = data.inputRowMeta.clone();
                meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);

                // Get total previous fields
                data.totalpreviousfields = data.inputRowMeta.size();

                // Create convert meta-data objects that will contain Date & Number formatters
                data.convertRowMeta = new RowMeta();
                for (ValueMetaInterface valueMeta : data.convertRowMeta.getValueMetaList()) {
                    data.convertRowMeta
                            .addValueMeta(ValueMetaFactory.cloneValueMeta(valueMeta, ValueMetaInterface.TYPE_STRING));
                }

                // For String to <type> conversions, we allocate a conversion meta data row as well...
                //
                data.convertRowMeta = data.outputRowMeta.cloneToType(ValueMetaInterface.TYPE_STRING);

                // Check is XML field is provided
                if (Utils.isEmpty(meta.getXMLField())) {
                    logError(BaseMessages.getString(PKG, "GetHTMLData.Log.NoField"));
                    throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.NoField"));
                }

                // cache the position of the field
                if (data.indexOfXmlField < 0) {
                    data.indexOfXmlField = getInputRowMeta().indexOfValue(meta.getXMLField());
                    if (data.indexOfXmlField < 0) {
                        // The field is unreachable !
                        logError(BaseMessages.getString(PKG, "GetHTMLData.Log.ErrorFindingField", meta.getXMLField()));
                        throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Exception.CouldnotFindField", meta
                                .getXMLField()));
                    }
                }
            }

            if (meta.isInFields()) {
                // get XML field value
                String Fieldvalue = getInputRowMeta().getString(data.readrow, data.indexOfXmlField);

                if (log.isDetailed()) {
                    logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.XMLStream", meta.getXMLField(), Fieldvalue));
                }

                if (meta.getIsAFile()) {
                    FileObject file = null;
                    try {
                        // XML source is a file.
                        file = KettleVFS.getFileObject(Fieldvalue, getTransMeta());

                        if (meta.isIgnoreEmptyFile() && file.getContent().getSize() == 0) {
                            logBasic(BaseMessages.getString(PKG, "GetHTMLData.Error.FileSizeZero", "" + file.getName()));
                            return ReadNextString();
                        }

                        // Open the XML document
                        if (!setDocument(null, file, false, false)) {
                            throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.UnableCreateDocument"));
                        }

                        if (!applyXPath()) {
                            throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.UnableApplyXPath"));
                        }

                        addFileToResultFilesname(file);

                        if (log.isDetailed()) {
                            logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.LoopFileOccurences", "" + data.nodesize, file
                                    .getName().getBaseName()));
                        }

                    } catch (Exception e) {
                        throw new KettleException(e);
                    } finally {
                        try {
                            if (file != null) {
                                file.close();
                            }
                        } catch (Exception e) {
                            // Ignore close errors
                        }
                    }
                } else {
                    boolean url = false;
                    boolean xmltring = true;
                    if (meta.isReadUrl()) {
                        url = true;
                        xmltring = false;
                    }

                    // Open the XML document
                    if (!setDocument(Fieldvalue, null, xmltring, url)) {
                        throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.UnableCreateDocument"));
                    }

                    // Apply XPath and set node list
                    if (!applyXPath()) {
                        throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.UnableApplyXPath"));
                    }
                    if (log.isDetailed()) {
                        logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.LoopFileOccurences", "" + data.nodesize));
                    }
                }
            }
        } catch (Exception e) {
            logError(BaseMessages.getString(PKG, "GetHTMLData.Log.UnexpectedError", e.toString()));
            stopAll();
            logError(Const.getStackTracker(e));
            setErrors(1);
            return false;
        }
        return true;

    }

    private void addFileToResultFilesname(FileObject file) throws Exception {
        if (meta.addResultFile()) {
            // Add this to the result file names...
            ResultFile resultFile =
                    new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname());
            resultFile.setComment(BaseMessages.getString(PKG, "GetHTMLData.Log.FileAddedResult"));
            addResultFile(resultFile);
        }
    }

    public String addNSPrefix(String path, String loopPath) {
        if (data.NSPath.size() > 0) {
            String fullPath = loopPath;
            if (!path.equals(fullPath)) {
                for (String tmp : path.split(GetHTMLDataMeta.N0DE_SEPARATOR)) {
                    if (tmp.equals("..")) {
                        fullPath = fullPath.substring(0, fullPath.lastIndexOf(GetHTMLDataMeta.N0DE_SEPARATOR));
                    } else {
                        fullPath += GetHTMLDataMeta.N0DE_SEPARATOR + tmp;
                    }
                }
            }
            int[] indexs = new int[fullPath.split(GetHTMLDataMeta.N0DE_SEPARATOR).length - 1];
            java.util.Arrays.fill(indexs, -1);
            int length = 0;
            for (int i = 0; i < data.NSPath.size(); i++) {
                if (data.NSPath.get(i).length() > length && fullPath.startsWith(data.NSPath.get(i))) {
                    java.util.Arrays.fill(indexs, data.NSPath.get(i).split(GetHTMLDataMeta.N0DE_SEPARATOR).length - 2,
                            indexs.length, i);
                    length = data.NSPath.get(i).length();
                }
            }

            StringBuilder newPath = new StringBuilder();
            String[] pathStrs = path.split(GetHTMLDataMeta.N0DE_SEPARATOR);
            for (int i = 0; i < pathStrs.length; i++) {
                String tmp = pathStrs[i];
                if (newPath.length() > 0) {
                    newPath.append(GetHTMLDataMeta.N0DE_SEPARATOR);
                }
                if (tmp.length() > 0 && !tmp.contains(":") && !tmp.contains(".") && !tmp.contains(GetHTMLDataMeta.AT)) {
                    int index = indexs[i + indexs.length - pathStrs.length];
                    if (index >= 0) {
                        newPath.append("pre").append(index).append(":").append(tmp);
                    } else {
                        newPath.append(tmp);
                    }
                } else {
                    newPath.append(tmp);
                }
            }
            return newPath.toString();
        }
        return path;
    }

    @SuppressWarnings("unchecked")
    private boolean applyXPath() {
        try {
            data.an = Xsoup.compile(data.PathValue).evaluate(data.document).getElements();
            data.nodesize = data.an.size();
            data.nodenr = 0;
        } catch (Exception e) {
            logError(BaseMessages.getString(PKG, "GetHTMLData.Log.ErrorApplyXPath", e.getMessage()));
            return false;
        }
        return true;
    }

    private boolean openNextFile() {
        try {
            if (data.filenr >= data.files.nrOfFiles()) {
                // finished processing!

                if (log.isDetailed()) {
                    logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.FinishedProcessing"));
                }
                return false;
            }
            // get file
            data.file = data.files.getFile(data.filenr);
            data.filename = KettleVFS.getFilename(data.file);
            // Add additional fields?
            if (meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0) {
                data.shortFilename = data.file.getName().getBaseName();
            }
            if (meta.getPathField() != null && meta.getPathField().length() > 0) {
                data.path = KettleVFS.getFilename(data.file.getParent());
            }
            if (meta.isHiddenField() != null && meta.isHiddenField().length() > 0) {
                data.hidden = data.file.isHidden();
            }
            if (meta.getExtensionField() != null && meta.getExtensionField().length() > 0) {
                data.extension = data.file.getName().getExtension();
            }
            if (meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0) {
                data.lastModificationDateTime = new Date(data.file.getContent().getLastModifiedTime());
            }
            if (meta.getUriField() != null && meta.getUriField().length() > 0) {
                data.uriName = data.file.getName().getURI();
            }
            if (meta.getRootUriField() != null && meta.getRootUriField().length() > 0) {
                data.rootUriName = data.file.getName().getRootURI();
            }
            // Check if file is empty
            long fileSize;
            try {
                fileSize = data.file.getContent().getSize();
            } catch (FileSystemException e) {
                fileSize = -1;
            }

            if (meta.getSizeField() != null && meta.getSizeField().length() > 0) {
                data.size = fileSize;
            }
            // Move file pointer ahead!
            data.filenr++;

            if (meta.isIgnoreEmptyFile() && fileSize == 0) {
                // log only basic as a warning (was before logError)
                logBasic(BaseMessages.getString(PKG, "GetHTMLData.Error.FileSizeZero", "" + data.file.getName()));
                openNextFile();

            } else {
                if (log.isDetailed()) {
                    logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.OpeningFile", data.file.toString()));
                }

                // Open the XML document
                if (!setDocument(null, data.file, false, false)) {
                    if (data.stopPruning) {
                        return false; // ignore error when stopped while pruning
                    }
                    throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.UnableCreateDocument"));
                }

                // Apply XPath and set node list
                if (data.prunePath == null) { // this was already done in processStreaming()
                    if (!applyXPath()) {
                        throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.UnableApplyXPath"));
                    }
                }

                addFileToResultFilesname(data.file);

                if (log.isDetailed()) {
                    logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.FileOpened", data.file.toString()));
                    logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.LoopFileOccurences", "" + data.nodesize, data.file
                            .getName().getBaseName()));
                }
            }
        } catch (Exception e) {
            logError(BaseMessages.getString(PKG, "GetHTMLData.Log.UnableToOpenFile", "" + data.filenr, data.file.toString(),
                    e.toString()));
            stopAll();
            setErrors(1);
            return false;
        }
        return true;
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        if (first && !meta.isInFields()) {
            first = false;

            data.files = meta.getFiles(this);

            if (!meta.isdoNotFailIfNoFile() && data.files.nrOfFiles() == 0) {
                throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Log.NoFiles"));
            }

            handleMissingFiles();

            // Create the output row meta-data
            data.outputRowMeta = new RowMeta();

            meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);

            // Create convert meta-data objects that will contain Date & Number formatters
            // For String to <type> conversions, we allocate a conversion meta data row as well...
            //
            data.convertRowMeta = data.outputRowMeta.cloneToType(ValueMetaInterface.TYPE_STRING);
        }
        // Grab a row
        Object[] r = getXMLRow();
        if (data.errorInRowButContinue) {
            return true; // continue without putting the row out
        }
        if (r == null) {
            setOutputDone(); // signal end to receiver(s)
            return false; // end of data or error.
        }

        return putRowOut(r);

    }

    private boolean putRowOut(Object[] r) throws KettleException {
        if (log.isRowLevel()) {
            logRowlevel(BaseMessages.getString(PKG, "GetHTMLData.Log.ReadRow", data.outputRowMeta.getString(r)));
        }
        incrementLinesInput();
        data.rownr++;
        putRow(data.outputRowMeta, r); // copy row to output rowset(s);

        if (meta.getRowLimit() > 0 && data.rownr > meta.getRowLimit()) {
            // limit has been reached: stop now.
            setOutputDone();
            return false;
        }

        return true;
    }

    private Object[] getXMLRow() throws KettleException {

        if (!meta.isInFields()) {
            while ((data.nodenr >= data.nodesize || data.file == null)) {
                if (!openNextFile()) {
                    data.errorInRowButContinue = false; // stop in all cases
                    return null;
                }
            }
        }
        return getXMLRowPutRowWithErrorhandling();
    }

    private Object[] getXMLRowPutRowWithErrorhandling() throws KettleException {
        // Build an empty row based on the meta-data
        Object[] r;
        data.errorInRowButContinue = false;
        try {
            if (meta.isInFields()) {
                while ((data.nodenr >= data.nodesize || data.readrow == null)) {
                    if (!ReadNextString()) {
                        return null;
                    }
                    if (data.readrow == null) {
                        return null;
                    }
                }
            }

            r = processPutRow(data.an.get(data.nodenr));
        } catch (Exception e) {
            throw new KettleException(BaseMessages.getString(PKG, "GetHTMLData.Error.UnableReadFile"), e);
        }

        return r;
    }

    private Object[] processPutRow(Element node) throws KettleException {
        // Create new row...
        Object[] outputRowData = buildEmptyRow();

        // Create new row or clone
        if (meta.isInFields()) {
            System.arraycopy(data.readrow, 0, outputRowData, 0, data.nrReadRow);
        }
        try {
            data.nodenr++;

            // Read fields...
            for (int i = 0; i < data.nrInputFields; i++) {
                // Get field
                GetHTMLDataField xmlDataField = meta.getInputFields()[i];
                // Get the Path to look for
                String XPathValue = xmlDataField.getXPath();
                XPathValue = environmentSubstitute(XPathValue);
                if (xmlDataField.getElementType() == GetHTMLDataField.ELEMENT_TYPE_ATTRIBUT) {
                    // We have an attribute
                    // do we need to add leading @?
                    // Only put @ to the last element in path, not in front at all
                    int last = XPathValue.lastIndexOf(GetHTMLDataMeta.N0DE_SEPARATOR);
                    if (last > -1) {
                        last++;
                        String attribut = XPathValue.substring(last, XPathValue.length());
                        if (!attribut.startsWith(GetHTMLDataMeta.AT)) {
                            XPathValue = XPathValue.substring(0, last) + GetHTMLDataMeta.AT + attribut;
                        }
                    } else {
                        if (!XPathValue.startsWith(GetHTMLDataMeta.AT)) {
                            XPathValue = GetHTMLDataMeta.AT + XPathValue;
                        }
                    }
                }
                if (meta.isuseToken()) {
                    // See if user use Token inside path field
                    // The syntax is : @_Fieldname-
                    // PDI will search for Fieldname value and replace it
                    // Fieldname must be defined before the current node
                    XPathValue = substituteToken(XPathValue, outputRowData);
                    if (isDetailed()) {
                        logDetailed(XPathValue);
                    }
                }

                // Get node value
                String nodevalue;

                if (xmlDataField.getResultType() == GetHTMLDataField.RESULT_TYPE_VALUE_OF) {
                    nodevalue = Xsoup.compile(XPathValue).evaluate(node).get();
                } else {
                    // nodevalue=node.selectSingleNode(XPathValue).asXML();
                    Element n = Xsoup.compile(XPathValue).evaluate(node).getElements().get(0);
                    if (n != null) {
                        nodevalue = n.html();
                    } else {
                        nodevalue = "";
                    }
                }

                // Do trimming
                switch (xmlDataField.getTrimType()) {
                    case GetHTMLDataField.TYPE_TRIM_LEFT:
                        nodevalue = Const.ltrim(nodevalue);
                        break;
                    case GetHTMLDataField.TYPE_TRIM_RIGHT:
                        nodevalue = Const.rtrim(nodevalue);
                        break;
                    case GetHTMLDataField.TYPE_TRIM_BOTH:
                        nodevalue = Const.trim(nodevalue);
                        break;
                    default:
                        break;
                }

                // Do conversions
                //
                ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(data.totalpreviousfields + i);
                ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(data.totalpreviousfields + i);
                outputRowData[data.totalpreviousfields + i] = targetValueMeta.convertData(sourceValueMeta, nodevalue);

                // Do we need to repeat this field if it is null?
                if (meta.getInputFields()[i].isRepeated()) {
                    if (data.previousRow != null && Utils.isEmpty(nodevalue)) {
                        outputRowData[data.totalpreviousfields + i] = data.previousRow[data.totalpreviousfields + i];
                    }
                }
            } // End of loop over fields...

            int rowIndex = data.totalpreviousfields + data.nrInputFields;

            // See if we need to add the filename to the row...
            if (meta.includeFilename() && !Utils.isEmpty(meta.getFilenameField())) {
                outputRowData[rowIndex++] = data.filename;
            }
            // See if we need to add the row number to the row...
            if (meta.includeRowNumber() && !Utils.isEmpty(meta.getRowNumberField())) {
                outputRowData[rowIndex++] = data.rownr;
            }
            // Possibly add short filename...
            if (meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0) {
                outputRowData[rowIndex++] = data.shortFilename;
            }
            // Add Extension
            if (meta.getExtensionField() != null && meta.getExtensionField().length() > 0) {
                outputRowData[rowIndex++] = data.extension;
            }
            // add path
            if (meta.getPathField() != null && meta.getPathField().length() > 0) {
                outputRowData[rowIndex++] = data.path;
            }
            // Add Size
            if (meta.getSizeField() != null && meta.getSizeField().length() > 0) {
                outputRowData[rowIndex++] = data.size;
            }
            // add Hidden
            if (meta.isHiddenField() != null && meta.isHiddenField().length() > 0) {
                outputRowData[rowIndex++] = Boolean.valueOf(data.path);
            }
            // Add modification date
            if (meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0) {
                outputRowData[rowIndex++] = data.lastModificationDateTime;
            }
            // Add Uri
            if (meta.getUriField() != null && meta.getUriField().length() > 0) {
                outputRowData[rowIndex++] = data.uriName;
            }
            // Add RootUri
            if (meta.getRootUriField() != null && meta.getRootUriField().length() > 0) {
                outputRowData[rowIndex] = data.rootUriName;
            }

            RowMetaInterface irow = getInputRowMeta();

            if (irow == null) {
                data.previousRow = outputRowData;
            } else {
                // clone to previously allocated array to make sure next step doesn't
                // change it in between...
                System.arraycopy(outputRowData, 0, this.prevRow, 0, outputRowData.length);
                // Pick up everything else that needs a real deep clone
                data.previousRow = irow.cloneRow(outputRowData, this.prevRow);
            }
        } catch (Exception e) {
            if (getStepMeta().isDoingErrorHandling()) {
                // Simply add this row to the error row
                putError(data.outputRowMeta, outputRowData, 1, e.toString(), null, "GetHTMLData001");
                data.errorInRowButContinue = true;
                return null;
            } else {
                logError(e.toString());
                throw new KettleException(e.toString());
            }
        }
        return outputRowData;
    }

    public String substituteToken(String aString, Object[] outputRowData) {
        if (aString == null) {
            return null;
        }

        StringBuilder buffer = new StringBuilder();

        String rest = aString;

        // search for closing string
        int i = rest.indexOf(data.tokenStart);
        while (i > -1) {
            int j = rest.indexOf(data.tokenEnd, i + data.tokenStart.length());
            // search for closing string
            if (j > -1) {
                String varName = rest.substring(i + data.tokenStart.length(), j);
                Object Value = varName;

                for (int k = 0; k < data.nrInputFields; k++) {
                    GetHTMLDataField Tmp_xmlInputField = meta.getInputFields()[k];
                    if (Tmp_xmlInputField.getName().equalsIgnoreCase(varName)) {
                        Value = "'" + outputRowData[data.totalpreviousfields + k] + "'";
                    }
                }
                buffer.append(rest.substring(0, i));
                buffer.append(Value);
                rest = rest.substring(j + data.tokenEnd.length());
            } else {
                // no closing tag found; end the search
                buffer.append(rest);
                rest = "";
            }
            // keep searching
            i = rest.indexOf(data.tokenEnd);
        }
        buffer.append(rest);
        return buffer.toString();
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (GetHTMLDataMeta) smi;
        data = (GetHTMLDataData) sdi;

        if (super.init(smi, sdi)) {
            data.rownr = 1L;
            data.nrInputFields = meta.getInputFields().length;
            data.PathValue = environmentSubstitute(meta.getLoopXPath());
            if (Utils.isEmpty(data.PathValue)) {
                logError(BaseMessages.getString(PKG, "GetHTMLData.Error.EmptyPath"));
                return false;
            }
            if (!data.PathValue.substring(0, 1).equals(GetHTMLDataMeta.N0DE_SEPARATOR)) {
                data.PathValue = GetHTMLDataMeta.N0DE_SEPARATOR + data.PathValue;
            }
            if (log.isDetailed()) {
                logDetailed(BaseMessages.getString(PKG, "GetHTMLData.Log.LoopXPath", data.PathValue));
            }

            data.prunePath = environmentSubstitute(meta.getPrunePath());
            if (data.prunePath != null) {
                if (Utils.isEmpty(data.prunePath.trim())) {
                    data.prunePath = null;
                } else {
                    // ensure a leading slash
                    if (!data.prunePath.startsWith(GetHTMLDataMeta.N0DE_SEPARATOR)) {
                        data.prunePath = GetHTMLDataMeta.N0DE_SEPARATOR + data.prunePath;
                    }
                    // check if other conditions apply that do not allow pruning
                    if (meta.isInFields()) {
                        data.prunePath = null; // not possible by design, could be changed later on
                    }
                }
            }

            return true;
        }
        return false;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (GetHTMLDataMeta) smi;
        data = (GetHTMLDataData) sdi;
        if (data.file != null) {
            try {
                data.file.close();
            } catch (Exception e) {
                // Ignore close errors
            }
        }
        if (data.an != null) {
            data.an.clear();
            data.an = null;
        }
        if (data.NAMESPACE != null) {
            data.NAMESPACE.clear();
            data.NAMESPACE = null;
        }
        if (data.NSPath != null) {
            data.NSPath.clear();
            data.NSPath = null;
        }
        if (data.readrow != null) {
            data.readrow = null;
        }
        if (data.document != null) {
            data.document = null;
        }
        if (data.fr != null) {
            BaseStep.closeQuietly(data.fr);
        }
        if (data.is != null) {
            BaseStep.closeQuietly(data.is);
        }
        if (data.files != null) {
            data.files = null;
        }
        super.dispose(smi, sdi);
    }

}
