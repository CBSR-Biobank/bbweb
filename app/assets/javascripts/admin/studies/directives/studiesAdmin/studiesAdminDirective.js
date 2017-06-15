/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function() {
  'use strict';

  /**
   * An AngularJS directive that displays a list of studies.
   *
   * @return {object} An AngularJS directive.
   */
  function studiesAdminDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      templateUrl : '/assets/javascripts/admin/studies/directives/studiesAdmin/studiesAdmin.html',
      controller: StudiesAdminCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudiesAdminCtrl.$inject = ['breadcrumbService'];

  function StudiesAdminCtrl(breadcrumbService) {
    var vm = this;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies')
    ];

  }

  return studiesAdminDirective;
});
