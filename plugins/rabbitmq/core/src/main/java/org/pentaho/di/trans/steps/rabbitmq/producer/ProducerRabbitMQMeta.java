package org.pentaho.di.trans.steps.rabbitmq.producer;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

/**
 * Store run-time data on the getHTMLData step.
 *
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
@Step(id = "rabbitMQProducerOutput", image = "RabbitMQProducerOutput.svg", i18nPackageName = "org.pentaho.di.trans.steps.rabbitmq.producer",
        name = "RabbitMQProducerOutput.name", description = "RabbitMQProducerOutput.description", categoryDescription = "RabbitMQProducerOutput.category",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/RabbitMQ+Producer+Output")
public class ProducerRabbitMQMeta extends BaseStepMeta implements StepMetaInterface {

    @Override
    public void setDefault() {

    }

    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return null;
    }

    @Override
    public StepDataInterface getStepData() {
        return null;
    }

}
