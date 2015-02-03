define(['./module'], function(module) {
  'use strict';

  module.directive('statusLine', statusLineDirective);

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

});
