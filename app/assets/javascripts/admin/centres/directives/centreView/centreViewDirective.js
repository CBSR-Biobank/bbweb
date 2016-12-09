/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function centreViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        centre: '='
      },
      templateUrl : '/assets/javascripts/admin/centres/directives/centreView/centreView.html',
      controller: CentreViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreViewCtrl.$inject = [
    '$window',
    '$controller',
    '$scope',
    '$state',
    'gettextCatalog'
  ];

  function CentreViewCtrl($window,
                          $controller,
                          $scope,
                          $state,
                          gettextCatalog) {
    var vm = this;

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

    init();

    //--

    /**
     * initialize the panels to open state when viewing a new centre
     */
    function init() {
      if (vm.centre.id !== $window.localStorage.getItem('centre.panel.centreId')) {
        // this way when the user selects a new centre, the panels always default to open
        $window.localStorage.setItem('centre.panel.locations', true);

        // remember the last viewed centre
        $window.localStorage.setItem('centre.panel.centreId', vm.centre.id);
      }
    }

  }

  return centreViewDirective;
});
