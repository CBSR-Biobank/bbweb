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

    vm.stateParams = {};
    if (study.isNew()) {
      action = 'Add';
      vm.returnState = { name: 'home.admin.studies' };
    } else {
      action = 'Update';
      vm.returnState = {
        name: 'home.admin.studies.study.summary',
        params: { studyId: study.id },
        options: { reload: true }
      };
    }

    vm.title =  action + ' study';

    //--

    function gotoReturnState() {
      $state.go.apply(null, _.values(vm.returnState));
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submitError(error) {
      // ensure state params has all 3 values
      var stateParams = _.defaults(vm.returnState, {
        params:   {},
        options:  { reload: true }
      });
      var params = [error, study].concat(_.values(stateParams));
      domainEntityUpdateError.handleError.call(null, params);
    }

    function submit(study) {
      studiesService.addOrUpdate(study)
        .then(submitSuccess)
        .catch(submitError);
    }

    function cancel() {
      gotoReturnState();
    }
  }

  return StudyEditCtrl;
});
