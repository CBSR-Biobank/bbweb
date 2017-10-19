/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./centresAdmin.html'),
    controller: CentresAdminController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  CentresAdminController.$inject = ['breadcrumbService'];

  /*
   * Controller for this component.
   */
  function CentresAdminController(breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    //---

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.centres')
      ];
    }
  }

  return component;
});
