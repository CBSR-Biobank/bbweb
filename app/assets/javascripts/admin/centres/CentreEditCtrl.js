/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CentreEditCtrl.$inject = [
    '$state',
    'domainEntityService',
    'notificationsService',
    'centre',
  ];

  /**
   *
   */
  function CentreEditCtrl($state,
                          domainEntityService,
                          notificationsService,
                          centre) {
    var vm = this;

    vm.title =  'Add study';
    vm.centre = centre;
    vm.submit = submit;
    vm.cancel = cancel;
    vm.returnState = {options: { reload: true } };
    vm.returnState.name = 'home.admin.centres';
    vm.returnState.params = { };

    //---

    function gotoReturnState(state) {
      $state.go(state.name, state.params, state.options);
    }

    function submit(centre) {
      centre.add()
        .then(submitSuccess)
        .catch(submitError);

      function submitSuccess() {
        notificationsService.submitSuccess();
        gotoReturnState(vm.returnState);
      }

      function submitError(error) {
        domainEntityService.updateErrorModal(error, 'centre');
      }
    }

    function cancel() {
      gotoReturnState(_.extend({}, vm.returnState, { options:{ reload: false } }));
    }
  }

  return CentreEditCtrl;
});
