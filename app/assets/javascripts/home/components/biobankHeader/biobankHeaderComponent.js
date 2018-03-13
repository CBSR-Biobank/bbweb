/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function BiobankHeaderController($scope,
                                 $state,
                                 $log,
                                 languageService,
                                 userService) {
  var vm = this;
  vm.$onInit = onInit;
  this.navCollapsed = true;

  //--

  function onInit() {
    vm.navCollapsed = true;
    vm.logout = logout;
    vm.user = undefined;
    vm.changeLanguage = changeLanguage;

    // Wrap the current user from the service in a watch expression to display the user's name in
    // the navigation bar
    $scope.$watch(function() {
      var user = userService.getCurrentUser();
      return user;
    }, function(user) {
      vm.user = user;
    }, true);
  }

  function logout() {
    userService.logout().then(goHome).catch(goHomeError);

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


/**
 * An AngularJS component for the page header.
 *
 * @memberOf home.components
 */
const biobankHeaderComponent = {
  template: require('./biobankHeader.html'),
  controller: BiobankHeaderController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('biobankHeader', biobankHeaderComponent)
