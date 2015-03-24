/** Study service */
define(['underscore'], function(_) {
  'use strict';

  StudyEditCtrl.$inject = [
    '$state',
    'studiesService',
    'notificationsService',
    'domainEntityUpdateError',
    'study'
  ];

  /**
   * Adds or updates a study.
   */
  function StudyEditCtrl($state,
                         studiesService,
                         notificationsService,
                         domainEntityUpdateError,
                         study) {
    var vm = this,
        action;

    vm.study = study;
    vm.submit = submit;
    vm.cancel = cancel;
    vm.returnState = {};

    vm.returnState = {options: { reload: true } };

    if (study.isNew()) {
      action = 'Add';
      vm.returnState.name = 'home.admin.studies';
      vm.returnState.params = { };
    } else {
      action = 'Update';
      vm.returnState.name = 'home.admin.studies.study.summary';
      vm.returnState.params = { studyId: study.id };
    }

    vm.title =  action + ' study';

    //--

    function gotoReturnState(state) {
      $state.go(state.name, state.params, state.options);
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState(vm.returnState);
    }

    function submitError(error) {
      domainEntityUpdateError.handleErrorNoStateChange(error, 'study');
    }

    function submit(study) {
      studiesService.addOrUpdate(study)
        .then(submitSuccess)
        .catch(submitError);
    }

    function cancel() {
      gotoReturnState(_.extend({}, vm.returnState, { options:{ reload: false } }));
    }
  }

  return StudyEditCtrl;
});
