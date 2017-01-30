/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function biobankHeaderDirective() {
    var directive = {
      restrict: 'E',
      templateUrl: '/assets/javascripts/home/directives/biobankHeader/biobankHeader.html',
      controller: BiobankHeaderCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  BiobankHeaderCtrl.$inject = [
    '$scope',
    '$state',
    '$log',
    'languageService',
    'usersService'
  ];

  function BiobankHeaderCtrl($scope,
                             $state,
                             $log,
                             languageService,
                             usersService) {
    var vm = this;
    vm.logout = logout;
    vm.user = undefined;
    vm.changeLanguage = changeLanguage;

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
      usersService.logout().then(goHome).catch(goHomeError);

      function goHome() {
        $state.go('home', {}, { reload: true });
      }

      function goHomeError(error) {
        $log.error('logout failed:', error);
        goHome();
      }
    }

    function changeLanguage(lang) {
      languageService.setLanguage(lang);
    }
  }

  return biobankHeaderDirective;
});
