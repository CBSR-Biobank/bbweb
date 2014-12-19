/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  './ForgotPasswordCtrl',
  './LoginCtrl',
  './PasswordSentCtrl',
  './passwordCheckDirective',
  './RegisterUserCtrl',
  './states',
  './userResolveConstant',
  './usersService',
  './UserProfileCtrl',
], function() {});
