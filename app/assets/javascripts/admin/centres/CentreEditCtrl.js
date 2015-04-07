define([], function() {
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
    var vm = this, action;

    vm.centre = centre;
    vm.submit = submit;
    vm.cancel = cancel;
    vm.returnState = {options: { reload: true } };

    if (centre.isNew()) {
      action = 'Add';
      vm.returnState.name = 'home.admin.centres';
      vm.returnState.params = { };
    } else {
      action = 'Update';
      vm.returnState.name = 'home.admin.centres.centre.summary';
      vm.returnState.params = { centreId: centre.id };
    }

    vm.title =  action + ' study';

    //---

    function gotoReturnState(state) {
      $state.go(state.name, state.params, state.options);
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState(vm.returnState);
    }

    function submitError(error) {
      domainEntityService.updateErrorModal(error, 'centre');
    }

    function submit(centre) {
      centre.addOrUpdate()
        .then(submitSuccess)
        .catch(submitError);
    }

    function cancel() {
      gotoReturnState(_.extend({}, vm.returnState, { options:{ reload: false } }));
    }
  }

  return CentreEditCtrl;
});
