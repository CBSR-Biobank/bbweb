/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  annotationTypeAddService.$inject = [
    '$state',
    'notificationsService',
    'domainEntityService'
  ];

  /**
   * Description
   */
  function annotationTypeAddService($state,
                                    notificationsService,
                                    domainEntityService) {
    var service = {
      onAddSuccessful: onAddSuccessful,
      onAddFailed: onAddFailed,
      onCancel: onCancel
    };
    return service;

    //-------

    function onAddSuccessful(state) {
      return notifyAndGotoState;

      function notifyAndGotoState() {
        notificationsService.submitSuccess();
        $state.go(state, {}, { reload: true });
      }
    }

    function onAddFailed(error) {
      return domainEntityService.updateErrorModal(error, 'study');
    }

    function onCancel(state) {
      return gotoState;

      function gotoState() {
        $state.go(state);
      }
    }

  }

  return annotationTypeAddService;
});
