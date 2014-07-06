/**
 * Administration controllers.
 *
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.controllers', []);

  mod.controller('AdminCtrl', [
    '$scope', 'userService',
    function($scope, userService) {
  }]);

  return mod;

});
