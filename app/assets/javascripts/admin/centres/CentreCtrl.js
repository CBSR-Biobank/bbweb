define([], function() {
  'use strict';

  CentreCtrl.$inject = [
    '$window',
    '$scope',
    '$state',
    '$timeout',
    'centre'
  ];

  /**
   *
   */
  function CentreCtrl($window, $scope, $state, $timeout, centre) {
    var vm = this;
    vm.centre = centre;

    vm.tabSummaryActive   = false;
    vm.tabLocationsActive = false;
    vm.tabStudiesActive   = false;

    init();
    activeTabUpdateFix();

    //--

    // initialize the panels to open state when viewing a new centre
    function init() {
      if (vm.centre.id !== $window.localStorage.getItem('centre.panel.centreId')) {
        // this way when the user selects a new centre, the panels always default to open
        $window.localStorage.setItem('centre.panel.locations', true);

        // remember the last viewed centre
        $window.localStorage.setItem('centre.panel.centreId', centre.id);
      }
    }

    function activeTabUpdateFix() {
      $timeout(activeTabUpdate, 0);

      function activeTabUpdate() {
        vm.tabSummaryActive   = ($state.current.name === 'home.admin.centres.centre.summary');
        vm.tabLocationsActive = ($state.current.name === 'home.admin.centres.centre.locations');
        vm.tabStudiesActive   = ($state.current.name === 'home.admin.centres.centre.studies');
      }
    }

  }

  return CentreCtrl;
});
