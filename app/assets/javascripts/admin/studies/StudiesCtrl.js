define(['../module'], function(module) {
  'use strict';

  module.controller('StudiesCtrl', StudiesCtrl);

  StudiesCtrl.$inject = ['$state', '$log', 'studies'];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesCtrl($state, $log, studies) {
    var vm = this;
    vm.studies = studies;
  }

});
