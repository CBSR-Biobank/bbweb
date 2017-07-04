/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/centres/components/centreView/centreView.html',
    controller: CentreViewDirective,
    controllerAs: 'vm',
    bindings: {
      centre: '='
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

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.centres'),
      breadcrumbService.forStateWithFunc('home.admin.centres.centre', function () {
        return vm.centre.name;
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

    //--

    /*
     * initialize the panels to open state when viewing a new centre
     */
    function onInit() {
      if (vm.centre.id !== $window.localStorage.getItem('centre.panel.centreId')) {
        // this way when the user selects a new centre, the panels always default to open
        $window.localStorage.setItem('centre.panel.locations', true);

        // remember the last viewed centre
        $window.localStorage.setItem('centre.panel.centreId', vm.centre.id);
      }
    }

     function centreNameUpdated(event, centre) {
      event.stopPropagation();
      vm.centre = centre;
     }

  }

  return component;
});
