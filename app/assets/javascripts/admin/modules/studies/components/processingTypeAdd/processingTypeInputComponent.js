import _ from 'lodash';

class ProcessingTypeInput {

  constructor($state,
              ProcessingTypeAdd,
              ProcessingTypeAddTasks,
              CollectionEventType,
              CollectionEventTypeName,
              biobankApi) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    ProcessingTypeAdd,
                    ProcessingTypeAddTasks,
                    CollectionEventType,
                    CollectionEventTypeName,
                    biobankApi
                  });
    this.inputType = undefined;
  }

  $onInit() {
    this.inputType = undefined;
    this.progressInfo = this.ProcessingTypeAddTasks.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 2);
      return taskInfo;
    });

    this.ProcessingTypeAdd.initIfRequired();

    if (!this.ProcessingTypeAdd.isValid()) {
      // page was reloaded, must go back to first step
      this.goToPreviousState(true);
      return;
    }

    const input = this.ProcessingTypeAdd.processingType.specimenProcessing.input;
    if (input.definitionType !== undefined) {
      this.inputTypeIsCollected = input.isCollected();
    }

    if (this.inputTypeIsCollected) {
      this.ProcessingTypeAdd.getCollectedSpecimenDefinitions(this.study)
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
  }

  collectedSpecimen() {
    this.eventType = undefined;
    this.ProcessingTypeAdd.getCollectedSpecimenDefinitions(this.study)
      .then(reply => {
        this.eventTypes = reply;
        this.eventType = (reply.length === 1) ? reply[0].slug : undefined;
        this.commonInit();
      })
  }

  commonInit() {
    const input = this.ProcessingTypeAdd.processingType.specimenProcessing.input;
    this.expectedChange  = input.expectedChange;
    this.count           = input.count;
    this.containerTypeId = input.containerTypeId;
  }

  processedSpecimen() {
    console.log(this.inputTypeIsCollected);
    this.eventType = undefined;
  }

  updateCollectionEventType() {
    this.specimenDefinition = undefined;
  }

  assignValues() {
    const input = this.ProcessingTypeAdd.processingType.specimenProcessing.input;

    if (this.inputTypeIsCollected) {
      input.setDefinitionType(true);
      input.entityId = this.eventType.id;
      input.specimenDefinitionId = this.specimenDefinition.id;
    } else if (this.inputTypeIsProcessed) {
      input.setDefinitionType(false);
      input.entityId = this.processingType.id;
      input.specimenDefinitionId = this.processingType.output.specimenDefinition.id;
    }

    input.expectedChange  = this.expectedChange;
    input.count           = this.count;
    input.containerTypeId = this.containerTypeId;
   }

  goToPreviousState(initialize) {
    this.$state.go('home.admin.studies.study.processing.addType.information',
                   {
                     study: this.study,
                     initialize: initialize
                   });
  }

  previous() {
    this.assignValues();
    this.goToPreviousState(false);
  }

  next() {
    this.assignValues();
    this.$state.go('home.admin.studies.study.processing.addType.output');
  }

  cancel() {
    this.$state.go('home.admin.studies.study.processing');
  }

}

/**
 * An AngularJS component that
 *
 * @memberOf
 */
const processingTypeInput = {
  template: require('./processingTypeInput.html'),
  controller: ProcessingTypeInput,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.processing.addType.input', {
    url: '/input',
    views: {
      'processingTypeAdd': 'processingTypeInput'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('processingTypeInput', processingTypeInput);
}
