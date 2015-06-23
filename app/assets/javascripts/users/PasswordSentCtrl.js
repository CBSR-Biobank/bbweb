/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

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

  return PasswordSentCtrl;
});
