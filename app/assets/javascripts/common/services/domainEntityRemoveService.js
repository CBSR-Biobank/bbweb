define(['../module'], function(module) {
  'use strict';

  module.service('domainEntityRemoveService', domainEntityRemoveService);

  domainEntityRemoveService.$inject = ['$state', 'modalService'];

  /**
   *
   */
  function domainEntityRemoveService($state, modalService) {
    var service = {
      remove : remove
    };
    return service;

    //-------

    function remove(modalTitle, modalMsg, removeErrorMsgPrefix, removeFn, domainObj, returnState) {
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerText: modalTitle,
        bodyText: modalMsg
      };

      modalService.showModal({}, modalOptions)
        .then(function () {
          removeFn(domainObj).then(reloadReturnState).catch(removeFailed);
        })
        .catch(function() {
          gotoReturnState();
        });

      function removeFailed(error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          headerText: 'Remove failed',
          bodyText: removeErrorMsgPrefix + error
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

  }

});
