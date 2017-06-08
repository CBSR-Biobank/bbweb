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

  module.config(require('./states'));

  module.component('forgotPassword', require('./components/forgotPassword/forgotPasswordComponent'));
  module.component('registerUser',   require('./components/registerUser/registerUserComponent'));
  module.component('userProfile',    require('./components/userProfile/userProfileComponent'));

  module.directive('login',         require('./directives/login/loginDirective'));
  module.directive('passwordCheck', require('./directives/passwordCheck/passwordCheckDirective'));
  module.directive('passwordSent',  require('./directives/passwordSent/passwordSentDirective'));
  module.directive('passwordCheck', require('./directives/passwordCheck/passwordCheckDirective'));

  module.service('usersService',    require('./usersService'));

  return {
    name: name,
    module: module
  };
});
