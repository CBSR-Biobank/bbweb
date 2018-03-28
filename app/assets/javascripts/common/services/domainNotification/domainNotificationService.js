/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';

/**
 * Utilities for services that access domain objects.
 *
 * @memberOf common.services
 */
class DomainNotificationService {

  constructor($q, $log, $state, gettextCatalog, modalService) {
    'ngInject';
    Object.assign(this, { $q, $log, $state, gettextCatalog, modalService });
  }

  /**
   * Used when updating a {@link domain|Domain Entity}.
   *
   * Called when either adding or updating a domain entity and there is afailure. Displays the error message
   * and asks the user if they wish to attempt the change again.
   *
   * @param {object} error an object contaiing the error.
   *
   * @param {string} domainEntityName - the name of the domain entity.
   *
   * @return {Promise<boolean>} The promise is resolved if the user pressed the `OK` button, or rejected if
   * the `CANCEL` button was pressed.
   */
  updateErrorModal(error, domainEntityName) {
    var modalDefaults = {};
    var modalOptions = {
      closeButtonText: this.gettextCatalog.getString('Cancel'),
      actionButtonText: this.gettextCatalog.getString('OK')
    };

    if (error.message) {
      this.$log.error(error.message);
    }

    if ((typeof error.message === 'string') &&
        (error.message.indexOf('expected version doesn\'t match current version') > -1)) {
      /* concurrent change error */
      modalDefaults.template = require('./modalConcurrencyError.html');
      modalOptions.domainType = domainEntityName;
    } else {
      // most likely a programming error
      modalOptions.headerHtml = this.gettextCatalog.getString('Cannot submit this change');
      modalOptions.bodyHtml = this.gettextCatalog.getString('Error: ') + JSON.stringify(error.message);
    }

    return this.modalService.showModal(modalDefaults, modalOptions);
  }

  /**
   * Used when removing a {@link domain|Domain Entity}.
   *
   * First a confirmation dialog box is shown to the user asking him to confirm that he wishes to remove the
   * entity. The user can either confirm the action by pressing the `OK` button, or cancel the action by
   * pressing the `Cancel` button. If the user confirmed the action, a request is sent to the server to remove
   * the entity. If the user cancelled the action the returned promise is rejected.
   *
   * If the server allows the removal of the entity, then the promise is resolved successfully. If the server
   * responds with an error, another dialog box is displayed showing the error and the promise is rejected.
   *
   * @param {function} promiseFunc - A function that returns a promise. It is invoked if the user confirms
   * that he / she wants to proceed with removing the entity.
   *
   * @param {string} headerHtml - The header text to display in the confirmation dialog box.
   *
   * @param {string} bodyHtml - The body text to display in the confirmation dialog box.
   *
   * @param {string} removeFailedHeaderHtml - The header text to display in the remove error dialog box.
   *
   * @param {string} removeFaileBodyHtml - The body text to display in the remove error dialog box.
   *
   * @return {Promise<boolean>} The promise is resolved if the entity was removed. The promise is rejected
   * if the user does not want to remove the entity or if the server does not allow the entity to be
   * removed.
   */
  removeEntity(promiseFunc,
               headerHtml,
               bodyHtml,
               removeFailedHeaderHtml,
               removeFaileBodyHtml) {
    return this.modalService.modalOkCancel(headerHtml, bodyHtml)
      .then(() => promiseFunc()
            .catch((error) => {
              var errMsg = JSON.stringify(error);
              if (error.status && (error.status === 401)) {
                errMsg = this.gettextCatalog.getString('You do not have permission to perform this action');
              }
              return this.modalService.modalOkCancel(removeFailedHeaderHtml,
                                                     removeFaileBodyHtml + ': ' + errMsg);
            }))
      .catch(angular.noop);
  }
}

export default ngModule => ngModule.service('domainNotificationService', DomainNotificationService)
