/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular');

  ComponentTestSuiteMixinFactory.$inject = ['TestSuiteMixin'];

  function ComponentTestSuiteMixinFactory(TestSuiteMixin) {

    function ComponentTestSuiteMixin() {
      TestSuiteMixin.call(this);
    }

    ComponentTestSuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    ComponentTestSuiteMixin.prototype.constructor = ComponentTestSuiteMixin;

    ComponentTestSuiteMixin.prototype.createScope = function (scopeVars) {
      var scope = this.$rootScope.$new();
      if (scopeVars) {
        scope.vm = scopeVars;
      }
      return scope;
    };

    ComponentTestSuiteMixin.prototype.createController = function (htmlElement, scopeVars, controllerName) {
      this.element = angular.element(htmlElement);
      this.scope = this.createScope(scopeVars);
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller(controllerName);
    };

    return ComponentTestSuiteMixin;
  }

  return ComponentTestSuiteMixinFactory;

});
