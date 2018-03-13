/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class ResourceNotFoundController {
}

/**
 * An AngularJS component that displays an error message on the page.
 *
 * @memberOf home.components
 *
 * @param {string} errMessage - the message to display on the page.
 */
const resourceNotFoundComponent = {
  template: require('./resourceNotFound.html'),
  controller: ResourceNotFoundController,
  controllerAs: 'vm',
  bindings: {
    errMessage: '@'
  }
};

export default ngModule => ngModule.component('resourceNotFound', resourceNotFoundComponent)
