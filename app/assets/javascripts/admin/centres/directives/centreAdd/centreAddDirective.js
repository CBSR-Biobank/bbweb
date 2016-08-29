/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   *
   */
  function centreAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {},
      templateUrl : '/assets/javascripts/admin/centres/directives/centreAdd/centreAdd.html',
      controller: CentreAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreAddCtrl.$inject = [
    '$state',
    'gettextCatalog',
    'Centre',
    'domainNotificationService',
    'notificationsService'
  ];

  function CentreAddCtrl($state,
                         gettextCatalog,
                         Centre,
                         domainNotificationService,
                         notificationsService) {
    var vm = this;

    vm.centre = new Centre();
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
      centre.add().then(onSubmitSuccess).catch(onSubmitError);
    }

    function onSubmitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState(vm.returnState);
    }

    function onSubmitError(error) {
      domainNotificationService.updateErrorModal(error, gettextCatalog.getString('centre'));
    }

    function cancel() {
      gotoReturnState(_.extend({}, vm.returnState, { options:{ reload: false } }));
    }
  }

  return centreAddDirective;
});
