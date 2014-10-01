define(['../module'], function(module) {
  'use strict';

  module.service('domainEntityUpdateError', domainEntityUpdateError);

  domainEntityUpdateError.$inject = ['modalService', 'stateHelper'];

  /**
   * Called when either adding or updating a domain object and there is afailure. Displays the error message
   * and asks the user if he / she wishes to attempt the change again.
   *
   * If the user presses the OK button, then the current state is reloaded. If the Cancel button is pressed
   * the users is takent to 'returnState'.
   */
  function domainEntityUpdateError(modalService, stateHelper) {
    var service = {
      handleError: handleError
    };
    return service;

    //-------

    function handleError(error, domainObjTypeName, returnStateName, returnStateParams, returnStateOptions) {
      var modalDefaults = {};
      var modalOptions = {
        closeButtonText: 'Cancel',
        actionButtonText: 'OK'
      };

      if (error.message.indexOf('expected version doesn\'t match current version') > -1) {
        /* concurrent change error */
        modalDefaults.templateUrl = '/assets/javascripts/common/services/modalConcurrencyError.html';
        modalOptions.domainType = domainObjTypeName;
      } else {
        /* some other error */
        modalOptions.headerText = 'An error happened when submitting this change.';
        modalOptions.bodyText = 'Error: ' + error.message;
      }

      modalService.showModal(modalDefaults, modalOptions)
        .then(function() {
          stateHelper.reloadAndReinit();
        })
        .catch(function() {
          stateHelper.reloadStateAndReinit(returnStateName, returnStateParams, returnStateOptions);
        });
    }
  }

});
