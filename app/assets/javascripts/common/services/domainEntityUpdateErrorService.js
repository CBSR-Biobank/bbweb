define([], function(){
  'use strict';

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

      if (error.data.message instanceof String) {
        if (error.data.message.indexOf('expected version doesn\'t match current version') > -1) {
          /* concurrent change error */
          modalDefaults.templateUrl = '/assets/javascripts/common/modalConcurrencyError.html';
          modalOptions.domainType = domainObjTypeName;
        } else {
          /* some other error */
          modalOptions.headerHtml = 'An error happened when submitting this change.';
          modalOptions.bodyHtml = 'Error: ' + error.data.message;
        }
      } else {
        // most likely a programming error
        console.log('Error:', error.data.message.toString);
        modalOptions.headerHtml = 'An error happened when submitting this change.';
        modalOptions.bodyHtml = 'Error: ' + JSON.stringify(error.data.message);
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

  return domainEntityUpdateError;
});
