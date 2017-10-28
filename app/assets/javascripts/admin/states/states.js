/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Configures routes for the administration module.
 */

/* @ngInject */
function config($stateProvider) {

  $stateProvider.state('home.admin', {
    // this state is checked for an authorized user, see uiRouterIsAuthorized() in app.js
    url: 'admin',
    views: {
      'main@': 'biobankAdmin'
    }
  });

}

export default ngModule => ngModule.config(config)
