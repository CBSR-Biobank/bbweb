/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import angular from 'angular';

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @exports test.mixins.ComponentTestSuiteMixin
 */
let ComponentTestSuiteMixin = {

  /**
   * Used to inject AngularJS dependencies into the test suite.
   *
   * Also injects dependencies required by this mixin.
   *
   * @param {...string} dependencies - the AngularJS dependencies to inject.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    const allDependencies = dependencies.concat([ '$rootScope', '$compile' ]);
    TestSuiteMixin.injectDependencies.call(this, ...allDependencies);
  },

  createScope: function (scopeVars) {
    var scope = this.$rootScope.$new();
    if (scopeVars) {
      scope.vm = scopeVars;
    }
    return scope;
  },

  createController: function (htmlElement, scopeVars, controllerName) {
    this.element = angular.element(htmlElement);
    this.scope = this.createScope(scopeVars);
    this.$compile(this.element)(this.scope);
    this.scope.$digest();
    if (controllerName) {
      this.controller = this.element.controller(controllerName);
    }
  }

}

ComponentTestSuiteMixin = Object.assign({},
                                        TestSuiteMixin,
                                        ComponentTestSuiteMixin);

export { ComponentTestSuiteMixin };
export default () => {};
