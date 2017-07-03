/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/studies/components/ceventTypeAdd/ceventTypeAdd.html',
    controller: CeventTypeAddController,
    controllerAs: 'vm',
    bindings: {
      study: '='
    }
  };

  CeventTypeAddController.$inject = [
    '$state',
    'gettextCatalog',
    'CollectionEventType',
    'domainNotificationService',
    'notificationsService'
  ];

  /*
   * Controller for this component.
   */
  function CeventTypeAddController($state,
                                   gettextCatalog,
                                   CollectionEventType,
                                   domainNotificationService,
                                   notificationsService) {
    var vm = this;

    vm.ceventType  = new CollectionEventType({}, { study: vm.study });
    vm.returnState = 'home.admin.studies.study.collection';

    vm.title       = gettextCatalog.getString('Add Collection Event');
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
        domainNotificationService.updateErrorModal(error, gettextCatalog.getString('collection event type'));
      }
    }

    function cancel() {
      return $state.go(vm.returnState);
    }

  }

  return component;
});
