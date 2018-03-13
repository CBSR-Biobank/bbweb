/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.rolesPagedList
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { PagedListController } from '../../../../../common/controllers/PagedListController'

/*
 * Controller for this component.
 */
class RolesPagedListController extends PagedListController {

  constructor($log,
              $scope,
              $state,
              Role,
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
                    Role,
                    NameFilter,
                    resourceErrorService
                  })
  }

  $onInit() {
    this.counts = { total: 1 }
    super.$onInit()
  }

  getItems(options) {
    return this.Role.list(options)
  }

  getItemIcon() {
    return 'glyphicon-file'
  }

}

/**
 * An AngularJS component that displays {@link domain.access.Role Roles} in a panel list.
 *
 * The list of roles can be filtered and sorted by different fields. The roles are displayed in a
 * paged fashion, allowing the user to page through all the roles in the system.
 *
 * @memberOf admin.users.components.rolesPagedList
 */
const rolesPagedListComponent = {
  template: require('./rolesPagedList.html'),
  controller: RolesPagedListController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('rolesPagedList', rolesPagedListComponent)
