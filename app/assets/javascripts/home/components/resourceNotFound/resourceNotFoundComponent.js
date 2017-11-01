/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

class ResourceNotFoundController {

  constructor() {
    'ngInject'
    Object.assign(this, {})
  }

}

/**
 *
 */
var COMPONENT = {
  template: require('./resourceNotFound.html'),
  controller: ResourceNotFoundController,
  controllerAs: 'vm',
  bindings: {
    errMessage: '@'
  }
};

export default ngModule => ngModule.component('resourceNotFound', COMPONENT)
