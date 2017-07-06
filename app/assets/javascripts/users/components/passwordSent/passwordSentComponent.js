/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/users/components/passwordSent/passwordSent.html',
    controller: PasswordSentController,
    controllerAs: 'vm',
    bindings: {
      email: '<'
    }
  };

  //PasswordSentController.$inject = [];

  /*
   * Controller for this component.
   */
  function PasswordSentController() {
  }

  return component;
});
