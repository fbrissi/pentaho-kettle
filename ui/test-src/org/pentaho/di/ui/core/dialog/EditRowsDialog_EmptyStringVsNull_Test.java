/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.widgets.TableItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.test.util.FieldAccessor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class EditRowsDialog_EmptyStringVsNull_Test {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  @Test
  public void emptyAndNullsAreNotDifferent() throws Exception {
    doTestEmptyStringVsNull( false, "", null, null );
  }


  @Test
  public void emptyAndNullsAreDifferent() throws Exception {
    doTestEmptyStringVsNull( true, "", "", null );
  }


  private void doTestEmptyStringVsNull( boolean diffProperty, String... expected ) throws Exception {
    FieldAccessor.ensureEmptyStringIsNotNull( diffProperty );
    try {
      executeAndAssertResults( expected );
    } finally {
      FieldAccessor.resetEmptyStringIsNotNull();
    }
  }

  private void executeAndAssertResults( String[] expected ) throws Exception {
    EditRowsDialog dialog = mock( EditRowsDialog.class );

    when( dialog.getRowForData( any( TableItem.class ), anyInt() ) ).thenCallRealMethod();
    doCallRealMethod().when( dialog ).setRowMeta( any( RowMetaInterface.class ) );
    doCallRealMethod().when( dialog ).setStringRowMeta( any( RowMetaInterface.class ) );

    when( dialog.isDisplayingNullValue( any( TableItem.class ), anyInt() ) ).thenReturn( false );

    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "s1" ) );
    meta.addValueMeta( new ValueMetaString( "s2" ) );
    meta.addValueMeta( new ValueMetaString( "s3" ) );
    dialog.setRowMeta( meta );
    dialog.setStringRowMeta( meta );

    TableItem item = mock( TableItem.class );
    when( item.getText( 1 ) ).thenReturn( " " );
    when( item.getText( 2 ) ).thenReturn( "" );
    when( item.getText( 3 ) ).thenReturn( null );

    Object[] data = dialog.getRowForData( item, 0 );
    TransTestingUtil.assertResult( expected, data );
  }
}
