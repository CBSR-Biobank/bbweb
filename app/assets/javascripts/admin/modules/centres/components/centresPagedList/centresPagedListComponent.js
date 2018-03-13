/**
 * AngularJS Component for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.components.centresPagedList
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
import { PagedListController } from '../../../../../common/controllers/PagedListController';
import _ from 'lodash';

/*
 * Controller for this component.
 */
class Controller extends PagedListController {

  constructor($q,
              $log,
              $state,
              Centre,
              CentreState,
              CentreCounts,
              gettextCatalog,
              NameFilter,
              StateFilter,
              centreStateLabelService,
              resourceErrorService) {
    'ngInject';

    const stateData = _.values(CentreState).map((state) => ({
      id: state,
      label: centreStateLabelService.stateToLabelFunc(state)
    }));

    super($log,
          $state,
          gettextCatalog,
          resourceErrorService,
          {
            nameFilter: new NameFilter(),
            stateFilter: new StateFilter(stateData, 'all', true)
          },
          [
            { id: 'name',  labelFunc: () => gettextCatalog.getString('Name') },
            { id: 'state', labelFunc: () => gettextCatalog.getString('State') }
          ],
          5);

    Object.assign(this,
                  {
                    $q,
                    Centre,
                    CentreState,
                    CentreCounts,
                    NameFilter,
                    StateFilter,
                    centreStateLabelService,
                    resourceErrorService
                  });

    this.stateLabelFuncs = {};
    _.values(this.CentreState).forEach((state) => {
      this.stateLabelFuncs[state] = this.centreStateLabelService.stateToLabelFunc(state);
    });
  }

  $onInit() {
    this.counts = {};

    this.CentreCounts.get()
      .then((counts) => {
        this.counts = counts;
        super.$onInit();
      })
      .catch(this.resourceErrorService.checkUnauthorized());
  }

  getItems(options) {
    return this.CentreCounts.get()
      .then((counts) => {
        this.counts = counts;
        return this.Centre.list(options);
      });
  }

  getItemIcon(centre) {
    if (centre.isDisabled()) {
      return 'glyphicon-cog';
    }
    if (centre.isEnabled()) {
      return 'glyphicon-ok-circle';
    }
    throw new Error('invalid centre state: ' + centre.state);
  }
}

/**
 * An AngularJS component that displays {@link domain.centres.Centre Centres} in a panel list.
 *
 * The list of centres can be filtered and sorted by different fields. The centres are displayed in a paged
 * fashion, allowing the user to page through all the centres in the system.
 *
 * @memberOf admin.centres.components.centresPagedList
 */
const centresPagedListComponent = {
  template: require('./centresPagedList.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('centresPagedList', centresPagedListComponent)
