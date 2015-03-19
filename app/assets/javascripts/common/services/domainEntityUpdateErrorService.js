define([], function(){
  'use strict';

  domainEntityUpdateError.$inject = ['$state', 'modalService', 'stateHelper'];

  /**
   * Called when either adding or updating a domain object and there is afailure. Displays the error message
   * and asks the user if he / she wishes to attempt the change again.
   *
   * If the user presses the OK button, then the current state is reloaded. If the Cancel button is pressed
   * the users is takent to 'returnState'.
   */
  function domainEntityUpdateError($state, modalService, stateHelper) {
    var service = {
      handleError: handleError,
      handleErrorNoStateChange: handleErrorNoStateChange
    };
    return service;

    //-------

    /**
     * @deprecated use handleErrorNoStateChange instead
     */
    function handleError(error, domainObjTypeName, returnStateName, returnStateParams, returnStateOptions) {
      handleErrorNoStateChange(error, domainObjTypeName)
        .then(function() {
          stateHelper.reloadAndReinit();
        })
        .catch(function() {
          $state.go(returnStateName, returnStateParams, returnStateOptions);
        });
    }
    /**
     * Rename to handleError once the original is removed.
     */
    function handleErrorNoStateChange(error, domainObjTypeName) {
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
          modalOptions.headerHtml = 'Cannot submitting this change';
          modalOptions.bodyHtml = 'Error: ' + error.data.message;
        }
      } else {
        // most likely a programming error
        console.log('Error:', error.data.message.toString);
        modalOptions.headerHtml = 'Cannot submitting this change';
        modalOptions.bodyHtml = 'Error: ' + error.data.message;
      }

      return modalService.showModal(modalDefaults, modalOptions);
    }
  }

  return domainEntityUpdateError;
});
