define(['angular'], function(angular) {
  'use strict';

  /**
   *
   */
  function statusLineDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        item: '='
      },
      templateUrl : '/assets/javascripts/admin/statusLine.html',
      replace: true
    };
    return directive;
  }

  return statusLineDirective;
});
