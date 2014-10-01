define(['./module'], function(module) {
  'use strict';

  module.controller('PasswordSentCtrl', PasswordSentCtrl);

  PasswordSentCtrl.$inject = ['$stateParams'];

  /**
   * Tells the user that his new password was sent via email.
   *
   * Template file: passwordSent.html
   * State definition: states.js
   */
  function PasswordSentCtrl($stateParams) {
    var vm = this;
    vm.email = $stateParams.email;
  }

});
