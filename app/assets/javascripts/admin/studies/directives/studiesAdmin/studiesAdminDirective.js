/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
      templateUrl : '/assets/javascripts/admin/studies/directives/studiesAdmin/studiesAdmin.html'
    };

    return directive;
  }

  return studiesAdminDirective;
});
