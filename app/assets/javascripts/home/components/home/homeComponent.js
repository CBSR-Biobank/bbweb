/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

class HomeController {

  constructor($rootScope, usersService, breadcrumbService) {
    this.$rootScope = $rootScope;
    this.usersService = usersService;
    this.breadcrumbService = breadcrumbService;
  }

  $onInit() {
    this.breadcrumbs = [ this.breadcrumbService.forState('home') ];

    this.userIsAuthenticated = false;
    this.$rootScope.pageTitle = 'Biobank';

    this.usersService.requestCurrentUser()
      .then((user) => {
        this.user = user;
        this.userIsAuthenticated = true;
        this.allowCollection = this.user.hasRole('SpecimenCollector');
        this.shippingAllowed = this.user.hasRole('ShippingUser');
        this.adminAllowed = this.user.hasAnyRoleOf('StudyAdministrator',
                                                   'CentreAdministrator',
                                                   'UserAdministrator');
      })
      .catch(() => {
        this.user = null;
      });
  }

}

HomeController.$inject = [ '$rootScope', 'usersService', 'breadcrumbService' ];

const component = {
  template: require('./home.html'),
  controller: HomeController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default component;
