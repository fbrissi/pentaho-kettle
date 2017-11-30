package org.pentaho.di.trans.steps.rabbitmq.producer;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.BaseStepMetaInjection;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

import java.util.List;

/**
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
public class ProducerRabbitMQMetaInjection extends BaseStepMetaInjection implements StepMetaInjectionInterface {

    @Override
    public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
        return null;
    }

    @Override
    public void injectStepMetadataEntries(List<StepInjectionMetaEntry> metadata) throws KettleException {

    }

    @Override
    public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
        return null;
    }

}
