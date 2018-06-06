/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.processingTypeInputForm
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

class ProcessingTypeInputFormController {

  constructor(gettextCatalog) {
    'ngInject';
    Object.assign(this,
                  {
                    gettextCatalog,
                    processingTypes: [],
                    haveProcessingTypes: false
                  });
  }

  $onInit() {
    switch (this.buttonConfig) {
    case '2-button':
      this.haveBackButton = false;
      this.submitButtonLabel = this.gettextCatalog.getString('Submit');
      this.submitButtonIcon = 'glyphicon-check';
      break;

    case '3-button':
      this.haveBackButton = true;
      this.submitButtonLabel = this.gettextCatalog.getString('Next step');
      this.submitButtonIcon = 'glyphicon-chevron-right';
      break;

    default:
      throw new Error('invalid value for button config: ' + this.buttonConfig);
    }

    this.inputType = undefined;
    const input = this.processingType.specimenProcessing.input;
    if (input.definitionType !== undefined) {
      this.inputTypeIsCollected = input.isCollected();
    }

    if (this.inputTypeIsCollected) {
      this.validInputType = true;
      this.getCollectionSpecimenDefinitions()()
        .then(reply => {
          this.eventTypes = reply;
          this.eventType = _.find(this.eventTypes, { id: input.entityId });
          if (this.eventType !== undefined) {
            this.specimenDefinition = _.find(this.eventType.specimenDefinitionNames,
                                             { id: input.specimenDefinitionId });
          }
          this.commonInit();
        })
    }

    this.getProcessedSpecimenDefinitions()()
      .then(reply => {
        this.processingTypes = this.combinedNames(reply);
        this.haveProcessingTypes = (reply.length > 0);

        if (this.haveProcessingTypes) {
          if (this.inputTypeIsCollected === false) {
            this.inputProcessingType = _.find(this.processingTypes, { id: input.entityId });
            this.validInputType = true;
          }
          this.commonInit();
        } else {
          this.inputTypeIsCollected = true;
          this.collectedSpecimen();
        }
      })
  }

  collectedSpecimen() {
    this.eventType = undefined;
    this.specimenDefinition = undefined;
    this.getCollectionSpecimenDefinitions()()
      .then(reply => {
        this.eventTypes = reply;
        this.eventType = (reply.length === 1) ? reply[0].slug : undefined;
        this.commonInit();
        this.validInputType = true;
      });
  }

  processedSpecimen() {
    this.eventType = undefined;
    this.inputProcessingType = undefined;
    this.getProcessedSpecimenDefinitions()()
      .then(reply => {
        this.processingTypes = this.combinedNames(reply);
        if (reply.length === 1) {
          this.inputProcessingType = reply[0];
        }
        this.commonInit();
        this.validInputType = (reply.length > 0);
      })
  }

  commonInit() {
    const input = this.processingType.specimenProcessing.input;
    this.expectedChange  = input.expectedChange;
    this.count           = input.count;
    this.containerTypeId = input.containerTypeId;
  }

  combinedNames(processingTypes) {
    return processingTypes.map(pt => {
      const combinedName = pt.name + ': ' + pt.specimenDefinitionName.name;
      return Object.assign(pt, { combinedName })
    });
  }

  updateCollectionEventType() {
    this.specimenDefinition = undefined;
  }

  assignValues() {
    const input = this.processingType.specimenProcessing.input;

    if (this.inputTypeIsCollected && (this.eventType !== undefined)) {
      input.setDefinitionType(true);
      input.entityId = this.eventType.id;
      input.specimenDefinitionId = this.specimenDefinition.id;
    } else if ((this.inputTypeIsCollected === false) && (this.inputProcessingType !== undefined)) {
      input.setDefinitionType(false);
      input.entityId = this.inputProcessingType.id;
      input.specimenDefinitionId = this.inputProcessingType.specimenDefinitionName.id;
    }

    input.expectedChange  = this.expectedChange;
    input.count           = this.count;
    input.containerTypeId = this.containerTypeId;
  }

  previous() {
    this.assignValues();
    if (this.onPrevious()) {
      this.onPrevious()(this.processingType);
    }
  }

  submit() {
    this.assignValues();
    if (this.onSubmit()) {
      this.onSubmit()(this.processingType);
    }
  }

  cancel() {
    if (this.onCancel()) {
      this.onCancel()();
    }
  }

}

/**
 * Configuration parameter for {@link
 * admin.studies.components.processingTypeInputForm.processingTypeInputFormComponent
 * processingTypeInputFormComponent}.
 *
 * @enum {string}
 * @memberOf admin.studies.components.processingTypeInputForm
 */
const ProcessingTypeInputFormButtonConfig = {
  /** Submit and Cancel button are shown.*/
  TWO_BUTTON: '2-button',

  /** Previous, Next and Cancel button are shown.*/
  THREE_BUTTON: '3-button'
};

/**
 * An AngularJS component that displays a form so that the user can edit the input specimen for a
 * {@link domain.studies.ProcessingType ProcessingType}.
 *
 * @param {domain.studies.ProcessingType} processingType The processing type to modify.
 *
 * @param {function} getCollectionSpecimenDefinitions
 *
 * @param {function} getProcessedSpecimenDefinitions
 *
 * @param {admin.studies.components.processingTypeInputForm.ProcessingTypeInputFormButtonConfig} buttonConfig
 *
 * @param {function} onPrevious
 *
 * @param {function} onSubmit
 *
 * @param {function} onCancel
 *
 *
 * @memberOf admin.studies.components.processingTypeInputForm
 */
const processingTypeInputFormComponent = {
  template: require('./processingTypeInputForm.html'),
  controller: ProcessingTypeInputFormController,
  controllerAs: 'vm',
  bindings: {
    processingType:                   '<',
    getCollectionSpecimenDefinitions: '&',
    getProcessedSpecimenDefinitions:  '&',
    buttonConfig:                     '@',
    onPrevious:                       '&',
    onSubmit:                         '&',
    onCancel:                         '&'
  }
};

export default ngModule => {
  ngModule
    .constant('ProcessingTypeInputFormButtonConfig', ProcessingTypeInputFormButtonConfig)
    .component('processingTypeInputForm', processingTypeInputFormComponent);
}
