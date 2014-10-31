define(['../module'], function(module) {
  'use strict';

  module.controller('StudiesCtrl', StudiesCtrl);

  StudiesCtrl.$inject = ['$state', 'studies'];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesCtrl($state, studies) {
    var vm = this;
    vm.studies = studies;
  }

});
