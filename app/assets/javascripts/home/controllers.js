/**
 * Home controllers.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('home.controllers', ['user.services']);

  // For debugging
  //
  mod.run(['$rootScope', '$state', '$stateParams', function ($rootScope, $state, $stateParams) {
    // $rootScope.$state = $state;
    // $rootScope.$stateParams = $stateParams;

    $rootScope.$on('$stateChangeError',function(event, toState, toParams, fromState, fromParams){
      console.log('$stateChangeError - fired when an error occurs during transition.');
      console.log(arguments);
    });

    // $rootScope.$on('$stateChangeStart',function(event, toState, toParams, fromState, fromParams){
    //   console.log('$stateChangeStart to '+toState.to+'- fired when the transition begins. toState,toParams : \n',toState, toParams);
    // });
    // $rootScope.$on('$stateChangeSuccess',function(event, toState, toParams, fromState, fromParams){
    //   console.log('$stateChangeSuccess to '+toState.name+'- fired once the state transition is complete.');
    // });
    // // $rootScope.$on('$viewContentLoading',function(event, viewConfig){
    // //   // runs on individual scopes, so putting it in "run" doesn't work.
    // //   console.log('$viewContentLoading - view begins loading - dom not rendered',viewConfig);
    // // });
    // $rootScope.$on('$viewContentLoaded',function(event){
    //   console.log('$viewContentLoaded - fired after dom rendered',event);
    // });
    // $rootScope.$on('$stateNotFound',function(event, unfoundState, fromState, fromParams){
    //   console.log('$stateNotFound '+unfoundState.to+'  - fired when a state cannot be found by its name.');
    //   console.log(unfoundState, fromState, fromParams);
    // });
  }]);

  /** Controls the index page */
  mod.controller('HomeCtrl', ['$scope', '$rootScope', function($scope, $rootScope) {
    $rootScope.pageTitle = 'Biobank';
  }]);

  /** Controls the header */
  mod.controller('HeaderCtrl', ['$scope', '$state', 'userService', function($scope, $state, userService) {
    // Wrap the current user from the service in a watch expression
    $scope.$watch(function() {
      var user = userService.getUser();
      return user;
    }, function(user) {
      $scope.user = user;
    }, true);

    $scope.logout = function() {
      userService.logout();
      $scope.user = undefined;
      $state.go('home');
    };
  }]);

  /** Controls the footer */
  mod.controller('FooterCtrl', function(/*$scope*/) {
  });

});
