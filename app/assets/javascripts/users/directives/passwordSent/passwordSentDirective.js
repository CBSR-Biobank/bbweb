/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function passwordSentDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        email: '='
      },
      templateUrl : '/assets/javascripts/users/directives/passwordSent/passwordSent.html',
      controller: PasswordSentCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  //PasswordSentCtrl.$inject = [];

  /**
   * Tells the user that his new password was sent via email.
   *
   * Template file: passwordSent.html
   * State definition: states.js
   */
  function PasswordSentCtrl() {
    var vm = this;
  }

  return passwordSentDirective;
});
