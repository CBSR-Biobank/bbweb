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
    'gettextCatalog',
    'notificationsService',
    'domainNotificationService'
  ];

  var returnState = 'home.admin.studies';

  function StudyAddCtrl($state,
                        gettextCatalog,
                        notificationsService,
                        domainNotificationService) {

    var vm = this;

    vm.submit = submit;
    vm.cancel = cancel;

    //--

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

  return studyAddDirective;

});
