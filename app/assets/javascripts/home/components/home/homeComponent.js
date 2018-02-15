/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class HomeController {

  constructor($rootScope, userService, breadcrumbService) {
    'ngInject'
    Object.assign(this, { $rootScope, userService, breadcrumbService })
  }

  $onInit() {
    this.breadcrumbs = [ this.breadcrumbService.forState('home') ];

    this.userIsAuthenticated = false;
    this.$rootScope.pageTitle = 'Biobank';

    this.userService.requestCurrentUser()
      .then((user) => {
        this.user = user;
        if (user !== undefined) {
          this.userIsAuthenticated = true;
          this.allowCollection = this.user.hasSpecimenCollectorRole();
          this.shippingAllowed = this.user.hasShippingUserRole();
          this.adminAllowed = this.user.hasAdminRole();
          this.hasRoles = this.allowCollection || this.shippingAllowed || this.adminAllowed;
        }
      })
      .catch(() => {
        this.user = null;
      });
  }

}

/**
 * @class home
 *
 * An AngularJS component for the home page.
 *
 * @memberOf ng.home.components
 */
const home = {
  template: require('./home.html'),
  controller: HomeController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('home', home)
