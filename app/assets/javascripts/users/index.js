/**
 * User configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';
import biobankCommon from '../common';

const MODULE_NAME = 'biobank.users';

angular.module(MODULE_NAME, [ biobankCommon ])
  .config(require('./states'))

  .component('forgotPassword', require('./components/forgotPassword/forgotPasswordComponent'))
  .component('login',          require('./components/login/loginComponent'))
  .component('passwordSent',   require('./components/passwordSent/passwordSentComponent'))
  .component('registerUser',   require('./components/registerUser/registerUserComponent'))

  .directive('passwordCheck',  require('./directives/passwordCheck/passwordCheckDirective'))

  .service('usersService',          require('./usersService'))
  .service('userStateLabelService', require('./userStateLabelService'));

export default MODULE_NAME;
