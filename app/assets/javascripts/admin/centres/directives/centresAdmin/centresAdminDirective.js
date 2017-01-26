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
      templateUrl : '/assets/javascripts/admin/centres/directives/centresAdmin/centresAdmin.html'
    };

    return directive;
  }

  return centresAdminDirective;
});
