/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  StudyEditCtrl.$inject = [
    '$state',
    'notificationsService',
    'domainEntityService',
    'study'
  ];

  /**
   * Adds or updates a study.
   */
  function StudyEditCtrl($state,
                         notificationsService,
                         domainEntityService,
                         study) {
    var vm = this,
        action;

    vm.study = study;
    vm.submit = submit;
    vm.cancel = cancel;
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
      domainEntityService.updateErrorModal(error, 'study');
    }

    function submit(study) {
      study.add()
        .then(submitSuccess)
        .catch(submitError);
    }

    function cancel() {
      gotoReturnState(_.extend({}, vm.returnState, { options:{ reload: false } }));
    }
  }

  return StudyEditCtrl;
});
