class ProcessingTypeOutput {

  constructor($state,
              ProcessingTypeAdd,
              ProcessingTypeAddTasks,
              AnatomicalSourceType,
              PreservationType,
              PreservationTemperature,
              SpecimenType) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    ProcessingTypeAdd,
                    ProcessingTypeAddTasks,
                    AnatomicalSourceType,
                    PreservationType,
                    PreservationTemperature,
                    SpecimenType
                  });

    this.anatomicalSourceTypes = Object.values(this.AnatomicalSourceType);
    this.preservTypes          = Object.values(this.PreservationType);
    this.preservTempTypes      = Object.values(this.PreservationTemperature);
    this.specimenTypes         = Object.values(this.SpecimenType);
  }

  $onInit() {
    this.progressInfo = this.ProcessingTypeAddTasks.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 3);
      return taskInfo;
    });

    this.ProcessingTypeAdd.initIfRequired();
    if (!this.ProcessingTypeAdd.isValid()) {
      // page was reloaded, must go back to first step
      this.goToFirstState(true);
      return;
    }

    const output = this.ProcessingTypeAdd.processingType.specimenProcessing.output;

    this.expectedChange          = output.expectedChange;
    this.count                   = output.count;
    this.containerTypeId         = output.containerTypeId;
    this.name                    = output.specimenDefinition.name;
    this.description             = output.specimenDefinition.description;
    this.units                   = output.specimenDefinition.units;
    this.anatomicalSourceType    = output.specimenDefinition.anatomicalSourceType;
    this.preservationType        = output.specimenDefinition.preservationType;
    this.preservationTemperature = output.specimenDefinition.preservationTemperature;
    this.specimenType            = output.specimenDefinition.specimenType;
  }

  goToFirstState() {
    this.$state.go('home.admin.studies.study.processing.addType.information',
                   {
                     study: this.study,
                     initialize: true
                   });
  }

  assignValues() {
   const output = this.ProcessingTypeAdd.processingType.specimenProcessing.output;

    output.expectedChange                             = this.expectedChange;
    output.count                                      = this.count;
    output.containerTypeId                            = this.containerTypeId;
    output.specimenDefinition.name                    = this.name;
    output.specimenDefinition.description             = this.description;
    output.specimenDefinition.units                   = this.units;
    output.specimenDefinition.anatomicalSourceType    = this.anatomicalSourceType;
    output.specimenDefinition.preservationType        = this.preservationType;
    output.specimenDefinition.preservationTemperature = this.preservationTemperature;
    output.specimenDefinition.specimenType            = this.specimenType;

  }

  previous() {
    this.assignValues();
    this.$state.go('home.admin.studies.study.processing.addType.input');
  }

  submit() {
    this.assignValues();
    this.ProcessingTypeAdd.processingType.add()
      .then(() => {
        this.$state.go('home.admin.studies.study.processing');
      });
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
const processingTypeOutput = {
  template: require('./processingTypeOutput.html'),
  controller: ProcessingTypeOutput,
  controllerAs: 'vm',
  bindings: {
  }
};

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.processing.addType.output', {
    url: '/output',
    views: {
      'processingTypeAdd': 'processingTypeOutput'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('processingTypeOutput', processingTypeOutput);
}
