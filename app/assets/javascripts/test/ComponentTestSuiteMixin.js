/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';
import angular from 'angular';

/* @ngInject */
export default function ComponentTestSuiteMixin(TestSuiteMixin, $rootScope, $compile) {

  return _.extend(
    {
      createScope: createScope,
      createController: createController
    },
    TestSuiteMixin);

  function createScope(scopeVars) {
    var scope = $rootScope.$new();
    if (scopeVars) {
      scope.vm = scopeVars;
    }
    return scope;
  }

  function createController(htmlElement, scopeVars, controllerName) {
    this.element = angular.element(htmlElement);
    this.scope = this.createScope(scopeVars);
    $compile(this.element)(this.scope);
    this.scope.$digest();
    if (controllerName) {
      this.controller = this.element.controller(controllerName);
    }
  }

}
