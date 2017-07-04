/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/centres/components/centreLocationAdd/centreLocationAdd.html',
    controller: CentreLocationAddController,
    controllerAs: 'vm',
    bindings: {
      centre: '='
    }
  };

  CentreLocationAddController.$inject = [
    '$state',
    'gettextCatalog',
    'domainNotificationService',
    'notificationsService'
  ];

  /*
   * Controller for this component.
   */
  function CentreLocationAddController($state,
                                       gettextCatalog,
                                       domainNotificationService,
                                       notificationsService) {
    var vm = this;

    vm.submit = submit;
    vm.cancel = cancel;
    vm.returnStateName = 'home.admin.centres.centre.locations';

    //---

    function submit(location) {
      vm.centre.addLocation(location)
        .then(submitSuccess)
        .catch(submitError);

      //--

      function submitSuccess() {
        notificationsService.submitSuccess();
        $state.go(vm.returnStateName, {}, { reload: true });
      }

      function submitError(error) {
        return domainNotificationService.updateErrorModal(error, gettextCatalog.getString('location'));
      }
    }

    function cancel() {
      $state.go(vm.returnStateName);
    }

  }

  return component;
});
