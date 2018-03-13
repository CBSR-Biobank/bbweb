/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

const  COMPONENT = {
  template: require('./passwordSent.html'),
  controller: PasswordSentController,
  controllerAs: 'vm',
  bindings: {
    email: '<'
  }
};

/*
 * Controller for this component.
 */
function PasswordSentController() {
}

export default ngModule => ngModule.component('passwordSent', COMPONENT)
