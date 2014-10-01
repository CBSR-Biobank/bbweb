define(['./module'], function(module) {
  'use strict';

  module.controller('HeaderCtrl', HeaderCtrl);

  HeaderCtrl.$inject = ['$scope', '$state', 'userService'];

  /**
   * Controller for the page header. Contains the navigation bar.
   */
  function HeaderCtrl($scope, $state, userService) {
    var vm = this;
    vm.logout = logout;
    vm.user = undefined;

    // Wrap the current user from the service in a watch expression to display the user's name in
    // the navigation bar
    $scope.$watch(function() {
      var user = userService.getUser();
      return user;
    }, function(user) {
      vm.user = user;
    }, true);

    //---

    function logout() {
      userService.logout();
      $scope.user = undefined;
      $state.go('home');
    }
  }

});
