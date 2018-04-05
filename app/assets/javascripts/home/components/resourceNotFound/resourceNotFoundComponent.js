/**
 * AngularJS Component used in the home page.
 *
 * @namespace home.components.resourceNotFound
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS component that displays an error message on the page.
 *
 * @memberOf home.components.resourceNotFound
 *
 * @param {string} errMessage - the message to display on the page.
 */
const resourceNotFoundComponent = {
  template: require('./resourceNotFound.html'),
  controllerAs: 'vm',
  bindings: {
    errMessage: '@'
  }
};

export default ngModule => ngModule.component('resourceNotFound', resourceNotFoundComponent)
