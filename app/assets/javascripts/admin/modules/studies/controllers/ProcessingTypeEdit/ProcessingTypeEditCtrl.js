/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 *
 */
/* @ngInject */
function ProcessingTypeEditCtrl($state,                          // eslint-disable-line no-unused-vars
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

// TEMP: don't add this controller for now
export default () => {}
