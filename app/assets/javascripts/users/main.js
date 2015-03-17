/**
 * User package module.
 */
define([
  'angular',
  './authorizationService',
  './ForgotPasswordCtrl',
  './LoginCtrl',
  './PasswordSentCtrl',
  './passwordCheckDirective',
  './RegisterUserCtrl',
  './states',
  './usersService',
  './UserProfileCtrl',
], function(angular,
            authorizationService,
            ForgotPasswordCtrl,
            LoginCtrl,
            PasswordSentCtrl,
            passwordCheckDirective,
            RegisterUserCtrl,
            states,
            usersService,
            UserProfileCtrl) {
  'use strict';

  var module = angular.module('biobank.users', ['ngCookies', 'biobank.common']);

  module.provider('authorization', authorizationService);
  module.controller('ForgotPasswordCtrl', ForgotPasswordCtrl);
  module.controller('LoginCtrl', LoginCtrl);
  module.controller('PasswordSentCtrl', PasswordSentCtrl);
  module.directive('passwordCheck', passwordCheckDirective);
  module.controller('RegisterUserCtrl', RegisterUserCtrl);

  module.config(states);

  module.service('usersService', usersService);

  module.controller('UserProfileCtrl', UserProfileCtrl);

  return module;
});
