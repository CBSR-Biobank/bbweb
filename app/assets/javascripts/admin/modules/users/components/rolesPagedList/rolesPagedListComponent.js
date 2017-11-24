/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { PagedListController } from '../../../../../common/controllers/PagedListController'

/*
 * Controller for this component.
 */
class Controller extends PagedListController {

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
      .catch(this.resourceErrorService.checkUnauthorized())
  }

  getItemIcon() {
    return 'glyphicon-file'
  }

}

/**
 * Displays studies in a panel list.
 *
 * @return {object} An AngularJS component.
 */
const component = {
  template: require('./rolesPagedList.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
}

export default ngModule => ngModule.component('rolesPagedList', component)
