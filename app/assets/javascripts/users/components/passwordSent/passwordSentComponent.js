/**
 * AngularJS Component for user login.
 *
 * @namespace users.components.passwordSent
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS component that confirms that the user's password has been reset.
 *
 * @memberOf users.components.passwordSent
 *
 * @param {string} email - the email address for the user.
 */
const passwordSentComponent = {
  template: require('./passwordSent.html'),
  controllerAs: 'vm',
  bindings: {
    email: '<'
  }
};

export default ngModule => ngModule.component('passwordSent', passwordSentComponent)
