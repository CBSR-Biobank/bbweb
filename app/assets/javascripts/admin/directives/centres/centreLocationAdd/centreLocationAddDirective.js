/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function centreLocationAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        centre: '='
      },
      templateUrl : '/assets/javascripts/admin/directives/centres/centreLocationAdd/centreLocationAdd.html',
      controller: CentreLocationAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreLocationAddCtrl.$inject = [
    '$state',
    'domainEntityService',
    'notificationsService'
  ];

  function CentreLocationAddCtrl($state, domainEntityService, notificationsService) {
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
        $state.go(vm.returnStateName, {}, {reload: true});
      }

      function submitError(error) {
        return domainEntityService.updateErrorModal(error, 'location');
      }
    }

    function cancel() {
      $state.go(vm.returnStateName);
    }

  }

  return centreLocationAddDirective;
});
