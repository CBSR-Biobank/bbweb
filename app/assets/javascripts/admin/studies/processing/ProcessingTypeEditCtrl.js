define([], function() {
  'use strict';

  ProcessingTypeEditCtrl.$inject = [
    '$state',
    'domainEntityUpdateError',
    'processingTypesService',
    'notificationsService',
    'processingType'
  ];

  /**
   *
   */
  function ProcessingTypeEditCtrl($state,
                                  domainEntityUpdateError,
                                  processingTypesService,
                                  notificationsService,
                                  processingType) {

    var action = (processingType.id) ? 'Update' : 'Add';
    var vm = this;
    vm.title =  action  + ' Processing Type';
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
          domainEntityUpdateError.handleErrorNoStateChange(
            error, 'processing type');
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

  return ProcessingTypeEditCtrl;
});
