/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function statusLineDirective() {
    var directive = {
      restrict: 'E',
      replace: true,
      scope: {
        item: '=',
        showStatus: '='
      },
      templateUrl : '/assets/javascripts/common/directives/statusLine/statusLine.html'
    };
    return directive;
  }

  return statusLineDirective;
});
