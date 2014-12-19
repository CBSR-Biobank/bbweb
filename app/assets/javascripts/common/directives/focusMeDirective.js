define(['../module'], function(module) {
  'use strict';

  module.directive('focusMe', focusMe);

  focusMe.$inject = ['$timeout'];

  /**
   * the HTML5 autofocus property can be finicky when it comes to dynamically loaded templates and such with
   * AngularJS. Use this simple directive to tame this beast once and for all.
   *
   * Usage:
   * <input type="text" autofocus>
   *
   */
  function focusMe($timeout) {
    var directive = {
      restrict: 'A',
      scope : {
        focusMe : '@'
      },
      link: link
    };
    return directive;

    function link(scope, element) {
      scope.$watch('focusMe', function(value) {
        if(value === 'true') {
          $timeout(function() {
            element[0].focus();
          });
        }
      });
    }
  }

});
