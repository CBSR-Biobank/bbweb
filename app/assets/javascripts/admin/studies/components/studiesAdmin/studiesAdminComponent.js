/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/studies/components/studiesAdmin/studiesAdmin.html',
    controller: StudiesAdminController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  StudiesAdminController.$inject = ['breadcrumbService'];

  /*
   * Controller for this component.
   */
  function StudiesAdminController(breadcrumbService) {
    var vm = this;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies')
    ];
  }

  return component;
});
