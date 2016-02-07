/**
 * User configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var angular                = require('angular'),
      authorizationService   = require('./authorizationService'),
      ForgotPasswordCtrl     = require('./ForgotPasswordCtrl'),
      LoginCtrl              = require('./LoginCtrl'),
      PasswordSentCtrl       = require('./PasswordSentCtrl'),
      passwordCheckDirective = require('./directives/passwordCheck/passwordCheckDirective'),
      RegisterUserCtrl       = require('./RegisterUserCtrl'),
      states                 = require('./states'),
      usersService           = require('./usersService'),
      UserProfileCtrl        = require('./UserProfileCtrl');

  var module = angular.module('biobank.users', ['biobank.common']);

  module.provider('authorization',        authorizationService);
  module.controller('ForgotPasswordCtrl', ForgotPasswordCtrl);
  module.controller('LoginCtrl',          LoginCtrl);
  module.controller('PasswordSentCtrl',   PasswordSentCtrl);
  module.directive('passwordCheck',       passwordCheckDirective);
  module.controller('RegisterUserCtrl',   RegisterUserCtrl);

  module.config(states);

  module.service('usersService', usersService);

  module.controller('UserProfileCtrl', UserProfileCtrl);

  return module;
});
