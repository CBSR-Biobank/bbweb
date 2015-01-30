define(['../../module'], function(module) {
  'use strict';

  module.controller('ProcessingTypeEditCtrl', ProcessingTypeEditCtrl);

  ProcessingTypeEditCtrl.$inject = [
    '$state',
    '$stateParams',
    'stateHelper',
    'domainEntityUpdateError',
    'processingTypesService',
    'notificationsService',
    'study',
    'processingType'
  ];

  /**
   *
   */
  function ProcessingTypeEditCtrl($state,
                                  $stateParams,
                                  stateHelper,
                                  domainEntityUpdateError,
                                  processingTypesService,
                                  notificationsService,
                                  study,
                                  processingType) {

    var action = (processingType.id) ? 'Update' : 'Add';
    var vm = this;
    vm.title =  action  + ' Processing Type';
    vm.study = study;
    vm.processingType = processingType;
    vm.submit = submit;
    vm.cancel = cancel;

    //---

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.processing', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(processingType) {
      processingTypesService.addOrUpdate(processingType)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'processing type',
            'home.admin.studies.study.processing',
            {studyId: study.id},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

});
