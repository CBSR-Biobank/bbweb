/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
import PagedListController from '../../../../common/controllers/PagedListController';
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
              centreStateLabelService) {
    'ngInject';

    const stateData = _.values(CentreState).map((state) => ({
      id: state,
      label: centreStateLabelService.stateToLabelFunc(state)
    }));

    super($log,
          $state,
          gettextCatalog,
          {
            nameFilter: new NameFilter(),
            stateFilter: new StateFilter(true, stateData, 'all')
          },
          5);

    Object.assign(this,
                  {
                    $q,
                    Centre,
                    CentreState,
                    CentreCounts,
                    NameFilter,
                    StateFilter,
                    centreStateLabelService
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
      .catch((error) => {
        this.$log.error(error);
      });
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
 * Displays items in a panel list. Can only be used for collections {@link domain.study.Study} and {@link
 * domain.cnetres.Centres}.
 *
 * @return {object} An AngularJS directive.
 */
const component = {
  template: require('./centresPagedList.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
};

export default component;
