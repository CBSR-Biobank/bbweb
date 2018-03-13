/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function focusMeDirectiveFactory($timeout) {

  /**
   * An AngularJS Factory for focusing on an input in an HTML Form.
   *
   * The HTML5 autofocus property can be finicky when it comes to dynamically loaded templates and such with
   * AngularJS. Use this simple directive to tame this beast once and for all.
   *
   * @memberOf common.directives
   *
   * @example
   * <input type="text" focus-me="true">
   */
  class FocusMeDirective {

    constructor() {
      this.restrict = 'A';
      this.scope = { focusMe : '@' };
    }

    link(scope, element) {
      scope.$watch('focusMe', (value) => {
        if(value === 'true') {
          $timeout(() => {
            element[0].focus();
          });
        }
      });
    }

  }

  return new FocusMeDirective();
}

export default ngModule => ngModule.directive('focusMe', focusMeDirectiveFactory)
