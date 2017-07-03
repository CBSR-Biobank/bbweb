/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/studies/components/studyView/studyView.html',
    controller: StudyViewController,
    controllerAs: 'vm',
    bindings: {
      study: '='
    }
  };

  StudyViewController.$inject = [
    '$controller',
    '$scope',
    '$window',
    '$state',
    'gettextCatalog',
    'breadcrumbService'
  ];

  /*
   * Controller for this component.
   */
  function StudyViewController($controller,
                               $scope,
                               $window,
                               $state,
                               gettextCatalog,
                               breadcrumbService) {
    var vm = this;

    vm.$onInit = onInit;

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

    //--

    /**
     * Initialize the panels to open state when viewing a new study.
     */
    function onInit() {
      if (vm.study.id !== $window.localStorage.getItem('study.panel.studyId')) {
        // this way when the user selects a new study, the panels always default to open
        $window.localStorage.setItem('study.panel.processingTypes',                true);
        $window.localStorage.setItem('study.panel.specimenLinkAnnotationTypes',    true);
        $window.localStorage.setItem('study.panel.specimenLinkTypes',              true);

        // remember the last viewed study
        $window.localStorage.setItem('study.panel.studyId', vm.study.id);
      }
    }

    function studyNameUpdated(event, study) {
      event.stopPropagation();
      vm.study = study;
    }
  }

  return component;
});
