/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/home/components/biobankHeader/biobankHeader.html',
    controller: BiobankHeaderController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  BiobankHeaderController.$inject = [
    '$scope',
    '$state',
    '$log',
    'languageService',
    'usersService'
  ];

  /*
   * Controller for this component.
   */
  function BiobankHeaderController($scope,
                                   $state,
                                   $log,
                                   languageService,
                                   usersService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.navCollapsed = true;
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
    }

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

  return component;
});
