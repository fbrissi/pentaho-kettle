//CHECKSTYLE:FileLength:OFF
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.gethtmldata.GetHTMLDataField;
import org.pentaho.di.trans.steps.gethtmldata.GetHTMLDataMeta;
import org.pentaho.di.ui.core.dialog.*;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
public class GetHTMLDataDialog extends BaseStepDialog implements StepDialogInterface {
    private String XMLSource = null;

    private static Class<?> PKG = GetHTMLDataMeta.class; // for i18n purposes, needed by Translator2!!

    private static String EMPTY_FIELDS = "<EMPTY>";

    private CTabFolder wTabFolder;
    private FormData fdTabFolder;

    private CTabItem wFileTab, wContentTab, wFieldsTab;

    private Composite wFileComp, wContentComp, wFieldsComp;
    private FormData fdFileComp, fdContentComp, fdFieldsComp;

    private Label wlFilename, wlXMLIsAFile;
    private Button wbbFilename; // Browse: add file or directory
    private Button wbdFilename; // Delete
    private Button wbeFilename; // Edit
    private Button wbaFilename; // Add or change
    private TextVar wFilename;
    private FormData fdlFilename, fdbFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename;

    private Label wlFilenameList;
    private TableView wFilenameList;
    private FormData fdlFilenameList, fdFilenameList;

    private Label wlFilemask;
    private TextVar wFilemask;
    private FormData fdlFilemask, fdFilemask;

    private Button wbShowFiles;
    private FormData fdbShowFiles;

    private FormData fdlXMLField, fdlXMLStreamField, fdlXMLIsAFile;
    private FormData fdXMLField, fdXSDFileField;
    private FormData fdOutputField, fdXMLIsAFile, fdAdditionalFields, fdAddFileResult, fdXmlConf;
    private Label wlXMLField, wlXmlStreamField;
    private CCombo wXMLField;
    private Button wXMLStreamField, wXMLIsAFile;

    private Label wlInclFilename;
    private Button wInclFilename, wAddResult;
    private FormData fdlInclFilename, fdInclFilename, fdAddResult, fdlAddResult;

    private Label wlNameSpaceAware;
    private Button wNameSpaceAware;
    private FormData fdlNameSpaceAware, fdNameSpaceAware;

    private Label wlreadUrl;
    private Button wreadUrl;
    private FormData fdlreadUrl, fdreadUrl;

    private Label wlIgnoreComment;
    private Button wIgnoreComment;
    private FormData fdlIgnoreComment, fdIgnoreComment;

    private Label wlInclFilenameField;
    private TextVar wInclFilenameField;
    private FormData fdlInclFilenameField, fdInclFilenameField;

    private Label wlInclRownum, wlAddResult;
    private Button wInclRownum;
    private FormData fdlInclRownum, fdRownum;

    private Label wlInclRownumField;
    private TextVar wInclRownumField;
    private FormData fdlInclRownumField, fdInclRownumField;

    private Label wlLimit;
    private Text wLimit;
    private FormData fdlLimit, fdLimit;

    private Label wlLoopXPath;
    private TextVar wLoopXPath;
    private FormData fdlLoopXPath, fdLoopXPath;

    private Label wlPrunePath;
    private TextVar wPrunePath;
    private FormData fdlPrunePath, fdPrunePath;

    private Label wlEncoding;
    private CCombo wEncoding;
    private FormData fdlEncoding, fdEncoding;

    private TableView wFields;
    private FormData fdFields;

    private Group wOutputField;
    private Group wAdditionalFields;
    private Group wAddFileResult;
    private Group wXmlConf;

    private Button wbbLoopPathList;
    private FormData fdbLoopPathList;

    private Label wlExcludeFilemask;
    private TextVar wExcludeFilemask;
    private FormData fdlExcludeFilemask, fdExcludeFilemask;

    // ignore empty files flag
    private Label wlIgnoreEmptyFile;
    private Button wIgnoreEmptyFile;
    private FormData fdlIgnoreEmptyFile, fdIgnoreEmptyFile;

    // do not fail if no files?
    private Label wldoNotFailIfNoFile;
    private Button wdoNotFailIfNoFile;
    private FormData fdldoNotFailIfNoFile, fddoNotFailIfNoFile;

    private CTabItem wAdditionalFieldsTab;
    private Composite wAdditionalFieldsComp;
    private FormData fdAdditionalFieldsComp;

    private Label wlShortFileFieldName;
    private FormData fdlShortFileFieldName;
    private TextVar wShortFileFieldName;
    private FormData fdShortFileFieldName;
    private Label wlPathFieldName;
    private FormData fdlPathFieldName;
    private TextVar wPathFieldName;
    private FormData fdPathFieldName;

    private Label wlIsHiddenName;
    private FormData fdlIsHiddenName;
    private TextVar wIsHiddenName;
    private FormData fdIsHiddenName;
    private Label wlLastModificationTimeName;
    private FormData fdlLastModificationTimeName;
    private TextVar wLastModificationTimeName;
    private FormData fdLastModificationTimeName;
    private Label wlUriName;
    private FormData fdlUriName;
    private TextVar wUriName;
    private FormData fdUriName;
    private Label wlRootUriName;
    private FormData fdlRootUriName;
    private TextVar wRootUriName;
    private FormData fdRootUriName;
    private Label wlExtensionFieldName;
    private FormData fdlExtensionFieldName;
    private TextVar wExtensionFieldName;
    private FormData fdExtensionFieldName;
    private Label wlSizeFieldName;
    private FormData fdlSizeFieldName;
    private TextVar wSizeFieldName;
    private FormData fdSizeFieldName;

    private GetHTMLDataMeta input;

    private int middle;
    private int margin;
    private ModifyListener lsMod;

    private boolean gotEncodings = false;

    public static final int[] dateLengths = new int[]{23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6};

    String precNodeName = null;

    public GetHTMLDataDialog(Shell parent, Object in, TransMeta transMeta, String sname) {

        super(parent, (BaseStepMeta) in, transMeta, sname);
        input = (GetHTMLDataMeta) in;
    }

    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, input);

        lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                input.setChanged();
            }
        };
        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.DialogTitle"));

        middle = props.getMiddlePct();
        margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.top = new FormAttachment(0, margin);
        fdlStepname.right = new FormAttachment(middle, -margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        // ////////////////////////
        // START OF FILE TAB ///
        // ////////////////////////
        wFileTab = new CTabItem(wTabFolder, SWT.NONE);
        wFileTab.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.File.Tab"));

        wFileComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wFileComp);

        FormLayout fileLayout = new FormLayout();
        fileLayout.marginWidth = 3;
        fileLayout.marginHeight = 3;
        wFileComp.setLayout(fileLayout);

        // ///////////////////////////////
        // START OF Output Field GROUP //
        // ///////////////////////////////

        wOutputField = new Group(wFileComp, SWT.SHADOW_NONE);
        props.setLook(wOutputField);
        wOutputField.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.wOutputField.Label"));

        FormLayout outputfieldgroupLayout = new FormLayout();
        outputfieldgroupLayout.marginWidth = 10;
        outputfieldgroupLayout.marginHeight = 10;
        wOutputField.setLayout(outputfieldgroupLayout);

        // Is XML string defined in a Field
        wlXmlStreamField = new Label(wOutputField, SWT.RIGHT);
        wlXmlStreamField.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.wlXmlStreamField.Label"));
        props.setLook(wlXmlStreamField);
        fdlXMLStreamField = new FormData();
        fdlXMLStreamField.left = new FormAttachment(0, -margin);
        fdlXMLStreamField.top = new FormAttachment(0, margin);
        fdlXMLStreamField.right = new FormAttachment(middle, -2 * margin);
        wlXmlStreamField.setLayoutData(fdlXMLStreamField);

        wXMLStreamField = new Button(wOutputField, SWT.CHECK);
        props.setLook(wXMLStreamField);
        wXMLStreamField.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.wXmlStreamField.Tooltip"));
        fdXSDFileField = new FormData();
        fdXSDFileField.left = new FormAttachment(middle, -margin);
        fdXSDFileField.top = new FormAttachment(0, margin);
        wXMLStreamField.setLayoutData(fdXSDFileField);
        SelectionAdapter lsxmlstream = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                XMLSource = null;
                ActiveXmlStreamField();
                input.setChanged();
            }
        };
        wXMLStreamField.addSelectionListener(lsxmlstream);

        // Is XML source is a file?
        wlXMLIsAFile = new Label(wOutputField, SWT.RIGHT);
        wlXMLIsAFile.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.XMLIsAFile.Label"));
        props.setLook(wlXMLIsAFile);
        fdlXMLIsAFile = new FormData();
        fdlXMLIsAFile.left = new FormAttachment(0, -margin);
        fdlXMLIsAFile.top = new FormAttachment(wXMLStreamField, margin);
        fdlXMLIsAFile.right = new FormAttachment(middle, -2 * margin);
        wlXMLIsAFile.setLayoutData(fdlXMLIsAFile);

        wXMLIsAFile = new Button(wOutputField, SWT.CHECK);
        props.setLook(wXMLIsAFile);
        wXMLIsAFile.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.XMLIsAFile.Tooltip"));
        fdXMLIsAFile = new FormData();
        fdXMLIsAFile.left = new FormAttachment(middle, -margin);
        fdXMLIsAFile.top = new FormAttachment(wXMLStreamField, margin);
        wXMLIsAFile.setLayoutData(fdXMLIsAFile);
        SelectionAdapter lsxmlisafile = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                XMLSource = null;
                if (wXMLIsAFile.getSelection()) {
                    wreadUrl.setSelection(false);
                }
                input.setChanged();
            }
        };
        wXMLIsAFile.addSelectionListener(lsxmlisafile);

        // read url as source ?
        wlreadUrl = new Label(wOutputField, SWT.RIGHT);
        wlreadUrl.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.readUrl.Label"));
        props.setLook(wlreadUrl);
        fdlreadUrl = new FormData();
        fdlreadUrl.left = new FormAttachment(0, -margin);
        fdlreadUrl.top = new FormAttachment(wXMLIsAFile, margin);
        fdlreadUrl.right = new FormAttachment(middle, -2 * margin);
        wlreadUrl.setLayoutData(fdlreadUrl);
        wreadUrl = new Button(wOutputField, SWT.CHECK);
        props.setLook(wreadUrl);
        wreadUrl.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.readUrl.Tooltip"));
        fdreadUrl = new FormData();
        fdreadUrl.left = new FormAttachment(middle, -margin);
        fdreadUrl.top = new FormAttachment(wXMLIsAFile, margin);
        wreadUrl.setLayoutData(fdreadUrl);
        SelectionAdapter lsreadurl = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                XMLSource = null;
                if (wreadUrl.getSelection()) {
                    wXMLIsAFile.setSelection(false);
                }
                input.setChanged();
            }
        };
        wreadUrl.addSelectionListener(lsreadurl);

        // If XML string defined in a Field
        wlXMLField = new Label(wOutputField, SWT.RIGHT);
        wlXMLField.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.wlXMLField.Label"));
        props.setLook(wlXMLField);
        fdlXMLField = new FormData();
        fdlXMLField.left = new FormAttachment(0, -margin);
        fdlXMLField.top = new FormAttachment(wreadUrl, margin);
        fdlXMLField.right = new FormAttachment(middle, -2 * margin);
        wlXMLField.setLayoutData(fdlXMLField);

        wXMLField = new CCombo(wOutputField, SWT.BORDER | SWT.READ_ONLY);
        wXMLField.setEditable(true);
        props.setLook(wXMLField);
        wXMLField.addModifyListener(lsMod);
        fdXMLField = new FormData();
        fdXMLField.left = new FormAttachment(middle, -margin);
        fdXMLField.top = new FormAttachment(wreadUrl, margin);
        fdXMLField.right = new FormAttachment(100, -margin);
        wXMLField.setLayoutData(fdXMLField);
        wXMLField.addFocusListener(new FocusListener() {
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
            }

            public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                setXMLStreamField();
                shell.setCursor(null);
                busy.dispose();
            }
        });

        fdOutputField = new FormData();
        fdOutputField.left = new FormAttachment(0, margin);
        fdOutputField.top = new FormAttachment(wFilenameList, margin);
        fdOutputField.right = new FormAttachment(100, -margin);
        wOutputField.setLayoutData(fdOutputField);

        // ///////////////////////////////////////////////////////////
        // / END OF Output Field GROUP
        // ///////////////////////////////////////////////////////////

        // Filename line
        wlFilename = new Label(wFileComp, SWT.RIGHT);
        wlFilename.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.Filename.Label"));
        props.setLook(wlFilename);
        fdlFilename = new FormData();
        fdlFilename.left = new FormAttachment(0, 0);
        fdlFilename.top = new FormAttachment(wOutputField, margin);
        fdlFilename.right = new FormAttachment(middle, -margin);
        wlFilename.setLayoutData(fdlFilename);

        wbbFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
        props.setLook(wbbFilename);
        wbbFilename.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameBrowse.Button"));
        wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdbFilename = new FormData();
        fdbFilename.right = new FormAttachment(100, 0);
        fdbFilename.top = new FormAttachment(wOutputField, margin);
        wbbFilename.setLayoutData(fdbFilename);

        wbaFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
        props.setLook(wbaFilename);
        wbaFilename.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameAdd.Button"));
        wbaFilename.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameAdd.Tooltip"));
        fdbaFilename = new FormData();
        fdbaFilename.right = new FormAttachment(wbbFilename, -margin);
        fdbaFilename.top = new FormAttachment(wOutputField, margin);
        wbaFilename.setLayoutData(fdbaFilename);

        wFilename = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFilename);
        wFilename.addModifyListener(lsMod);
        fdFilename = new FormData();
        fdFilename.left = new FormAttachment(middle, 0);
        fdFilename.right = new FormAttachment(wbaFilename, -margin);
        fdFilename.top = new FormAttachment(wOutputField, margin);
        wFilename.setLayoutData(fdFilename);

        wlFilemask = new Label(wFileComp, SWT.RIGHT);
        wlFilemask.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.RegExp.Label"));
        props.setLook(wlFilemask);
        fdlFilemask = new FormData();
        fdlFilemask.left = new FormAttachment(0, 0);
        fdlFilemask.top = new FormAttachment(wFilename, margin);
        fdlFilemask.right = new FormAttachment(middle, -margin);
        wlFilemask.setLayoutData(fdlFilemask);
        wFilemask = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFilemask);
        wFilemask.addModifyListener(lsMod);
        fdFilemask = new FormData();
        fdFilemask.left = new FormAttachment(middle, 0);
        fdFilemask.top = new FormAttachment(wFilename, margin);
        fdFilemask.right = new FormAttachment(100, 0);
        wFilemask.setLayoutData(fdFilemask);

        wlExcludeFilemask = new Label(wFileComp, SWT.RIGHT);
        wlExcludeFilemask.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.ExcludeFilemask.Label"));
        props.setLook(wlExcludeFilemask);
        fdlExcludeFilemask = new FormData();
        fdlExcludeFilemask.left = new FormAttachment(0, 0);
        fdlExcludeFilemask.top = new FormAttachment(wFilemask, margin);
        fdlExcludeFilemask.right = new FormAttachment(middle, -margin);
        wlExcludeFilemask.setLayoutData(fdlExcludeFilemask);
        wExcludeFilemask = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wExcludeFilemask);
        wExcludeFilemask.addModifyListener(lsMod);
        fdExcludeFilemask = new FormData();
        fdExcludeFilemask.left = new FormAttachment(middle, 0);
        fdExcludeFilemask.top = new FormAttachment(wFilemask, margin);
        fdExcludeFilemask.right = new FormAttachment(wFilename, 0, SWT.RIGHT);
        wExcludeFilemask.setLayoutData(fdExcludeFilemask);

        // Filename list line
        wlFilenameList = new Label(wFileComp, SWT.RIGHT);
        wlFilenameList.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameList.Label"));
        props.setLook(wlFilenameList);
        fdlFilenameList = new FormData();
        fdlFilenameList.left = new FormAttachment(0, 0);
        fdlFilenameList.top = new FormAttachment(wExcludeFilemask, margin);
        fdlFilenameList.right = new FormAttachment(middle, -margin);
        wlFilenameList.setLayoutData(fdlFilenameList);

        // Buttons to the right of the screen...
        wbdFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
        props.setLook(wbdFilename);
        wbdFilename.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameRemove.Button"));
        wbdFilename.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameRemove.Tooltip"));
        fdbdFilename = new FormData();
        fdbdFilename.right = new FormAttachment(100, 0);
        fdbdFilename.top = new FormAttachment(wExcludeFilemask, 40);
        wbdFilename.setLayoutData(fdbdFilename);

        wbeFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
        props.setLook(wbeFilename);
        wbeFilename.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameEdit.Button"));
        wbeFilename.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilenameEdit.Tooltip"));
        fdbeFilename = new FormData();
        fdbeFilename.right = new FormAttachment(100, 0);
        fdbeFilename.left = new FormAttachment(wbdFilename, 0, SWT.LEFT);
        fdbeFilename.top = new FormAttachment(wbdFilename, margin);
        wbeFilename.setLayoutData(fdbeFilename);

        wbShowFiles = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
        props.setLook(wbShowFiles);
        wbShowFiles.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.ShowFiles.Button"));
        fdbShowFiles = new FormData();
        fdbShowFiles.left = new FormAttachment(middle, 0);
        fdbShowFiles.bottom = new FormAttachment(100, 0);
        wbShowFiles.setLayoutData(fdbShowFiles);

        ColumnInfo[] colinfo = new ColumnInfo[5];
        colinfo[0] =
                new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.Files.Filename.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false);
        colinfo[1] =
                new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.Files.Wildcard.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false);
        colinfo[2] =
                new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.Files.ExcludeWildcard.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false);

        colinfo[0].setUsingVariables(true);
        colinfo[1].setUsingVariables(true);
        colinfo[1].setToolTip(BaseMessages.getString(PKG, "GetHTMLDataDialog.Files.Wildcard.Tooltip"));
        colinfo[2].setUsingVariables(true);
        colinfo[2].setToolTip(BaseMessages.getString(PKG, "GetHTMLDataDialog.Files.ExcludeWildcard.Tooltip"));
        colinfo[3] =
                new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.Required.Column"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, GetHTMLDataMeta.RequiredFilesDesc);
        colinfo[3].setToolTip(BaseMessages.getString(PKG, "GetHTMLDataDialog.Required.Tooltip"));
        colinfo[4] =
                new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.IncludeSubDirs.Column"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, GetHTMLDataMeta.RequiredFilesDesc);
        colinfo[4].setToolTip(BaseMessages.getString(PKG, "GetHTMLDataDialog.IncludeSubDirs.Tooltip"));

        wFilenameList =
                new TableView(transMeta, wFileComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 2, lsMod, props);
        props.setLook(wFilenameList);
        fdFilenameList = new FormData();
        fdFilenameList.left = new FormAttachment(middle, 0);
        fdFilenameList.right = new FormAttachment(wbdFilename, -margin);
        fdFilenameList.top = new FormAttachment(wExcludeFilemask, margin);
        fdFilenameList.bottom = new FormAttachment(wbShowFiles, -margin);
        wFilenameList.setLayoutData(fdFilenameList);

        fdFileComp = new FormData();
        fdFileComp.left = new FormAttachment(0, 0);
        fdFileComp.top = new FormAttachment(0, 0);
        fdFileComp.right = new FormAttachment(100, 0);
        fdFileComp.bottom = new FormAttachment(100, 0);
        wFileComp.setLayoutData(fdFileComp);

        wFileComp.layout();
        wFileTab.setControl(wFileComp);

        // ///////////////////////////////////////////////////////////
        // / END OF FILE TAB
        // ///////////////////////////////////////////////////////////

        // ////////////////////////
        // START OF CONTENT TAB///
        // /
        wContentTab = new CTabItem(wTabFolder, SWT.NONE);
        wContentTab.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.Content.Tab"));

        FormLayout contentLayout = new FormLayout();
        contentLayout.marginWidth = 3;
        contentLayout.marginHeight = 3;

        wContentComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wContentComp);
        wContentComp.setLayout(contentLayout);

        // ///////////////////////////////
        // START OF XmlConf Field GROUP //
        // ///////////////////////////////

        wXmlConf = new Group(wContentComp, SWT.SHADOW_NONE);
        props.setLook(wXmlConf);
        wXmlConf.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.wXmlConf.Label"));

        FormLayout XmlConfgroupLayout = new FormLayout();
        XmlConfgroupLayout.marginWidth = 10;
        XmlConfgroupLayout.marginHeight = 10;
        wXmlConf.setLayout(XmlConfgroupLayout);

        wbbLoopPathList = new Button(wXmlConf, SWT.PUSH | SWT.CENTER);
        props.setLook(wbbLoopPathList);
        wbbLoopPathList.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.LoopPathList.Button"));
        wbbLoopPathList.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdbLoopPathList = new FormData();
        fdbLoopPathList.right = new FormAttachment(100, 0);
        fdbLoopPathList.top = new FormAttachment(0, 0);
        wbbLoopPathList.setLayoutData(fdbLoopPathList);

        wbbLoopPathList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getLoopPathList();
            }
        });

        wlLoopXPath = new Label(wXmlConf, SWT.RIGHT);
        wlLoopXPath.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.LoopXPath.Label"));
        props.setLook(wlLoopXPath);
        fdlLoopXPath = new FormData();
        fdlLoopXPath.left = new FormAttachment(0, 0);
        fdlLoopXPath.top = new FormAttachment(0, margin);
        fdlLoopXPath.right = new FormAttachment(middle, -margin);
        wlLoopXPath.setLayoutData(fdlLoopXPath);
        wLoopXPath = new TextVar(transMeta, wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLoopXPath.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.LoopXPath.Tooltip"));
        props.setLook(wLoopXPath);
        wLoopXPath.addModifyListener(lsMod);
        fdLoopXPath = new FormData();
        fdLoopXPath.left = new FormAttachment(middle, 0);
        fdLoopXPath.top = new FormAttachment(0, margin);
        fdLoopXPath.right = new FormAttachment(wbbLoopPathList, -margin);
        wLoopXPath.setLayoutData(fdLoopXPath);

        wlEncoding = new Label(wXmlConf, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding = new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top = new FormAttachment(wLoopXPath, margin);
        fdlEncoding.right = new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding = new CCombo(wXmlConf, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding = new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top = new FormAttachment(wLoopXPath, margin);
        fdEncoding.right = new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener() {
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
            }

            public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                setEncodings();
                shell.setCursor(null);
                busy.dispose();
            }
        });

        // Set Namespace aware ?
        wlNameSpaceAware = new Label(wXmlConf, SWT.RIGHT);
        wlNameSpaceAware.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.NameSpaceAware.Label"));
        props.setLook(wlNameSpaceAware);
        fdlNameSpaceAware = new FormData();
        fdlNameSpaceAware.left = new FormAttachment(0, 0);
        fdlNameSpaceAware.top = new FormAttachment(wEncoding, margin);
        fdlNameSpaceAware.right = new FormAttachment(middle, -margin);
        wlNameSpaceAware.setLayoutData(fdlNameSpaceAware);
        wNameSpaceAware = new Button(wXmlConf, SWT.CHECK);
        props.setLook(wNameSpaceAware);
        wNameSpaceAware.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.NameSpaceAware.Tooltip"));
        fdNameSpaceAware = new FormData();
        fdNameSpaceAware.left = new FormAttachment(middle, 0);
        fdNameSpaceAware.top = new FormAttachment(wEncoding, margin);
        wNameSpaceAware.setLayoutData(fdNameSpaceAware);

        // Ignore comments ?
        wlIgnoreComment = new Label(wXmlConf, SWT.RIGHT);
        wlIgnoreComment.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.IgnoreComment.Label"));
        props.setLook(wlIgnoreComment);
        fdlIgnoreComment = new FormData();
        fdlIgnoreComment.left = new FormAttachment(0, 0);
        fdlIgnoreComment.top = new FormAttachment(wNameSpaceAware, margin);
        fdlIgnoreComment.right = new FormAttachment(middle, -margin);
        wlIgnoreComment.setLayoutData(fdlIgnoreComment);
        wIgnoreComment = new Button(wXmlConf, SWT.CHECK);
        props.setLook(wIgnoreComment);
        wIgnoreComment.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.IgnoreComment.Tooltip"));
        fdIgnoreComment = new FormData();
        fdIgnoreComment.left = new FormAttachment(middle, 0);
        fdIgnoreComment.top = new FormAttachment(wNameSpaceAware, margin);
        wIgnoreComment.setLayoutData(fdIgnoreComment);

        // Ignore Empty File
        wlIgnoreEmptyFile = new Label(wXmlConf, SWT.RIGHT);
        wlIgnoreEmptyFile.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.IgnoreEmptyFile.Label"));
        props.setLook(wlIgnoreEmptyFile);
        fdlIgnoreEmptyFile = new FormData();
        fdlIgnoreEmptyFile.left = new FormAttachment(0, 0);
        fdlIgnoreEmptyFile.top = new FormAttachment(wIgnoreComment, margin);
        fdlIgnoreEmptyFile.right = new FormAttachment(middle, -margin);
        wlIgnoreEmptyFile.setLayoutData(fdlIgnoreEmptyFile);
        wIgnoreEmptyFile = new Button(wXmlConf, SWT.CHECK);
        props.setLook(wIgnoreEmptyFile);
        wIgnoreEmptyFile.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.IgnoreEmptyFile.Tooltip"));
        fdIgnoreEmptyFile = new FormData();
        fdIgnoreEmptyFile.left = new FormAttachment(middle, 0);
        fdIgnoreEmptyFile.top = new FormAttachment(wIgnoreComment, margin);
        wIgnoreEmptyFile.setLayoutData(fdIgnoreEmptyFile);

        // do not fail if no files?
        wldoNotFailIfNoFile = new Label(wXmlConf, SWT.RIGHT);
        wldoNotFailIfNoFile.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.doNotFailIfNoFile.Label"));
        props.setLook(wldoNotFailIfNoFile);
        fdldoNotFailIfNoFile = new FormData();
        fdldoNotFailIfNoFile.left = new FormAttachment(0, 0);
        fdldoNotFailIfNoFile.top = new FormAttachment(wIgnoreEmptyFile, margin);
        fdldoNotFailIfNoFile.right = new FormAttachment(middle, -margin);
        wldoNotFailIfNoFile.setLayoutData(fdldoNotFailIfNoFile);
        wdoNotFailIfNoFile = new Button(wXmlConf, SWT.CHECK);
        props.setLook(wdoNotFailIfNoFile);
        wdoNotFailIfNoFile.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.doNotFailIfNoFile.Tooltip"));
        fddoNotFailIfNoFile = new FormData();
        fddoNotFailIfNoFile.left = new FormAttachment(middle, 0);
        fddoNotFailIfNoFile.top = new FormAttachment(wIgnoreEmptyFile, margin);
        wdoNotFailIfNoFile.setLayoutData(fddoNotFailIfNoFile);

        wlLimit = new Label(wXmlConf, SWT.RIGHT);
        wlLimit.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.Limit.Label"));
        props.setLook(wlLimit);
        fdlLimit = new FormData();
        fdlLimit.left = new FormAttachment(0, 0);
        fdlLimit.top = new FormAttachment(wdoNotFailIfNoFile, margin);
        fdlLimit.right = new FormAttachment(middle, -margin);
        wlLimit.setLayoutData(fdlLimit);
        wLimit = new Text(wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLimit);
        wLimit.addModifyListener(lsMod);
        fdLimit = new FormData();
        fdLimit.left = new FormAttachment(middle, 0);
        fdLimit.top = new FormAttachment(wdoNotFailIfNoFile, margin);
        fdLimit.right = new FormAttachment(100, 0);
        wLimit.setLayoutData(fdLimit);

        // Prune path to handle large files (streaming mode)
        wlPrunePath = new Label(wXmlConf, SWT.RIGHT);
        wlPrunePath.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.StreamingMode.Label"));
        props.setLook(wlPrunePath);
        fdlPrunePath = new FormData();
        fdlPrunePath.left = new FormAttachment(0, 0);
        fdlPrunePath.top = new FormAttachment(wLimit, margin);
        fdlPrunePath.right = new FormAttachment(middle, -margin);
        wlPrunePath.setLayoutData(fdlPrunePath);
        wPrunePath = new TextVar(transMeta, wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wPrunePath.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.StreamingMode.Tooltip"));
        props.setLook(wPrunePath);
        wPrunePath.addModifyListener(lsMod);
        fdPrunePath = new FormData();
        fdPrunePath.left = new FormAttachment(middle, 0);
        fdPrunePath.top = new FormAttachment(wLimit, margin);
        fdPrunePath.right = new FormAttachment(100, 0);
        wPrunePath.setLayoutData(fdPrunePath);

        fdXmlConf = new FormData();
        fdXmlConf.left = new FormAttachment(0, margin);
        fdXmlConf.top = new FormAttachment(0, margin);
        fdXmlConf.right = new FormAttachment(100, -margin);
        wXmlConf.setLayoutData(fdXmlConf);

        // ///////////////////////////////////////////////////////////
        // / END OF XmlConf Field GROUP
        // ///////////////////////////////////////////////////////////

        // ///////////////////////////////
        // START OF Additional Fields GROUP //
        // ///////////////////////////////

        wAdditionalFields = new Group(wContentComp, SWT.SHADOW_NONE);
        props.setLook(wAdditionalFields);
        wAdditionalFields.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.wAdditionalFields.Label"));

        FormLayout AdditionalFieldsgroupLayout = new FormLayout();
        AdditionalFieldsgroupLayout.marginWidth = 10;
        AdditionalFieldsgroupLayout.marginHeight = 10;
        wAdditionalFields.setLayout(AdditionalFieldsgroupLayout);

        wlInclFilename = new Label(wAdditionalFields, SWT.RIGHT);
        wlInclFilename.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.InclFilename.Label"));
        props.setLook(wlInclFilename);
        fdlInclFilename = new FormData();
        fdlInclFilename.left = new FormAttachment(0, 0);
        fdlInclFilename.top = new FormAttachment(wXmlConf, 4 * margin);
        fdlInclFilename.right = new FormAttachment(middle, -margin);
        wlInclFilename.setLayoutData(fdlInclFilename);
        wInclFilename = new Button(wAdditionalFields, SWT.CHECK);
        props.setLook(wInclFilename);
        wInclFilename.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.InclFilename.Tooltip"));
        fdInclFilename = new FormData();
        fdInclFilename.left = new FormAttachment(middle, 0);
        fdInclFilename.top = new FormAttachment(wXmlConf, 4 * margin);
        wInclFilename.setLayoutData(fdInclFilename);

        wlInclFilenameField = new Label(wAdditionalFields, SWT.LEFT);
        wlInclFilenameField.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.InclFilenameField.Label"));
        props.setLook(wlInclFilenameField);
        fdlInclFilenameField = new FormData();
        fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
        fdlInclFilenameField.top = new FormAttachment(wLimit, 4 * margin);
        wlInclFilenameField.setLayoutData(fdlInclFilenameField);
        wInclFilenameField = new TextVar(transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wInclFilenameField);
        wInclFilenameField.addModifyListener(lsMod);
        fdInclFilenameField = new FormData();
        fdInclFilenameField.left = new FormAttachment(wlInclFilenameField, margin);
        fdInclFilenameField.top = new FormAttachment(wLimit, 4 * margin);
        fdInclFilenameField.right = new FormAttachment(100, 0);
        wInclFilenameField.setLayoutData(fdInclFilenameField);

        wlInclRownum = new Label(wAdditionalFields, SWT.RIGHT);
        wlInclRownum.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.InclRownum.Label"));
        props.setLook(wlInclRownum);
        fdlInclRownum = new FormData();
        fdlInclRownum.left = new FormAttachment(0, 0);
        fdlInclRownum.top = new FormAttachment(wInclFilenameField, margin);
        fdlInclRownum.right = new FormAttachment(middle, -margin);
        wlInclRownum.setLayoutData(fdlInclRownum);
        wInclRownum = new Button(wAdditionalFields, SWT.CHECK);
        props.setLook(wInclRownum);
        wInclRownum.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.InclRownum.Tooltip"));
        fdRownum = new FormData();
        fdRownum.left = new FormAttachment(middle, 0);
        fdRownum.top = new FormAttachment(wInclFilenameField, margin);
        wInclRownum.setLayoutData(fdRownum);

        wlInclRownumField = new Label(wAdditionalFields, SWT.RIGHT);
        wlInclRownumField.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.InclRownumField.Label"));
        props.setLook(wlInclRownumField);
        fdlInclRownumField = new FormData();
        fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
        fdlInclRownumField.top = new FormAttachment(wInclFilenameField, margin);
        wlInclRownumField.setLayoutData(fdlInclRownumField);
        wInclRownumField = new TextVar(transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wInclRownumField);
        wInclRownumField.addModifyListener(lsMod);
        fdInclRownumField = new FormData();
        fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
        fdInclRownumField.top = new FormAttachment(wInclFilenameField, margin);
        fdInclRownumField.right = new FormAttachment(100, 0);
        wInclRownumField.setLayoutData(fdInclRownumField);

        fdAdditionalFields = new FormData();
        fdAdditionalFields.left = new FormAttachment(0, margin);
        fdAdditionalFields.top = new FormAttachment(wXmlConf, margin);
        fdAdditionalFields.right = new FormAttachment(100, -margin);
        wAdditionalFields.setLayoutData(fdAdditionalFields);

        // ///////////////////////////////////////////////////////////
        // / END OF Additional Fields GROUP
        // ///////////////////////////////////////////////////////////

        // ///////////////////////////////
        // START OF AddFileResult GROUP //
        // ///////////////////////////////

        wAddFileResult = new Group(wContentComp, SWT.SHADOW_NONE);
        props.setLook(wAddFileResult);
        wAddFileResult.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.wAddFileResult.Label"));

        FormLayout AddFileResultgroupLayout = new FormLayout();
        AddFileResultgroupLayout.marginWidth = 10;
        AddFileResultgroupLayout.marginHeight = 10;
        wAddFileResult.setLayout(AddFileResultgroupLayout);

        wlAddResult = new Label(wAddFileResult, SWT.RIGHT);
        wlAddResult.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.AddResult.Label"));
        props.setLook(wlAddResult);
        fdlAddResult = new FormData();
        fdlAddResult.left = new FormAttachment(0, 0);
        fdlAddResult.top = new FormAttachment(wAdditionalFields, margin);
        fdlAddResult.right = new FormAttachment(middle, -margin);
        wlAddResult.setLayoutData(fdlAddResult);
        wAddResult = new Button(wAddFileResult, SWT.CHECK);
        props.setLook(wAddResult);
        wAddResult.setToolTipText(BaseMessages.getString(PKG, "GetHTMLDataDialog.AddResult.Tooltip"));
        fdAddResult = new FormData();
        fdAddResult.left = new FormAttachment(middle, 0);
        fdAddResult.top = new FormAttachment(wAdditionalFields, margin);
        wAddResult.setLayoutData(fdAddResult);

        fdAddFileResult = new FormData();
        fdAddFileResult.left = new FormAttachment(0, margin);
        fdAddFileResult.top = new FormAttachment(wAdditionalFields, margin);
        fdAddFileResult.right = new FormAttachment(100, -margin);
        wAddFileResult.setLayoutData(fdAddFileResult);

        // ///////////////////////////////////////////////////////////
        // / END OF AddFileResult GROUP
        // ///////////////////////////////////////////////////////////

        fdContentComp = new FormData();
        fdContentComp.left = new FormAttachment(0, 0);
        fdContentComp.top = new FormAttachment(0, 0);
        fdContentComp.right = new FormAttachment(100, 0);
        fdContentComp.bottom = new FormAttachment(100, 0);
        wContentComp.setLayoutData(fdContentComp);

        wContentComp.layout();
        wContentTab.setControl(wContentComp);

        // ///////////////////////////////////////////////////////////
        // / END OF CONTENT TAB
        // ///////////////////////////////////////////////////////////

        // Fields tab...
        //
        wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
        wFieldsTab.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.Fields.Tab"));

        FormLayout fieldsLayout = new FormLayout();
        fieldsLayout.marginWidth = Const.FORM_MARGIN;
        fieldsLayout.marginHeight = Const.FORM_MARGIN;

        wFieldsComp = new Composite(wTabFolder, SWT.NONE);
        wFieldsComp.setLayout(fieldsLayout);
        props.setLook(wFieldsComp);

        wGet = new Button(wFieldsComp, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.GetFields.Button"));
        fdGet = new FormData();
        fdGet.left = new FormAttachment(50, 0);
        fdGet.bottom = new FormAttachment(100, 0);
        wGet.setLayoutData(fdGet);

        final int FieldsRows = input.getInputFields().length;

        ColumnInfo[] colinf =
                new ColumnInfo[]{
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Name.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.XPath.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Element.Column"),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, GetHTMLDataField.ElementTypeDesc, true),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.ResultType.Column"),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, GetHTMLDataField.ResultTypeDesc, true),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Type.Column"),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes(), true),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Format.Column"),
                                ColumnInfo.COLUMN_TYPE_FORMAT, 4),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Length.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Precision.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Currency.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Decimal.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Group.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.TrimType.Column"),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, GetHTMLDataField.trimTypeDesc, true),
                        new ColumnInfo(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Repeat.Column"),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{BaseMessages.getString(PKG, "System.Combo.Yes"),
                                BaseMessages.getString(PKG, "System.Combo.No")}, true),

                };

        colinf[0].setUsingVariables(true);
        colinf[0].setToolTip(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.Name.Column.Tooltip"));
        colinf[1].setUsingVariables(true);
        colinf[1].setToolTip(BaseMessages.getString(PKG, "GetHTMLDataDialog.FieldsTable.XPath.Column.Tooltip"));

        wFields = new TableView(transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);

        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(wGet, -margin);
        wFields.setLayoutData(fdFields);

        fdFieldsComp = new FormData();
        fdFieldsComp.left = new FormAttachment(0, 0);
        fdFieldsComp.top = new FormAttachment(0, 0);
        fdFieldsComp.right = new FormAttachment(100, 0);
        fdFieldsComp.bottom = new FormAttachment(100, 0);
        wFieldsComp.setLayoutData(fdFieldsComp);

        wFieldsComp.layout();
        wFieldsTab.setControl(wFieldsComp);

        addAdditionalFieldsTab();

        fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(wStepname, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

        wPreview = new Button(shell, SWT.PUSH);
        wPreview.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.Button.PreviewRows"));

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[]{wOK, wPreview, wCancel}, margin, wTabFolder);

        // Add listeners
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        lsGet = new Listener() {
            public void handleEvent(Event e) {
                get();
            }
        };
        lsPreview = new Listener() {
            public void handleEvent(Event e) {
                preview();
            }
        };
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };

        wOK.addListener(SWT.Selection, lsOK);
        wGet.addListener(SWT.Selection, lsGet);
        wPreview.addListener(SWT.Selection, lsPreview);
        wCancel.addListener(SWT.Selection, lsCancel);

        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };

        wStepname.addSelectionListener(lsDef);
        wLimit.addSelectionListener(lsDef);
        wInclRownumField.addSelectionListener(lsDef);
        wInclFilenameField.addSelectionListener(lsDef);

        // Add the file to the list of files...
        SelectionAdapter selA = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                wFilenameList.add(new String[]{wFilename.getText(), wFilemask.getText(), wExcludeFilemask.getText(),
                        GetHTMLDataMeta.RequiredFilesCode[0], GetHTMLDataMeta.RequiredFilesCode[0]});
                wFilename.setText("");
                wFilemask.setText("");
                wExcludeFilemask.setText("");
                wFilenameList.removeEmptyRows();
                wFilenameList.setRowNums();
                wFilenameList.optWidth(true);
            }
        };
        wbaFilename.addSelectionListener(selA);
        wFilename.addSelectionListener(selA);

        // Delete files from the list of files...
        wbdFilename.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                int[] idx = wFilenameList.getSelectionIndices();
                wFilenameList.remove(idx);
                wFilenameList.removeEmptyRows();
                wFilenameList.setRowNums();
            }
        });

        // Edit the selected file & remove from the list...
        wbeFilename.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                int idx = wFilenameList.getSelectionIndex();
                if (idx >= 0) {
                    String[] string = wFilenameList.getItem(idx);
                    wFilename.setText(string[0]);
                    wFilemask.setText(string[1]);
                    wExcludeFilemask.setText(string[2]);
                    wFilenameList.remove(idx);
                }
                wFilenameList.removeEmptyRows();
                wFilenameList.setRowNums();
            }
        });

        // Show the files that are selected at this time...
        wbShowFiles.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    GetHTMLDataMeta tfii = new GetHTMLDataMeta();
                    getInfo(tfii);
                    FileInputList fileInputList = tfii.getFiles(transMeta);
                    String[] files = fileInputList.getFileStrings();
                    if (files != null && files.length > 0) {
                        EnterSelectionDialog esd =
                                new EnterSelectionDialog(shell, files, BaseMessages.getString(PKG,
                                        "GetHTMLDataDialog.FilesReadSelection.DialogTitle"), BaseMessages.getString(PKG,
                                        "GetHTMLDataDialog.FilesReadSelection.DialogMessage"));
                        esd.setViewOnly();
                        esd.open();
                    } else {
                        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                        mb.setMessage(BaseMessages.getString(PKG, "GetHTMLDataDialog.NoFileFound.DialogMessage"));
                        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                        mb.open();
                    }
                } catch (KettleException ex) {
                    new ErrorDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorParsingData.DialogTitle"),
                            BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorParsingData.DialogMessage"), ex);
                }
            }
        });
        // Enable/disable the right fields to allow a filename to be added to each row...
        wInclFilename.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setIncludeFilename();
            }
        });

        // Enable/disable the right fields to allow a row number to be added to each row...
        wInclRownum.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setIncludeRownum();
            }
        });

        // Whenever something changes, set the tooltip to the expanded version of the filename:
        wFilename.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                wFilename.setToolTipText(wFilename.getText());
            }
        });

        // Listen to the Browse... button
        wbbFilename.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // A mask: a directory!
                if (!Utils.isEmpty(wFilemask.getText()) || !Utils.isEmpty(wExcludeFilemask.getText())) {
                    DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
                    if (wFilename.getText() != null) {
                        String fpath = transMeta.environmentSubstitute(wFilename.getText());
                        dialog.setFilterPath(fpath);
                    }

                    if (dialog.open() != null) {
                        String str = dialog.getFilterPath();
                        wFilename.setText(str);
                    }
                } else {
                    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                    dialog.setFilterExtensions(new String[]{"*.xml;*.XML", "*"});
                    if (wFilename.getText() != null) {
                        String fname = transMeta.environmentSubstitute(wFilename.getText());
                        dialog.setFileName(fname);
                    }

                    dialog.setFilterNames(new String[]{BaseMessages.getString(PKG, "System.FileType.XMLFiles"),
                            BaseMessages.getString(PKG, "System.FileType.AllFiles")});

                    if (dialog.open() != null) {
                        String str = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();
                        wFilename.setText(str);
                    }
                }
            }
        });

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        wTabFolder.setSelection(0);

        // Set the shell size, based upon previous time...
        setSize();
        getData(input);
        ActiveXmlStreamField();
        setIncludeFilename();
        setIncludeRownum();
        input.setChanged(changed);
        wFields.optWidth(true);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return stepname;
    }

    private void setXMLStreamField() {
        try {

            wXMLField.removeAll();

            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r != null) {
                String[] fieldNames = r.getFieldNames();
                if (fieldNames != null) {

                    for (int i = 0; i < fieldNames.length; i++) {
                        wXMLField.add(fieldNames[i]);
                    }
                }
            }
        } catch (KettleException ke) {
            if (!Const.isOSX()) { // see PDI-8871 for details
                shell.setFocus();
            }
            wXMLField.add(EMPTY_FIELDS);
            wXMLField.setText(EMPTY_FIELDS);
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.FailedToGetFields.DialogTitle"),
                    BaseMessages.getString(PKG, "GetHTMLDataDialog.FailedToGetFields.DialogMessage"), ke);
        }
    }

    private void ActiveXmlStreamField() {
        wlXMLField.setEnabled(wXMLStreamField.getSelection());
        wXMLField.setEnabled(wXMLStreamField.getSelection());
        wlXMLIsAFile.setEnabled(wXMLStreamField.getSelection());
        wXMLIsAFile.setEnabled(wXMLStreamField.getSelection());
        wlreadUrl.setEnabled(wXMLStreamField.getSelection());
        wreadUrl.setEnabled(wXMLStreamField.getSelection());

        wlFilename.setEnabled(!wXMLStreamField.getSelection());
        wbbFilename.setEnabled(!wXMLStreamField.getSelection());
        wbaFilename.setEnabled(!wXMLStreamField.getSelection());
        wFilename.setEnabled(!wXMLStreamField.getSelection());
        wlExcludeFilemask.setEnabled(!wXMLStreamField.getSelection());
        wExcludeFilemask.setEnabled(!wXMLStreamField.getSelection());
        wlFilemask.setEnabled(!wXMLStreamField.getSelection());
        wFilemask.setEnabled(!wXMLStreamField.getSelection());
        wlFilenameList.setEnabled(!wXMLStreamField.getSelection());
        wbdFilename.setEnabled(!wXMLStreamField.getSelection());
        wbeFilename.setEnabled(!wXMLStreamField.getSelection());
        wbShowFiles.setEnabled(!wXMLStreamField.getSelection());
        wlFilenameList.setEnabled(!wXMLStreamField.getSelection());
        wFilenameList.setEnabled(!wXMLStreamField.getSelection());
        wInclFilename.setEnabled(!wXMLStreamField.getSelection());
        wlInclFilename.setEnabled(!wXMLStreamField.getSelection());

        if (wXMLStreamField.getSelection()) {
            wInclFilename.setSelection(false);
            wlInclFilenameField.setEnabled(false);
            wInclFilenameField.setEnabled(false);
        } else {
            wlInclFilenameField.setEnabled(wInclFilename.getSelection());
            wInclFilenameField.setEnabled(wInclFilename.getSelection());
        }

        if (wXMLStreamField.getSelection() && !wXMLIsAFile.getSelection()) {
            wEncoding.setEnabled(false);
            wlEncoding.setEnabled(false);
        } else {
            wEncoding.setEnabled(true);
            wlEncoding.setEnabled(true);
        }
        wAddResult.setEnabled(!wXMLStreamField.getSelection());
        wlAddResult.setEnabled(!wXMLStreamField.getSelection());
        wLimit.setEnabled(!wXMLStreamField.getSelection());
        wlLimit.setEnabled(!wXMLStreamField.getSelection());
        wPreview.setEnabled(!wXMLStreamField.getSelection());
        wPrunePath.setEnabled(!wXMLStreamField.getSelection());
        wlPrunePath.setEnabled(!wXMLStreamField.getSelection());
        wlShortFileFieldName.setEnabled(!wXMLStreamField.getSelection());
        wShortFileFieldName.setEnabled(!wXMLStreamField.getSelection());
        wlPathFieldName.setEnabled(!wXMLStreamField.getSelection());
        wPathFieldName.setEnabled(!wXMLStreamField.getSelection());
        wlIsHiddenName.setEnabled(!wXMLStreamField.getSelection());
        wIsHiddenName.setEnabled(!wXMLStreamField.getSelection());
        wlLastModificationTimeName.setEnabled(!wXMLStreamField.getSelection());
        wLastModificationTimeName.setEnabled(!wXMLStreamField.getSelection());
        wlUriName.setEnabled(!wXMLStreamField.getSelection());
        wUriName.setEnabled(!wXMLStreamField.getSelection());
        wlRootUriName.setEnabled(!wXMLStreamField.getSelection());
        wRootUriName.setEnabled(!wXMLStreamField.getSelection());
        wlExtensionFieldName.setEnabled(!wXMLStreamField.getSelection());
        wExtensionFieldName.setEnabled(!wXMLStreamField.getSelection());
        wlSizeFieldName.setEnabled(!wXMLStreamField.getSelection());
        wSizeFieldName.setEnabled(!wXMLStreamField.getSelection());
        if (wXMLStreamField.getSelection()) {
            wShortFileFieldName.setText("");
            wPathFieldName.setText("");
            wIsHiddenName.setText("");
            wLastModificationTimeName.setText("");
            wUriName.setText("");
            wRootUriName.setText("");
            wExtensionFieldName.setText("");
            wSizeFieldName.setText("");

        }

    }

    private void getLoopPathList() {
        try {
            GetHTMLDataMeta meta = new GetHTMLDataMeta();
            getInfo(meta);
            if (meta.isInFields()) {
                if (meta.isReadUrl()) {
                    // Read URL
                    String url = XMLSource;
                    if (url == null) {
                        EnterStringDialog d =
                                new EnterStringDialog(shell, "", BaseMessages.getString(PKG, "GetHTMLDataDialog.AskURL.Title"),
                                        BaseMessages.getString(PKG, "GetHTMLDataDialog.AskURL.Message"));
                        url = d.open();
                    }
                    populateLoopPaths(meta, url, true, true);

                } else if (meta.getIsAFile()) {
                    // Read file
                    String str = XMLSource;
                    if (str == null) {
                        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                        dialog.setFilterExtensions(new String[]{"*.xml;*.XML", "*"});
                        dialog.setFilterNames(new String[]{BaseMessages.getString(PKG, "System.FileType.XMLFiles"),
                                BaseMessages.getString(PKG, "System.FileType.AllFiles")});

                        if (dialog.open() != null) {
                            str = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();
                        }
                        populateLoopPaths(meta, str, false, false);
                    }
                } else {
                    // Read xml
                    String xml = XMLSource;
                    if (xml == null) {
                        EnterTextDialog d =
                                new EnterTextDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.AskXML.Title"),
                                        BaseMessages.getString(PKG, "GetHTMLDataDialog.AskXML.Message"), null);
                        xml = d.open();
                    }
                    populateLoopPaths(meta, xml, true, false);
                }
            } else {

                FileInputList fileinputList = meta.getFiles(transMeta);

                if (fileinputList.nrOfFiles() > 0) {
                    // Check the first file

                    if (fileinputList.getFile(0).exists()) {
                        populateLoopPaths(meta, KettleVFS.getFilename(fileinputList.getFile(0)), false, false);
                    } else {
                        // The file not exists !
                        throw new KettleException(BaseMessages.getString(PKG, "GetHTMLDataDialog.Exception.FileDoesNotExist",
                                KettleVFS.getFilename(fileinputList.getFile(0))));
                    }
                } else {
                    // No file specified
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                    mb.setMessage(BaseMessages.getString(PKG, "GetHTMLDataDialog.FilesMissing.DialogMessage"));
                    mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                    mb.open();
                }
            }
        } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.UnableToGetListOfPaths.Title"),
                    BaseMessages.getString(PKG, "GetHTMLDataDialog.UnableToGetListOfPaths.Message"), e);
        }
    }

    private void get() {
        InputStream is = null;
        try {
            GetHTMLDataMeta meta = new GetHTMLDataMeta();
            getInfo(meta);

            // check if the path is given
            if (!checkLoopXPath(meta)) {
                return;
            }
            int clearFields = SWT.YES;
            if (wFields.nrNonEmpty() > 0) {
                MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
                messageBox.setMessage(BaseMessages.getString(PKG, "GetHTMLDataDialog.ClearFieldList.DialogMessage"));
                messageBox.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.ClearFieldList.DialogTitle"));
                clearFields = messageBox.open();
                if (clearFields == SWT.CANCEL) {
                    return;
                }
            }

            if (meta.isInFields()) {
                if (meta.isReadUrl()) {
                    // Read URL
                    String url = XMLSource;
                    if (url == null) {
                        EnterStringDialog enterStringDialog =
                                new EnterStringDialog(shell, "", BaseMessages.getString(PKG, "GetHTMLDataDialog.AskURL.Title"),
                                        BaseMessages.getString(PKG, "GetHTMLDataDialog.AskURL.Title"));
                        url = enterStringDialog.open();
                    }
                    populateFields(meta, url, true, true, clearFields);

                } else if (meta.getIsAFile()) {
                    // Read file
                    String str = XMLSource;
                    if (str == null) {
                        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                        dialog.setFilterExtensions(new String[]{"*.xml;*.XML", "*"});
                        dialog.setFilterNames(new String[]{BaseMessages.getString(PKG, "System.FileType.XMLFiles"),
                                BaseMessages.getString(PKG, "System.FileType.AllFiles")});

                        if (dialog.open() != null) {
                            str = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();
                        }
                    }
                    populateFields(meta, str, false, false, clearFields);
                } else {
                    // Read xml
                    String xml = XMLSource;
                    if (xml == null) {
                        EnterTextDialog d =
                                new EnterTextDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.AskXML.Title"),
                                        BaseMessages.getString(PKG, "GetHTMLDataDialog.AskXML.Message"), null);
                        xml = d.open();
                    }
                    populateFields(meta, xml, true, false, clearFields);
                }
            } else {

                FileInputList inputList = meta.getFiles(transMeta);

                if (inputList.getFiles().size() > 0) {
                    populateFields(meta, KettleVFS.getFilename(inputList.getFile(0)), false, false, clearFields);
                }
            }
        } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorParsingData.DialogTitle"),
                    BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorParsingData.DialogMessage"), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) { /* Ignore */
            }
        }
    }

    private void setEncodings() {
        // Encoding of the text file:
        if (!gotEncodings) {
            gotEncodings = true;

            wEncoding.removeAll();
            ArrayList<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (int i = 0; i < values.size(); i++) {
                Charset charSet = values.get(i);
                wEncoding.add(charSet.displayName());
            }

            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems());
            if (idx >= 0) {
                wEncoding.select(idx);
            }
        }
    }

    public void setIncludeFilename() {
        wlInclFilenameField.setEnabled(wInclFilename.getSelection());
        wInclFilenameField.setEnabled(wInclFilename.getSelection());
    }

    public void setIncludeRownum() {
        wlInclRownumField.setEnabled(wInclRownum.getSelection());
        wInclRownumField.setEnabled(wInclRownum.getSelection());
    }

    /**
     * Read the data from the TextFileInputMeta object and show it in this dialog.
     *
     * @param in The TextFileInputMeta object to obtain the data from.
     */
    public void getData(GetHTMLDataMeta in) {
        if (in.getFileName() != null) {
            wFilenameList.removeAll();

            for (int i = 0; i < in.getFileName().length; i++) {
                wFilenameList
                        .add(new String[]{in.getFileName()[i], in.getFileMask()[i], in.getExludeFileMask()[i],
                                in.getRequiredFilesDesc(in.getFileRequired()[i]),
                                in.getRequiredFilesDesc(in.getIncludeSubFolders()[i])});
            }

            wFilenameList.removeEmptyRows();
            wFilenameList.setRowNums();
            wFilenameList.optWidth(true);
        }
        wInclFilename.setSelection(in.includeFilename());
        wInclRownum.setSelection(in.includeRowNumber());
        wAddResult.setSelection(in.addResultFile());
        wreadUrl.setSelection(in.isReadUrl());
        wIgnoreComment.setSelection(in.isIgnoreComments());
        wIgnoreEmptyFile.setSelection(in.isIgnoreEmptyFile());
        wdoNotFailIfNoFile.setSelection(in.isdoNotFailIfNoFile());
        wXMLStreamField.setSelection(in.isInFields());
        wXMLIsAFile.setSelection(in.getIsAFile());

        if (in.getXMLField() != null) {
            wXMLField.setText(in.getXMLField());
        }

        if (in.getFilenameField() != null) {
            wInclFilenameField.setText(in.getFilenameField());
        }
        if (in.getRowNumberField() != null) {
            wInclRownumField.setText(in.getRowNumberField());
        }
        wLimit.setText("" + in.getRowLimit());
        if (in.getPrunePath() != null) {
            wPrunePath.setText(in.getPrunePath());
        }
        if (in.getLoopXPath() != null) {
            wLoopXPath.setText(in.getLoopXPath());
        }
        if (in.getEncoding() != null) {
            wEncoding.setText("" + in.getEncoding());
        } else {
            wEncoding.setText("UTF-8");
        }

        logDebug(BaseMessages.getString(PKG, "GetHTMLDataDialog.Log.GettingFieldsInfo"));
        for (int i = 0; i < in.getInputFields().length; i++) {
            GetHTMLDataField field = in.getInputFields()[i];

            if (field != null) {
                TableItem item = wFields.table.getItem(i);
                String name = field.getName();
                String xpath = field.getXPath();
                String element = field.getElementTypeDesc();
                String resulttype = field.getResultTypeDesc();
                String type = field.getTypeDesc();
                String format = field.getFormat();
                String length = "" + field.getLength();
                String prec = "" + field.getPrecision();
                String curr = field.getCurrencySymbol();
                String group = field.getGroupSymbol();
                String decim = field.getDecimalSymbol();
                String trim = field.getTrimTypeDesc();
                String rep =
                        field.isRepeated() ? BaseMessages.getString(PKG, "System.Combo.Yes") : BaseMessages.getString(PKG,
                                "System.Combo.No");

                if (name != null) {
                    item.setText(1, name);
                }
                if (xpath != null) {
                    item.setText(2, xpath);
                }
                if (element != null) {
                    item.setText(3, element);
                }
                if (resulttype != null) {
                    item.setText(4, resulttype);
                }
                if (type != null) {
                    item.setText(5, type);
                }
                if (format != null) {
                    item.setText(6, format);
                }
                if (length != null && !"-1".equals(length)) {
                    item.setText(7, length);
                }
                if (prec != null && !"-1".equals(prec)) {
                    item.setText(8, prec);
                }
                if (curr != null) {
                    item.setText(9, curr);
                }
                if (decim != null) {
                    item.setText(10, decim);
                }
                if (group != null) {
                    item.setText(11, group);
                }
                if (trim != null) {
                    item.setText(12, trim);
                }
                if (rep != null) {
                    item.setText(13, rep);
                }

            }
        }

        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

        if (in.getShortFileNameField() != null) {
            wShortFileFieldName.setText(in.getShortFileNameField());
        }
        if (in.getPathField() != null) {
            wPathFieldName.setText(in.getPathField());
        }
        if (in.isHiddenField() != null) {
            wIsHiddenName.setText(in.isHiddenField());
        }
        if (in.getLastModificationDateField() != null) {
            wLastModificationTimeName.setText(in.getLastModificationDateField());
        }
        if (in.getUriField() != null) {
            wUriName.setText(in.getUriField());
        }
        if (in.getRootUriField() != null) {
            wRootUriName.setText(in.getRootUriField());
        }
        if (in.getExtensionField() != null) {
            wExtensionFieldName.setText(in.getExtensionField());
        }
        if (in.getSizeField() != null) {
            wSizeFieldName.setText(in.getSizeField());
        }

        wStepname.selectAll();
        wStepname.setFocus();
    }

    private void cancel() {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    public void dispose() {
        XMLSource = null;
        super.dispose();
    }

    private void ok() {
        try {
            getInfo(input);
        } catch (KettleException e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorParsingData.DialogTitle"),
                    BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorParsingData.DialogMessage"), e);
        }
        dispose();
    }

    private void getInfo(GetHTMLDataMeta in) throws KettleException {
        stepname = wStepname.getText(); // return value

        // copy info to TextFileInputMeta class (input)
        in.setRowLimit(Const.toLong(wLimit.getText(), 0L));
        in.setPrunePath(wPrunePath.getText());
        in.setLoopXPath(wLoopXPath.getText());
        in.setEncoding(wEncoding.getText());
        in.setFilenameField(wInclFilenameField.getText());
        in.setRowNumberField(wInclRownumField.getText());
        in.setAddResultFile(wAddResult.getSelection());
        in.setIncludeFilename(wInclFilename.getSelection());
        in.setIncludeRowNumber(wInclRownum.getSelection());
        in.setReadUrl(wreadUrl.getSelection());
        in.setIgnoreComments(wIgnoreComment.getSelection());
        in.setIgnoreEmptyFile(wIgnoreEmptyFile.getSelection());
        in.setdoNotFailIfNoFile(wdoNotFailIfNoFile.getSelection());

        in.setInFields(wXMLStreamField.getSelection());
        in.setIsAFile(wXMLIsAFile.getSelection());
        in.setXMLField(wXMLField.getText());

        int nrFiles = wFilenameList.getItemCount();
        int nrFields = wFields.nrNonEmpty();

        in.allocate(nrFiles, nrFields);
        in.setFileName(wFilenameList.getItems(0));
        in.setFileMask(wFilenameList.getItems(1));
        in.setExcludeFileMask(wFilenameList.getItems(2));
        in.setFileRequired(wFilenameList.getItems(3));
        in.setIncludeSubFolders(wFilenameList.getItems(4));

        for (int i = 0; i < nrFields; i++) {
            GetHTMLDataField field = new GetHTMLDataField();

            TableItem item = wFields.getNonEmpty(i);

            field.setName(item.getText(1));
            field.setXPath(item.getText(2));
            field.setElementType(GetHTMLDataField.getElementTypeByDesc(item.getText(3)));
            field.setResultType(GetHTMLDataField.getResultTypeByDesc(item.getText(4)));
            field.setType(ValueMeta.getType(item.getText(5)));
            field.setFormat(item.getText(6));
            field.setLength(Const.toInt(item.getText(7), -1));
            field.setPrecision(Const.toInt(item.getText(8), -1));
            field.setCurrencySymbol(item.getText(9));
            field.setDecimalSymbol(item.getText(10));
            field.setGroupSymbol(item.getText(11));
            field.setTrimType(GetHTMLDataField.getTrimTypeByDesc(item.getText(12)));
            field.setRepeated(BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(item.getText(13)));

            // CHECKSTYLE:Indentation:OFF
            in.getInputFields()[i] = field;
        }
        in.setShortFileNameField(wShortFileFieldName.getText());
        in.setPathField(wPathFieldName.getText());
        in.setIsHiddenField(wIsHiddenName.getText());
        in.setLastModificationDateField(wLastModificationTimeName.getText());
        in.setUriField(wUriName.getText());
        in.setRootUriField(wRootUriName.getText());
        in.setExtensionField(wExtensionFieldName.getText());
        in.setSizeField(wSizeFieldName.getText());
    }

    // check if the loop xpath is given
    private boolean checkLoopXPath(GetHTMLDataMeta meta) {
        if (meta.getLoopXPath() == null || meta.getLoopXPath().length() < 1) {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(BaseMessages.getString(PKG, "GetHTMLDataDialog.SpecifyRepeatingElement.DialogMessage"));
            mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
            mb.open();
            return false;
        } else {
            return true;
        }
    }

    // Preview the data
    private void preview() {
        try {
            // Create the XML input step
            GetHTMLDataMeta oneMeta = new GetHTMLDataMeta();
            getInfo(oneMeta);

            // check if the path is given
            if (!checkLoopXPath(oneMeta)) {
                return;
            }
            TransMeta previewMeta =
                    TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());

            EnterNumberDialog numberDialog =
                    new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG,
                            "GetHTMLDataDialog.NumberRows.DialogTitle"), BaseMessages.getString(PKG,
                            "GetHTMLDataDialog.NumberRows.DialogMessage"));

            int previewSize = numberDialog.open();
            if (previewSize > 0) {
                TransPreviewProgressDialog progressDialog =
                        new TransPreviewProgressDialog(shell, previewMeta, new String[]{wStepname.getText()},
                                new int[]{previewSize});
                progressDialog.open();

                if (!progressDialog.isCancelled()) {
                    Trans trans = progressDialog.getTrans();
                    String loggingText = progressDialog.getLoggingText();

                    if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
                        EnterTextDialog etd =
                                new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),
                                        BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true);
                        etd.setReadOnly();
                        etd.open();
                    }
                    PreviewRowsDialog prd =
                            new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog
                                    .getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()),
                                    loggingText);
                    prd.open();
                }
            }
        } catch (KettleException e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorPreviewingData.DialogTitle"),
                    BaseMessages.getString(PKG, "GetHTMLDataDialog.ErrorPreviewingData.DialogMessage"), e);
        }
    }

    private void addAdditionalFieldsTab() {
        // ////////////////////////
        // START OF ADDITIONAL FIELDS TAB ///
        // ////////////////////////
        wAdditionalFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
        wAdditionalFieldsTab.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.AdditionalFieldsTab.TabTitle"));

        wAdditionalFieldsComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wAdditionalFieldsComp);

        FormLayout fieldsLayout = new FormLayout();
        fieldsLayout.marginWidth = 3;
        fieldsLayout.marginHeight = 3;
        wAdditionalFieldsComp.setLayout(fieldsLayout);
        // ShortFileFieldName line
        wlShortFileFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlShortFileFieldName.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.ShortFileFieldName.Label"));
        props.setLook(wlShortFileFieldName);
        fdlShortFileFieldName = new FormData();
        fdlShortFileFieldName.left = new FormAttachment(0, 0);
        fdlShortFileFieldName.top = new FormAttachment(wInclRownumField, margin);
        fdlShortFileFieldName.right = new FormAttachment(middle, -margin);
        wlShortFileFieldName.setLayoutData(fdlShortFileFieldName);

        wShortFileFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wShortFileFieldName);
        wShortFileFieldName.addModifyListener(lsMod);
        fdShortFileFieldName = new FormData();
        fdShortFileFieldName.left = new FormAttachment(middle, 0);
        fdShortFileFieldName.right = new FormAttachment(100, -margin);
        fdShortFileFieldName.top = new FormAttachment(wInclRownumField, margin);
        wShortFileFieldName.setLayoutData(fdShortFileFieldName);

        // ExtensionFieldName line
        wlExtensionFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlExtensionFieldName.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.ExtensionFieldName.Label"));
        props.setLook(wlExtensionFieldName);
        fdlExtensionFieldName = new FormData();
        fdlExtensionFieldName.left = new FormAttachment(0, 0);
        fdlExtensionFieldName.top = new FormAttachment(wShortFileFieldName, margin);
        fdlExtensionFieldName.right = new FormAttachment(middle, -margin);
        wlExtensionFieldName.setLayoutData(fdlExtensionFieldName);

        wExtensionFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wExtensionFieldName);
        wExtensionFieldName.addModifyListener(lsMod);
        fdExtensionFieldName = new FormData();
        fdExtensionFieldName.left = new FormAttachment(middle, 0);
        fdExtensionFieldName.right = new FormAttachment(100, -margin);
        fdExtensionFieldName.top = new FormAttachment(wShortFileFieldName, margin);
        wExtensionFieldName.setLayoutData(fdExtensionFieldName);

        // PathFieldName line
        wlPathFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlPathFieldName.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.PathFieldName.Label"));
        props.setLook(wlPathFieldName);
        fdlPathFieldName = new FormData();
        fdlPathFieldName.left = new FormAttachment(0, 0);
        fdlPathFieldName.top = new FormAttachment(wExtensionFieldName, margin);
        fdlPathFieldName.right = new FormAttachment(middle, -margin);
        wlPathFieldName.setLayoutData(fdlPathFieldName);

        wPathFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPathFieldName);
        wPathFieldName.addModifyListener(lsMod);
        fdPathFieldName = new FormData();
        fdPathFieldName.left = new FormAttachment(middle, 0);
        fdPathFieldName.right = new FormAttachment(100, -margin);
        fdPathFieldName.top = new FormAttachment(wExtensionFieldName, margin);
        wPathFieldName.setLayoutData(fdPathFieldName);

        // SizeFieldName line
        wlSizeFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlSizeFieldName.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.SizeFieldName.Label"));
        props.setLook(wlSizeFieldName);
        fdlSizeFieldName = new FormData();
        fdlSizeFieldName.left = new FormAttachment(0, 0);
        fdlSizeFieldName.top = new FormAttachment(wPathFieldName, margin);
        fdlSizeFieldName.right = new FormAttachment(middle, -margin);
        wlSizeFieldName.setLayoutData(fdlSizeFieldName);

        wSizeFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSizeFieldName);
        wSizeFieldName.addModifyListener(lsMod);
        fdSizeFieldName = new FormData();
        fdSizeFieldName.left = new FormAttachment(middle, 0);
        fdSizeFieldName.right = new FormAttachment(100, -margin);
        fdSizeFieldName.top = new FormAttachment(wPathFieldName, margin);
        wSizeFieldName.setLayoutData(fdSizeFieldName);

        // IsHiddenName line
        wlIsHiddenName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlIsHiddenName.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.IsHiddenName.Label"));
        props.setLook(wlIsHiddenName);
        fdlIsHiddenName = new FormData();
        fdlIsHiddenName.left = new FormAttachment(0, 0);
        fdlIsHiddenName.top = new FormAttachment(wSizeFieldName, margin);
        fdlIsHiddenName.right = new FormAttachment(middle, -margin);
        wlIsHiddenName.setLayoutData(fdlIsHiddenName);

        wIsHiddenName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wIsHiddenName);
        wIsHiddenName.addModifyListener(lsMod);
        fdIsHiddenName = new FormData();
        fdIsHiddenName.left = new FormAttachment(middle, 0);
        fdIsHiddenName.right = new FormAttachment(100, -margin);
        fdIsHiddenName.top = new FormAttachment(wSizeFieldName, margin);
        wIsHiddenName.setLayoutData(fdIsHiddenName);

        // LastModificationTimeName line
        wlLastModificationTimeName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlLastModificationTimeName
                .setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.LastModificationTimeName.Label"));
        props.setLook(wlLastModificationTimeName);
        fdlLastModificationTimeName = new FormData();
        fdlLastModificationTimeName.left = new FormAttachment(0, 0);
        fdlLastModificationTimeName.top = new FormAttachment(wIsHiddenName, margin);
        fdlLastModificationTimeName.right = new FormAttachment(middle, -margin);
        wlLastModificationTimeName.setLayoutData(fdlLastModificationTimeName);

        wLastModificationTimeName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLastModificationTimeName);
        wLastModificationTimeName.addModifyListener(lsMod);
        fdLastModificationTimeName = new FormData();
        fdLastModificationTimeName.left = new FormAttachment(middle, 0);
        fdLastModificationTimeName.right = new FormAttachment(100, -margin);
        fdLastModificationTimeName.top = new FormAttachment(wIsHiddenName, margin);
        wLastModificationTimeName.setLayoutData(fdLastModificationTimeName);

        // UriName line
        wlUriName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlUriName.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.UriName.Label"));
        props.setLook(wlUriName);
        fdlUriName = new FormData();
        fdlUriName.left = new FormAttachment(0, 0);
        fdlUriName.top = new FormAttachment(wLastModificationTimeName, margin);
        fdlUriName.right = new FormAttachment(middle, -margin);
        wlUriName.setLayoutData(fdlUriName);

        wUriName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUriName);
        wUriName.addModifyListener(lsMod);
        fdUriName = new FormData();
        fdUriName.left = new FormAttachment(middle, 0);
        fdUriName.right = new FormAttachment(100, -margin);
        fdUriName.top = new FormAttachment(wLastModificationTimeName, margin);
        wUriName.setLayoutData(fdUriName);

        // RootUriName line
        wlRootUriName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
        wlRootUriName.setText(BaseMessages.getString(PKG, "GetHTMLDataDialog.RootUriName.Label"));
        props.setLook(wlRootUriName);
        fdlRootUriName = new FormData();
        fdlRootUriName.left = new FormAttachment(0, 0);
        fdlRootUriName.top = new FormAttachment(wUriName, margin);
        fdlRootUriName.right = new FormAttachment(middle, -margin);
        wlRootUriName.setLayoutData(fdlRootUriName);

        wRootUriName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wRootUriName);
        wRootUriName.addModifyListener(lsMod);
        fdRootUriName = new FormData();
        fdRootUriName.left = new FormAttachment(middle, 0);
        fdRootUriName.right = new FormAttachment(100, -margin);
        fdRootUriName.top = new FormAttachment(wUriName, margin);
        wRootUriName.setLayoutData(fdRootUriName);

        fdAdditionalFieldsComp = new FormData();
        fdAdditionalFieldsComp.left = new FormAttachment(0, 0);
        fdAdditionalFieldsComp.top = new FormAttachment(wStepname, margin);
        fdAdditionalFieldsComp.right = new FormAttachment(100, 0);
        fdAdditionalFieldsComp.bottom = new FormAttachment(100, 0);
        wAdditionalFieldsComp.setLayoutData(fdAdditionalFieldsComp);

        wAdditionalFieldsComp.layout();
        wAdditionalFieldsTab.setControl(wAdditionalFieldsComp);

        // ///////////////////////////////////////////////////////////
        // / END OF ADDITIONAL FIELDS TAB
        // ///////////////////////////////////////////////////////////

    }

    private void populateLoopPaths(GetHTMLDataMeta meta, String XMLSource, boolean dynamicXMLSource, boolean useURL) {
        if (Utils.isEmpty(XMLSource)) {
            return;
        }
        String[] list_xpath = null;
        LoopNodesImportProgressDialog pd = null;
        if (dynamicXMLSource) {
            pd = new LoopNodesImportProgressDialog(shell, meta, XMLSource, useURL);
        } else {
            pd =
                    new LoopNodesImportProgressDialog(shell, meta, XMLSource, meta.getEncoding() == null ? "UTF-8" : meta
                            .getEncoding());
        }
        if (pd != null) {
            list_xpath = pd.open();
            if (list_xpath != null) {
                EnterSelectionDialog s =
                        new EnterSelectionDialog(shell, list_xpath, BaseMessages.getString(PKG,
                                "GetHTMLDataDialog.Dialog.SelectALoopPath.Title"), BaseMessages.getString(PKG,
                                "GetHTMLDataDialog.Dialog.SelectALoopPath.Message"));
                String listxpaths = s.open();
                if (listxpaths != null) {
                    wLoopXPath.setText(listxpaths);
                }
            }
        }
        this.XMLSource = XMLSource;
    }

    private void populateFields(GetHTMLDataMeta meta, String XMLSource, boolean dynamicXMLSource, boolean useURL,
                                int clearFields) throws KettleException {
        if (Utils.isEmpty(XMLSource)) {
            return;
        }

        HTMLInputFieldsImportProgressDialog prd = null;
        RowMetaAndData[] fields = null;

        if (dynamicXMLSource) {
            prd =
                    new HTMLInputFieldsImportProgressDialog(shell, meta, XMLSource, useURL, transMeta.environmentSubstitute(meta
                            .getLoopXPath()));
        } else {
            prd =
                    new HTMLInputFieldsImportProgressDialog(shell, meta, XMLSource, meta.getEncoding() == null ? "UTF-8" : meta
                            .getEncoding(), transMeta.environmentSubstitute(meta.getLoopXPath()));
        }
        if (prd != null) {
            fields = prd.open();
            if (fields != null) {
                if (clearFields == SWT.YES) {
                    wFields.clearAll(false);
                }
                int nr = fields.length;
                for (int i = 0; i < nr; i++) {
                    RowMetaAndData row = fields[i];

                    TableItem item = new TableItem(wFields.table, SWT.NONE);
                    item.setText(1, row.getString(0, ""));
                    item.setText(2, row.getString(1, GetHTMLDataField.ElementTypeDesc[0]));
                    item.setText(3, row.getString(2, ""));
                    item.setText(4, row.getString(3, ""));
                    item.setText(5, row.getString(4, ""));
                }
                wFields.removeEmptyRows();
                wFields.setRowNums();
                wFields.optWidth(true);
            }
        }
    }
}
