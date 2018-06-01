/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.processingTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class ProcessingTypeAddController {

  constructor($state,
              breadcrumbService,
              gettextCatalog) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    breadcrumbService,
                    gettextCatalog
                  });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.processing({ studySlug: "${this.study.slug}" })`,
        () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.processing.addType',
        () => this.gettextCatalog.getString('Add specimen processing'))
    ];
  }

}

/**
 * An AngularJS component that allows the user to define a @{link domain.studies.ProcessingType
 * ProcessingType}.
 *
 * @memberOf admin.studies.components.processingTypeAdd
 */
const processingTypeAddComponent = {
  template: require('./processingTypeAdd.html'),
  controller: ProcessingTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.processing.addType', {
    url: '/add',
    views: {
      'main@': 'processingTypeAdd'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('processingTypeAdd', processingTypeAddComponent);
}
