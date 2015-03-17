/** Study service */
define([], function() {
  'use strict';

  StudyEditCtrl.$inject = [
    'stateHelper',
    'studiesService',
    'notificationsService',
    'domainEntityUpdateError',
    'study'
  ];

  /**
   * Adds or updates a study.
   */
  function StudyEditCtrl(stateHelper,
                         studiesService,
                         notificationsService,
                         domainEntityUpdateError,
                         study) {
    var vm = this;
    var action;
    vm.study = study;
    vm.submit = submit;
    vm.cancel = cancel;

    vm.stateParams = {};
    if (study.id) {
      action = 'Update';
      vm.returnState = 'home.admin.studies.study.summary';
      vm.stateParams.studyId = study.id;
    } else {
      action = 'Add';
      vm.returnState = 'home.admin.studies';
    }

    vm.title =  action + ' study';

    //--

    function gotoReturnState() {
      stateHelper.reloadStateAndReinit(vm.returnState, vm.stateParams, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(study) {
      studiesService.addOrUpdate(study)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'study',
            vm.returnState,
            vm.stateParams,
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }
  }

  return StudyEditCtrl;
});
