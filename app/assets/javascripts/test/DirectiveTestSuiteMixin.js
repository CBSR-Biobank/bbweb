/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
export default function DirectiveTestSuiteMixin(ComponentTestSuiteMixin) {

  return _.extend({},
                  ComponentTestSuiteMixin,
                  {
                    createController: createController
                  });

  function createController(htmlElement, scopeVars) {
    ComponentTestSuiteMixin.createController.call(this, htmlElement, scopeVars, undefined);
    this.controller = this.element.scope().vm;
  }

}
