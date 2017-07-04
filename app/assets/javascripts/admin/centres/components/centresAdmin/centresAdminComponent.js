/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/centres/components/centresAdmin/centresAdmin.html',
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

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.centres')
    ];
  }

  return component;
});
