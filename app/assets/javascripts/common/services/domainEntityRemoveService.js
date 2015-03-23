define([], function(){
  'use strict';

  domainEntityRemoveService.$inject = ['$q', '$state', 'modalService'];

  /**
   *
   */
  function domainEntityRemoveService($q, $state, modalService) {
    var service = {
      remove : remove,
      removeNoStateChange: removeNoStateChange
    };
    return service;

    //-------

    function remove(modalTitle, modalMsg, removeErrorMsgPrefix, removeFn, domainObj, returnState) {
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: modalTitle,
        bodyHtml: modalMsg
      };

      modalService.showModal({}, modalOptions).then(removeEntity).catch(gotoReturnState);

      function removeEntity() {
        return removeFn(domainObj).then(reloadReturnState).catch(removeFailed);
      }

      function removeFailed(error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          headerHtml: 'Remove failed',
          bodyHtml: removeErrorMsgPrefix + error
        };

        modalService.showModal({}, modalOptions)
          .then(gotoReturnState)
          .catch(gotoReturnState);
      }

      function gotoReturnState(options) {
        var opts = options || {};
        $state.go(returnState, {}, opts);
      }

      function reloadReturnState() {
        gotoReturnState({reload: true});
      }
    }

    /**
     * First a confirmation dialog box is shown to the user asking him to confirm that he wishes to remove the
     * entity. The user can either confirm the action by pressing the OK button, or cancel the action by
     * pressing the Cancel button. If the user confirmed the action, a request is sent to the server to remove
     * the entity. If the user cancelled the action the returned promise is rejected.
     *
     * If the server allows the removal of the entity, then the promise is resolved successfully.  If the
     * server responds with an error, another dialog box is displayed showing the error and the promise is
     * rejected.
     *
     * @param  {function} removePromiseFn - A function that returns a promise that removes the entity.
     *
     * @param {String} headerHtml - The header text to display in the confirmation dialog box.
     *
     * @param {String} bodyHtml - The body text to display in the confirmation dialog box.
     *
     * @param {String} removeFailedHeaderHtml - The header text to display in the remove error dialog box.
     *
     * @param {String} removeFaileBodyHtml - The body text to display in the remove error dialog box.
     *
     * @return A promise. The promise is resolved if the entity was removed. The promise is rejected if the
     * user does not want to remove the entity or if the server does not allow the entity to be removed.
     */
    function removeNoStateChange(removePromiseFn,
                                 headerHtml,
                                 bodyHtml,
                                 removeFailedHeaderHtml,
                                 removeFaileBodyHtml) {
      var deferred = $q.defer();

      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: headerHtml,
        bodyHtml: bodyHtml
        };

      modalService.showModal({}, modalOptions).then(removeConfirmed).catch(deferred.reject);
      return deferred.promise;

      function removeConfirmed() {
        return removePromiseFn()
          .then(deferred.resolve)
          .catch(function (error) {
            var modalOptions = {
              closeButtonText: 'Cancel',
              headerHtml: removeFailedHeaderHtml,
              bodyHtml: removeFaileBodyHtml + ': ' + error
            };
            modalService.showModal({}, modalOptions).then(deferred.reject);
          });
      }
    }

  }

  return domainEntityRemoveService;
});
