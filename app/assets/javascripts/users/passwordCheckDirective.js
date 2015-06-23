/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  /**
   * Code originally borrowed from:
   *
   * https://github.com/forbode/forbode-angularjs-pwCheck
   *
   * http://codepen.io/brunoscopelliti/pen/ECyka
   *
   * http://rogeralsing.com/2013/08/26/angularjs-directive-to-check-that-passwords-match-followup/
   *
   * https://egghead.io/lessons/angularjs-using-ngmodel-in-custom-directives
   */
  function passwordCheckDirectiveFactory() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, elem, attrs, ngModel) {
        var checkMatching = function() {
          var confirmPassword = ngModel.$viewValue;
          var password = scope.$eval(attrs.passwordCheck);
          return (confirmPassword === password);
        };

        scope.$watch(checkMatching, function (match) {
          ngModel.$setValidity('passwordmatch', match);
        });
      }
    };
  }

  return passwordCheckDirectiveFactory;
});
