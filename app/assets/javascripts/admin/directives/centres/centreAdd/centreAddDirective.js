/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  /**
   *
   */
  function centreAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        centre: '='
      },
      templateUrl : '/assets/javascripts/admin/directives/centres/centreAdd/centreAdd.html',
      controller: CentreAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreAddCtrl.$inject = [
    '$state',
    'domainEntityService',
    'notificationsService'
  ];

  function CentreAddCtrl($state,
                          domainEntityService,
                          notificationsService,
                          centre) {
    var vm = this;

    vm.title =  'Add centre';
    vm.submit = submit;
    vm.cancel = cancel;
    vm.returnState = {
      name: 'home.admin.centres',
      params: { },
      options: { reload: true }
    };

    //---

    function gotoReturnState(state) {
      $state.go(state.name, state.params, state.options);
    }

    function submit(centre) {
      centre.add()
        .then(submitSuccess)
        .catch(submitError);

      function submitSuccess() {
        notificationsService.submitSuccess();
        gotoReturnState(vm.returnState);
      }

      function submitError(error) {
        domainEntityService.updateErrorModal(error, 'centre');
      }
    }

    function cancel() {
      gotoReturnState(_.extend({}, vm.returnState, { options:{ reload: false } }));
    }
  }

  return centreAddDirective;
});
