/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  const angular = require('angular');

  domainNotificationService.$inject = [
    '$q',
    '$log',
    '$state',
    'gettextCatalog',
    'modalService'
  ];

  /**
   * Utilities for services that access domain objects.
   */
  function domainNotificationService($q, $log, $state, gettextCatalog, modalService) {
    var service = {
      updateErrorModal:     updateErrorModal,
      removeEntity:         removeEntity
    };
    return service;

    //-------

    /**
     * Called when either adding or updating a domain entity and there is afailure. Displays the error message
     * and asks the user if he / she wishes to attempt the change again.
     *
     * Returns a promise. The promise is resolved if the user pressed the OK button, or rejected if the CANCEL
     * button was pressed.
     */
    function updateErrorModal(error, domainObjTypeName) {
      var modalDefaults = {};
      var modalOptions = {
        closeButtonText: gettextCatalog.getString('Cancel'),
        actionButtonText: gettextCatalog.getString('OK')
      };

      if (error.message) {
        $log.error(error.message);
      }

      if ((typeof error.message === 'string') &&
          (error.message.indexOf('expected version doesn\'t match current version') > -1)) {
          /* concurrent change error */
          modalDefaults.template = require('./modalConcurrencyError.html');
          modalOptions.domainType = domainObjTypeName;
      } else {
        // most likely a programming error
        modalOptions.headerHtml = gettextCatalog.getString('Cannot submit this change');
        modalOptions.bodyHtml = gettextCatalog.getString('Error: ') + JSON.stringify(error.message);
      }

      return modalService.showModal(modalDefaults, modalOptions);
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
     * @param {function} promiseFunc - A function that returns a promise. It is invoked if the user confirms
     * that he / she wants to proceed with removing the entity.
     *
     * @param {String} headerHtml - The header text to display in the confirmation dialog box.
     *
     * @param {String} bodyHtml - The body text to display in the confirmation dialog box.
     *
     * @param {String} removeFailedHeaderHtml - The header text to display in the remove error dialog box.
     *
     * @param {String} removeFaileBodyHtml - The body text to display in the remove error dialog box.
     *
     * @return {Promise<boolean>} The promise is resolved if the entity was removed. The promise is rejected
     * if the user does not want to remove the entity or if the server does not allow the entity to be
     * removed.
     */
    function removeEntity(promiseFunc,
                          headerHtml,
                          bodyHtml,
                          removeFailedHeaderHtml,
                          removeFaileBodyHtml) {
      return modalService.modalOkCancel(headerHtml, bodyHtml)
        .then(() => promiseFunc().catch((error) => {
          var errMsg = JSON.stringify(error);
          if (error.status && (error.status === 401)) {
            errMsg = gettextCatalog.getString('You do not have permission to perform this action');
          }
          return modalService.modalOkCancel(removeFailedHeaderHtml,
                                            removeFaileBodyHtml + ': ' + errMsg);
        }))
        .catch(angular.noop);
    }
  }

  return domainNotificationService;
});
