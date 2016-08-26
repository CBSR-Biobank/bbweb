/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   *
   */
  function studyAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studyAdd/studyAdd.html',
      controller: StudyAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyAddCtrl.$inject = [
    '$state',
    'gettext',
    'notificationsService',
    'domainNotificationService'
  ];

  function StudyAddCtrl($state,
                        gettext,
                        notificationsService,
                        domainNotificationService) {

    var vm = this;

    vm.title =  'Add study';
    vm.submit = submit;
    vm.cancel = cancel;
    vm.returnState = 'home.admin.studies';

    //--

    function submit(study) {
      study.add()
        .then(submitSuccess)
        .catch(submitError);

      function submitSuccess() {
        notificationsService.submitSuccess();
        $state.go(vm.returnState, {}, { reload: true });
      }

      function submitError(error) {
        domainNotificationService.updateErrorModal(error, gettext('study'));
      }
    }

    function cancel() {
      $state.go(vm.returnState);
    }
  }

  return studyAddDirective;

});
