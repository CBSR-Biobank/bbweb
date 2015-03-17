define([], function(){
  'use strict';

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

  return domainEntityTimestampsDirective;
});
