/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./centreView.html'),
    controller: CentreViewDirective,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  CentreViewDirective.$inject = [
    '$window',
    '$controller',
    '$scope',
    '$state',
    'gettextCatalog',
    'breadcrumbService'
  ];

  /*
   * Controller for this component.
   */
  function CentreViewDirective($window,
                               $controller,
                               $scope,
                               $state,
                               gettextCatalog,
                               breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    // initialize this controller's base class
    $controller('TabbedPageController',
                {
                  vm:     vm,
                  $scope: $scope,
                  $state: $state
                });

    //--

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.centres'),
        breadcrumbService.forStateWithFunc('home.admin.centres.centre',
                                           function () { return vm.centre.name; })
      ];

      vm.tabs = [
        {
          heading: gettextCatalog.getString('Summary'),
          sref: 'home.admin.centres.centre.summary',
          active: true
        },
        {
          heading: gettextCatalog.getString('Studies'),
          sref: 'home.admin.centres.centre.studies',
          active: true
        },
        {
          heading: gettextCatalog.getString('Locations'),
          sref: 'home.admin.centres.centre.locations',
          active: true
        },
      ];

      $scope.$on('centre-name-changed', centreNameUpdated);
    }

     function centreNameUpdated(event, centre) {
      event.stopPropagation();
      vm.centre = centre;
     }

  }

  return component;
});
