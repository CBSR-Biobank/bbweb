define([], function() {
  'use strict';

  ProcessingTypeEditCtrl.$inject = [
    '$state',
    'domainEntityService',
    'notificationsService',
    'processingType'
  ];

  /**
   *
   */
  function ProcessingTypeEditCtrl($state,
                                  domainEntityService,
                                  notificationsService,
                                  processingType) {

    var vm = this;

    vm.title =  (processingType.isNew ? 'Add' : 'Update')  + ' Processing Type';
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
      processingType.addOrUpdate()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error, 'processing type');
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

  return ProcessingTypeEditCtrl;
});
