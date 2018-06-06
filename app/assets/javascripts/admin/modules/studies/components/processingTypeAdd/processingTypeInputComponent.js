class ProcessingTypeInput {

  constructor($state,
              ProcessingTypeAdd,
              ProcessingTypeAddTasks) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    ProcessingTypeAdd,
                    ProcessingTypeAddTasks
                  });
    this.inputType = undefined;
    this.processingTypes = [];
    this.haveProcessingTypes = false;
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
  }

  getCollectionSpecimenDefinitions() {
    return this.ProcessingTypeAdd.getCollectionSpecimenDefinitions(this.study);
  }

  getProcessedSpecimenDefinitions() {
    return this.ProcessingTypeAdd.getProcessedSpecimenDefinitions(this.study);
  }

  goToPreviousState(initialize) {
    this.$state.go('home.admin.studies.study.processing.addType.information',
                   {
                     study: this.study,
                     initialize: initialize
                   });
  }

  previous(processingType) {
    this.ProcessingTypeAdd.processingType = processingType;
    this.goToPreviousState(false);
  }

  next(processingType) {
    this.ProcessingTypeAdd.processingType = processingType;
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
