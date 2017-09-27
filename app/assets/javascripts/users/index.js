/**
 * User configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import CommonModule from '../common';
import angular      from 'angular';

const UsersModule = angular.module('biobank.users', [ CommonModule ])
      .config(require('./states'))

      .component('forgotPassword', require('./components/forgotPassword/forgotPasswordComponent'))
      .component('login',          require('./components/login/loginComponent'))
      .component('passwordSent',   require('./components/passwordSent/passwordSentComponent'))
      .component('registerUser',   require('./components/registerUser/registerUserComponent'))

      .directive('passwordCheck',  require('./directives/passwordCheck/passwordCheckDirective'))

      .service('usersService',          require('./usersService'))
      .service('userStateLabelService', require('./userStateLabelService'))
      .name;

export default UsersModule;
