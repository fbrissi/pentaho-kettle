package org.pentaho.di.ui.trans.steps.rabbitmq.producer;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
public class ProducerRabbitMQDialog extends BaseStepDialog implements StepDialogInterface {

    public ProducerRabbitMQDialog(Shell parent, BaseStepMeta baseStepMeta, TransMeta transMeta, String stepname) {
        super(parent, baseStepMeta, transMeta, stepname);
    }

    @Override
    public String open() {
        return null;
    }

}
