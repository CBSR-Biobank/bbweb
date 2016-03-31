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

  module.provider('authorization',        require('./authorizationService'));

  module.controller('ForgotPasswordCtrl', require('./ForgotPasswordCtrl'));
  module.controller('RegisterUserCtrl',   require('./RegisterUserCtrl'));

  module.directive('login',         require('./directives/login/loginDirective'));
  module.directive('passwordCheck', require('./directives/passwordCheck/passwordCheckDirective'));
  module.directive('userProfile',   require('./directives/userProfile/userProfileDirective'));
  module.directive('passwordSent',  require('./directives/passwordSent/passwordSentDirective'));
  module.directive('passwordCheck', require('./directives/passwordCheck/passwordCheckDirective'));

  module.service('usersService',          require('./usersService'));


  return {
    name: name,
    module: module
  };
});
