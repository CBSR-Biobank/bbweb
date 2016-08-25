/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  ProcessingTypeEditCtrl.$inject = [
    '$state',
    'domainNotificationService',
    'notificationsService',
    'processingType'
  ];

  /**
   *
   */
  function ProcessingTypeEditCtrl($state,
                                  domainNotificationService,
                                  notificationsService,
                                  processingType) {

    var vm = this;

    vm.title =  (processingType.isNew() ? 'Add' : 'Update')  + ' Processing Type';
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
          domainNotificationService.updateErrorModal(
            error, 'processing type');
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

  return ProcessingTypeEditCtrl;
});
