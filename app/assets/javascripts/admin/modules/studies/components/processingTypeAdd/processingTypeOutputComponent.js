class ProcessingTypeOutput {

  constructor($state,
              ProcessingTypeAdd,
              ProcessingTypeAddTasks,
              modalService,
              gettextCatalog,
              notificationsService,
              domainNotificationService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    ProcessingTypeAdd,
                    ProcessingTypeAddTasks,
                    modalService,
                    gettextCatalog,
                    notificationsService,
                    domainNotificationService
                  });

  }

  $onInit() {
    this.progressInfo = this.ProcessingTypeAddTasks.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 3);
      return taskInfo;
    });

    this.ProcessingTypeAdd.initIfRequired();
    if (!this.ProcessingTypeAdd.isValid()) {
      // page was reloaded, must go back to first step
      this.goToFirstState();
      return;
    }
  }

  goToFirstState() {
    this.$state.go('home.admin.studies.study.processing.addType.information',
                   {
                     study: this.study,
                     initialize: true
                   });
  }

  previous() {
    this.$state.go('home.admin.studies.study.processing.addType.input');
  }

  submit() {
    this.ProcessingTypeAdd.processingType.add()
      .then(() => {
        this.notificationsService.submitSuccess();
        this.$state.go('home.admin.studies.study.processing');
      })
      .catch(error => {
        if ((typeof error.message === 'string') && (error.message.indexOf('name already exists') > -1)) {
          this.modalService.modalOk(
            this.gettextCatalog.getString('Cannot Add'),
            this.gettextCatalog.getString(
              'The name <b>{{name}}</b> has already been used. Please use another name.',
              { name : this.ProcessingTypeAdd.processingType.name }));
        } else {
          this.domainNotificationService.updateErrorModal(
            error,
            this.gettextCatalog.getString('processing type'));
        }
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
