/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @exports test.mixins.DirectiveTestSuiteMixin
 */
let DirectiveTestSuiteMixin = {

  createController: function (htmlElement, scopeVars) {
    ComponentTestSuiteMixin.createController.call(this, htmlElement, scopeVars, undefined);
    this.controller = this.element.scope().vm;
  }

}

DirectiveTestSuiteMixin = Object.assign({},
                                        ComponentTestSuiteMixin,
                                        DirectiveTestSuiteMixin);

export { DirectiveTestSuiteMixin };
export default () => {};
