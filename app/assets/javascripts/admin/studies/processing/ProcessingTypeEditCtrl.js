define(['../../module'], function(module) {
  'use strict';

  module.controller('ProcessingTypeEditCtrl', ProcessingTypeEditCtrl);

  ProcessingTypeEditCtrl.$inject = [
    '$state',
    '$stateParams',
    'stateHelper',
    'domainEntityUpdateError',
    'ProcessingTypeService',
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
                                  ProcessingTypeService,
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
      return $state.go('admin.studies.study.processing', {}, {reload: true});
    }

    function submit(processingType) {
      ProcessingTypeService.addOrUpdate(processingType)
        .then(gotoReturnState)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'processing type',
            'admin.studies.study.processing',
            {studyId: study.id},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

});
