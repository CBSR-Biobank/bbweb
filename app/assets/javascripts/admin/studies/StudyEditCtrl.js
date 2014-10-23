/** Study service */
define(['../module'], function(module) {
  'use strict';

  /**
   * Called to edit or update a study.
   */
  module.controller('StudyEditCtrl', StudyEditCtrl);

  StudyEditCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'stateHelper',
    'studiesService',
    'domainEntityUpdateError',
    'study'
  ];

  function StudyEditCtrl($scope,
                         $state,
                         $stateParams,
                         stateHelper,
                         studiesService,
                         domainEntityUpdateError,
                         study) {
    var vm = this;
    var action = (study.id) ? 'Update' : 'Add';
    vm.returnState = study.id ? 'admin.studies.study.summary' : 'admin.studies';
    vm.title =  action + ' study';
    vm.study = study;
    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function gotoReturnState() {
      stateHelper.reloadStateAndReinit(vm.returnState, $stateParams, {reload: true});
    }

    function submit(study) {
      studiesService.addOrUpdate(study)
        .then(gotoReturnState)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'study',
            vm.returnState,
            $stateParams,
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }
  }

});
