/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  /*
   *
   */
  function centresAdminDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      templateUrl : '/assets/javascripts/admin/centres/directives/centresAdmin/centresAdmin.html',
      controller: CentresAdminCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentresAdminCtrl.$inject = ['breadcrumbService'];

  function CentresAdminCtrl(breadcrumbService) {
    var vm = this;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.centres')
    ];
  }

  return centresAdminDirective;
});
