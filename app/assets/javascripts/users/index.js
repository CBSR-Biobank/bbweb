/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  './authorizationService',
  './ForgotPasswordCtrl',
  './LoginCtrl',
  './PasswordSentCtrl',
  './passwordCheckDirective',
  './RegisterUserCtrl',
  './states',
  './usersService',
  './UserProfileCtrl',
], function() {});
