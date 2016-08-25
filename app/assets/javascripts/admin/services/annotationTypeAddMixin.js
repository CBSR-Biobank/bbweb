/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  annotationTypeAddMixin.$inject = [
    '$state',
    'notificationsService',
    'domainNotificationService'
  ];

  /**
   * A mixin that can be used by controllers that add annotation types. It is implemented as an Angular
   * factory.
   */
  function annotationTypeAddMixin($state,
                                  notificationsService,
                                  domainNotificationService) {
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
      return domainNotificationService.updateErrorModal(error, 'study');
    }

    function onCancel(state) {
      return gotoState;

      function gotoState() {
        $state.go(state);
      }
    }

  }

  return annotationTypeAddMixin;
});
