/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

class HomeController {

  constructor($rootScope, userService, breadcrumbService) {
    Object.assign(this, { $rootScope, userService, breadcrumbService })
  }

  $onInit() {
    this.breadcrumbs = [ this.breadcrumbService.forState('home') ];

    this.userIsAuthenticated = false;
    this.$rootScope.pageTitle = 'Biobank';

    this.userService.requestCurrentUser()
      .then((user) => {
        this.user = user;
        this.userIsAuthenticated = true;
        this.allowCollection = this.user.hasSpecimenCollectorRole();
        this.shippingAllowed = this.user.hasShippingUserRole();
        this.adminAllowed = this.user.hasAdminRole();
      })
      .catch(() => {
        this.user = null;
      });
  }

}

const component = {
  template: require('./home.html'),
  controller: HomeController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('home', component)
