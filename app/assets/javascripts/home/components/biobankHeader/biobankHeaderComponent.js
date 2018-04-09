/**
 * AngularJS Component used in the home page.
 *
 * @namespace home.components.biobankHeader
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
class BiobankHeaderController {

  constructor($scope,
              $state,
              $log,
              languageService,
              userService) {
    'ngInject';
    Object.assign(this,
                  {
                    $scope,
                    $state,
                    $log,
                    languageService,
                    userService
                  });
  }

  $onInit() {
    this.navCollapsed = true;
    this.user = undefined;

    // Wrap the current user from the service in a watch expression to display the user's name in
    // the navigation bar
    this.$scope
      .$watch(
        () => {
          const user = this.userService.getCurrentUser();
          return user;
        },
        (user) => {
          this.user = user;
        },
        true);
  }

  logout() {
    const goHome = () => {
      this.$state.go('home', {}, { reload: true });
    };

    this.userService.logout()
      .then(goHome)
      .catch((error) => {
        this.$log.error('logout failed:', error);
        goHome();
      });
  }

  changeLanguage(lang) {
    this.languageService.setLanguage(lang);
  }
}

/**
 * An AngularJS component for the page header.
 *
 * @memberOf home.components.biobankHeader
 */
const biobankHeaderComponent = {
  template: require('./biobankHeader.html'),
  controller: BiobankHeaderController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('biobankHeader', biobankHeaderComponent)
