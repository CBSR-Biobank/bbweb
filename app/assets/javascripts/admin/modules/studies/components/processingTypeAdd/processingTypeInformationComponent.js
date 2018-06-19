class ProcessingTypeInformationController {

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
  }

  $onInit() {
    this.progressInfo = this.ProcessingTypeAddTasks.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 1);
      return taskInfo;
    });

    if (this.initialize === undefined) {
      this.initialize = true;
    }

    if (this.initialize) {
      this.ProcessingTypeAdd.init();
    } else {
      this.ProcessingTypeAdd.initIfRequired();
      const processingType = this.ProcessingTypeAdd.processingType;
      this.name        = processingType.name;
      this.description = processingType.description;
      this.enabled     = processingType.enabled;
    }
  }

  next() {
    this.ProcessingTypeAdd.processingType.studyId = this.study.id;

    const processingType = this.ProcessingTypeAdd.processingType;
    processingType.name        = this.name;
    processingType.description = this.description;
    processingType.enabled     = this.enabled || false;
    this.$state.go('^.input');
  }

  cancel() {
    this.$state.go('^.^');
  }

}

/**
 * An AngularJS component that
 *
 * @memberOf
 */
const processingTypeInformationComponent = {
  template: require('./processingTypeInformation.html'),
  controller: ProcessingTypeInformationController,
  controllerAs: 'vm',
  bindings: {
    study: '<',
    initialize: '<'
  }
};

function resolveInitialize($transition$) {
  'ngInject';
  return $transition$.params().initialize !== 'false';
}

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.processing.addType.information', {
    url: '/information?initialize',
    resolve: {
      initialize: resolveInitialize
    },
    views: {
      'processingTypeAdd': 'processingTypeInformation'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('processingTypeInformation', processingTypeInformationComponent);
}
