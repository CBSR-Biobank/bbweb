/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @return {object} Object containing the functions that will be mixed in.
 */
/* @ngInject */
function DirectiveTestSuiteMixin(ComponentTestSuiteMixin) {

  return Object.assign({}, ComponentTestSuiteMixin, { createController });

  function createController(htmlElement, scopeVars) {
    ComponentTestSuiteMixin.createController.call(this, htmlElement, scopeVars, undefined);
    this.controller = this.element.scope().vm;
  }

}
export default ngModule => ngModule.service('DirectiveTestSuiteMixin', DirectiveTestSuiteMixin)
