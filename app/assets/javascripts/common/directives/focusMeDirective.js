/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * the HTML5 autofocus property can be finicky when it comes to dynamically loaded templates and such with
 * AngularJS. Use this simple directive to tame this beast once and for all.
 *
 * Usage:
 * <input type="text" autofocus>
 *
 */
/* @ngInject */
function focusMeDirective($timeout) {
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

export default ngModule => ngModule.directive('focusMe', focusMeDirective)
