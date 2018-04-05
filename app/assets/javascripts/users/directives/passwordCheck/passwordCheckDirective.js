/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Code originally borrowed from:
 *
 * https://github.com/forbode/forbode-angularjs-pwCheck
 *
 * http://rogeralsing.com/2013/08/26/angularjs-directive-to-check-that-passwords-match-followup/
 *
 * https://egghead.io/lessons/angularjs-using-ngmodel-in-custom-directives
 */

function passwordCheckDirectiveFactory() {

  /**
   * An AnglarJS Directive that confirms that the values in passwords fields match.
   *
   * Used in an HTML form where the user has to enter a password and a *confirm password* values.
   *
   * @memberOf users.directives
   */
  class PasswordCheckDirective {

    constructor() {
      this.restrict = 'A';
      this.require = 'ngModel';
    }

    link(scope, elem, attrs, ctrl) {
      const checkMatching = () => {
        const confirmPassword = ctrl.$viewValue;
        const password = scope.$eval(attrs.passwordCheck);
        return (confirmPassword === password);
      };

      scope.$watch(checkMatching,
                   (match) => {
                     ctrl.$setValidity('passwordmatch', match);
                   });
    }
  }

  return new PasswordCheckDirective();
}

export default ngModule => ngModule.directive('passwordCheck', passwordCheckDirectiveFactory)
