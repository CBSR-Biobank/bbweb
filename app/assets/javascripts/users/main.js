/**
 * User configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.users',
      module;

  module = angular.module(name, [ 'biobank.common' ]);

  module
    .config(require('./states'))

    .component('forgotPassword', require('./components/forgotPassword/forgotPasswordComponent'))
    .component('login',          require('./components/login/loginComponent'))
    .component('passwordSent',   require('./components/passwordSent/passwordSentComponent'))
    .component('registerUser',   require('./components/registerUser/registerUserComponent'))

    .directive('passwordCheck', require('./directives/passwordCheck/passwordCheckDirective'))

    .service('usersService',   require('./usersService'))
    .service('userStateLabel', require('./userStateLabelService'));

  return {
    name: name,
    module: module
  };
});
