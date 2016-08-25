/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  domainNotificationService.$inject = [
    '$q',
    '$log',
    '$state',
    'gettext',
    'modalService'
  ];

  /**
   * Utilities for services that access domain objects.
   */
  function domainNotificationService($q, $log, $state, gettext, modalService) {
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
        closeButtonText: gettext('Cancel'),
        actionButtonText: gettext('OK')
      };

      if (error.data.message) {
        $log.error(error.data.message);
      }

      if ((typeof error.data.message === 'string') &&
          (error.data.message.indexOf('expected version doesn\'t match current version') > -1)) {
          /* concurrent change error */
          modalDefaults.templateUrl = '/assets/javascripts/common/modalConcurrencyError.html';
          modalOptions.domainType = domainObjTypeName;
      } else {
        // most likely a programming error
        modalOptions.headerHtml = gettext('Cannot submit this change');
        modalOptions.bodyHtml = gettext('Error: ') + JSON.stringify(error.data.message);
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
     * @param  {function} entity - The domain entity to be removed.
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
    function removeEntity(promiseFunc,
                          headerHtml,
                          bodyHtml,
                          removeFailedHeaderHtml,
                          removeFaileBodyHtml) {
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: headerHtml,
        bodyHtml: bodyHtml
      };

      return modalService.showModal({}, modalOptions).then(removeConfirmed);

      function removeConfirmed() {
        return promiseFunc().catch(function (error) {
          var modalOptions = {
            closeButtonText: gettext('Cancel'),
            headerHtml:      removeFailedHeaderHtml,
            bodyHtml:        removeFaileBodyHtml + ': ' + error
          };
          modalService.showModal({}, modalOptions);
        });
      }

    }
  }

  return domainNotificationService;
});
