/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.membershipsPagedList
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { PagedListController } from '../../../../../common/controllers/PagedListController'

/*
 * Controller for this component.
 */
class MembershipsPagedListController extends PagedListController {

  constructor($log,
              $scope,
              $state,
              Membership,
              gettextCatalog,
              NameFilter,
              resourceErrorService) {
    'ngInject'
    super($log,
          $state,
          gettextCatalog,
          resourceErrorService,
          { nameFilter: new NameFilter() },
          [ { id: 'name',  labelFunc: () => gettextCatalog.getString('Name') } ],
          5)

    Object.assign(this,
                  {
                    $scope,
                    Membership,
                    NameFilter,
                    resourceErrorService
                  })
  }

  $onInit() {
    this.counts = { total: 1 }
    super.$onInit()
  }

  getItems(options) {
    return this.Membership.list(options)
  }

  getItemIcon() {
    return 'glyphicon-eye-open'
  }

}

/**
 * An AngularJS component that displays {@link domain.access.Membership Memberships} in a panel list.
 *
 * The list of memberships can be filtered and sorted by different fields. The memberships are displayed in a
 * paged fashion, allowing the user to page through all the memberships in the system.
 *
 * @memberOf admin.users.components.membershipsPagedList
 */
const membershipsPagedListComponent = {
  template: require('./membershipsPagedList.html'),
  controller: MembershipsPagedListController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('membershipsPagedList', membershipsPagedListComponent)
