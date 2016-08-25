/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   *
   */
  function ceventTypeAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/ceventTypeAdd/ceventTypeAdd.html',
      controller: CeventTypeAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CeventTypeAddCtrl.$inject = [
    '$state',
    'CollectionEventType',
    'domainNotificationService',
    'notificationsService'
  ];

  function CeventTypeAddCtrl($state,
                             CollectionEventType,
                             domainNotificationService,
                             notificationsService) {
    var vm = this;

    vm.ceventType  = new CollectionEventType({ studyId: vm.study.id });
    vm.returnState = 'home.admin.studies.study.collection';

    vm.title       = 'Add Collection Event';
    vm.submit      = submit;
    vm.cancel      = cancel;

    //---

    function submit(ceventType) {
      ceventType.add().then(submitSuccess).catch(submitError);

      function submitSuccess() {
        notificationsService.submitSuccess();
        return $state.go(vm.returnState, {}, { reload: true });
      }

      function submitError(error) {
        domainNotificationService.updateErrorModal(error, 'collection event type');
      }
    }

    function cancel() {
      return $state.go(vm.returnState);
    }

  }

  return ceventTypeAddDirective;

});
