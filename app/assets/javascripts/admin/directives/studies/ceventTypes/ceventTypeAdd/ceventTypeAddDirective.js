/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
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
      templateUrl : '/assets/javascripts/admin/directives/studies/ceventTypes/ceventTypeAdd/ceventTypeAdd.html',
      controller: CeventTypeAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CeventTypeAddCtrl.$inject = [
    '$state',
    'CollectionEventType',
    'domainEntityService',
    'notificationsService'
  ];

  function CeventTypeAddCtrl($state,
                             CollectionEventType,
                             domainEntityService,
                             notificationsService) {
    var vm = this;

    vm.ceventType  = new CollectionEventType({ studyId: vm.study.id });
    vm.returnState = 'home.admin.studies.study.collection';

    vm.title       = 'Add Collection Event Type';
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
        domainEntityService.updateErrorModal(error, 'collection event type');
      }
    }

    function cancel() {
      return $state.go(vm.returnState);
    }

  }

  return ceventTypeAddDirective;

});
