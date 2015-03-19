define([], function(){
  'use strict';

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

  }

  return domainEntityRemoveService;
});
