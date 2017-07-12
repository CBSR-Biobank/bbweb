/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/studies/components/studyAdd/studyAdd.html',
    controller: StudyAddController,
    controllerAs: 'vm',
    bindings: {
      study: '<'
    }
  };

  var returnState = 'home.admin.studies';

  StudyAddController.$inject = [
    '$state',
    'gettextCatalog',
    'notificationsService',
    'domainNotificationService'
  ];

  /*
   * Controller for this component.
   */
  function StudyAddController($state,
                              gettextCatalog,
                              notificationsService,
                              domainNotificationService) {

    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.submit = submit;
      vm.cancel = cancel;
    }

    function submit(study) {
      study.add()
        .then(submitSuccess)
        .catch(submitError);

      function submitSuccess() {
        notificationsService.submitSuccess();
        $state.go(returnState, {}, { reload: true });
      }

      function submitError(error) {
        domainNotificationService.updateErrorModal(error, gettextCatalog.getString('study'));
      }
    }

    function cancel() {
      $state.go(returnState);
    }
  }

  return component;
});
