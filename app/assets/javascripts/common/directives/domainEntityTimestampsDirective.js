define(['../module'], function(module) {
  'use strict';

  module.directive('domainEntityTimestamps', domainEntityTimestampsDirective);

  /**
   *
   */
  function domainEntityTimestampsDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        entity: '='
      },
      replace: true,
      templateUrl : '/assets/javascripts/common/directives/domainEntityTimestamps.html'
    };
    return directive;

  }

});
