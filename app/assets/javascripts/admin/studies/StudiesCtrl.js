define(['../module'], function(module) {
  'use strict';

  module.controller('StudiesCtrl', StudiesCtrl);

  StudiesCtrl.$inject = ['$state', 'paginatedStudies'];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesCtrl($state, paginatedStudies) {
    var vm = this;
    vm.studies = paginatedStudies.items;
    vm.paginatedStudies = paginatedStudies;
  }

});
