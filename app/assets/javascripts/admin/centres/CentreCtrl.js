define(['../module'], function(module) {
  'use strict';

  module.controller('CentreCtrl', CentreCtrl);

  CentreCtrl.$inject = ['$window', '$scope', '$state', '$timeout', 'centresService'];

  /**
   *
   */
  function CentreCtrl($window, $scope, $state, $timeout, centresService) {
    var vm = this;
    vm.centre = {};

    vm.tabSummaryActive   = false;
    vm.tabLocationsActive = false;
    vm.tabStudiesActive   = false;

    init();
    activeTabUpdateFix();

    //--

    // initialize the panels to open state when viewing a new centre
    function init() {
      centresService.get($state.params.centreId).then(function (centre) {
        vm.centre = centre;

        if (centre.id !== $window.localStorage.getItem('centre.panel.centreId')) {
          // this way when the user selects a new centre, the panels always default to open
          $window.localStorage.setItem('centre.panel.locations', true);

          // remember the last viewed centre
          $window.localStorage.setItem('centre.panel.centreId', centre.id);
        }
      });
    }

    function activeTabUpdateFix() {
      $timeout(activeTabUpdate, 0);

      function activeTabUpdate() {
        vm.tabSummaryActive   = ($state.current.name === 'admin.centres.centre.summary');
        vm.tabLocationsActive = ($state.current.name === 'admin.centres.centre.locations');
        vm.tabStudiesActive   = ($state.current.name === 'admin.centres.centre.studies');
      }
    }


  }

});
