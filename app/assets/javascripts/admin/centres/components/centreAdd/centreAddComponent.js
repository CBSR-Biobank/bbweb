/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/admin/centres/components/centreAdd/centreAdd.html',
    controller: CentreAddDirective,
    controllerAs: 'vm',
    bindings: {
    }
  };

  CentreAddDirective.$inject = [
    '$state',
    'gettextCatalog',
    'Centre',
    'domainNotificationService',
    'notificationsService'
  ];

  /*
   * Controller for this component.
   */
  function CentreAddDirective($state,
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

  return component;
});
