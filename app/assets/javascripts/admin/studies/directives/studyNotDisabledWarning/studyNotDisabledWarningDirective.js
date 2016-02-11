/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function studyNotDisabledWarningDirective() {
    var directive = {
      restrict: 'E',
      templateUrl : '/assets/javascripts/admin/studies/directives/studyNotDisabledWarning/studyNotDisabledWarning.html'
    };
    return directive;
  }

  return studyNotDisabledWarningDirective;

});
