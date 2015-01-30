define(['./module'], function(module) {
  'use strict';

  module.controller('HeaderCtrl', HeaderCtrl);

  HeaderCtrl.$inject = ['$scope', '$state', 'usersService'];

  /**
   * Controller for the page header. Contains the navigation bar.
   */
  function HeaderCtrl($scope, $state, usersService) {
    var vm = this;
    vm.logout = logout;
    vm.user = undefined;

    // Wrap the current user from the service in a watch expression to display the user's name in
    // the navigation bar
    $scope.$watch(function() {
      var user = usersService.getCurrentUser();
      return user;
    }, function(user) {
      vm.user = user;
    }, true);

    //---

    function logout() {
      usersService.logout();
      $scope.user = undefined;
      $state.go('home');
    }
  }

});
