/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./studyView.html'),
  controller: StudyViewController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function StudyViewController($controller,
                             $scope,
                             $window,
                             $state,
                             gettextCatalog,
                             breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies'),
      breadcrumbService.forStateWithFunc('home.admin.studies.study', function () {
        return vm.study.name;
      })
    ];

    // initialize this controller's base class
    $controller('TabbedPageController',
                {
                  vm:     vm,
                  $scope: $scope,
                  $state: $state
                });

    vm.tabs = [
      {
        heading: gettextCatalog.getString('Summary'),
        sref: 'home.admin.studies.study.summary',
        active: true
      },
      {
        heading: gettextCatalog.getString('Participants'),
        sref: 'home.admin.studies.study.participants',
        active: false
      },
      {
        heading: gettextCatalog.getString('Collection'),
        sref: 'home.admin.studies.study.collection',
        active: false
      },
      {
        heading: gettextCatalog.getString('Processing'),
        sref: 'home.admin.studies.study.processing',
        active: false
      }
    ];

    $scope.$on('study-name-changed', studyNameUpdated);
  }

  function studyNameUpdated(event, study) {
    event.stopPropagation();
    vm.study = study;
  }
}

export default ngModule => ngModule.component('studyView', component)
