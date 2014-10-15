define(['../module'], function(module) {
  'use strict';

  module.controller('CentresCtrl', CentresCtrl);

  CentresCtrl.$inject = ['centres'];

  /**
   * Displays a list of centres with each in its own mini-panel.
   */
  function CentresCtrl(centres) {
    var vm = this;
    vm.centres = centres;
  }

});
