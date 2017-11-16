/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { PagedListController } from '../../../../../common/controllers/PagedListController';
import _ from 'lodash';

/*
 * Controller for this component.
 */
class Controller extends PagedListController {

  constructor($log,
              $state,
              Study,
              StudyState,
              StudyCounts,
              gettextCatalog,
              NameFilter,
              StateFilter,
              studyStateLabelService,
              resourceErrorService) {
    'ngInject';

    const stateData = _.values(StudyState).map((state) => ({
      id: state,
      label: studyStateLabelService.stateToLabelFunc(state)
    }));

    super($log,
          $state,
          gettextCatalog,
          {
            nameFilter: new NameFilter(),
            stateFilter: new StateFilter(true, stateData, 'all')
          },
          [
            { id: 'name',  labelFunc: () => gettextCatalog.getString('Name') },
            { id: 'state', labelFunc: () => gettextCatalog.getString('State') }
          ],
          5);

    Object.assign(this,
                  {
                    Study,
                    StudyState,
                    StudyCounts,
                    NameFilter,
                    StateFilter,
                    studyStateLabelService,
                    resourceErrorService
                  });

    this.stateLabelFuncs = {};
    _.values(this.StudyState).forEach((state) => {
      this.stateLabelFuncs[state] = this.studyStateLabelService.stateToLabelFunc(state);
    });
  }

  $onInit() {
    this.counts = {};

    return this.StudyCounts.get()
      .then((counts) => {
        this.counts = counts;
        super.$onInit();
      })
      .catch(this.resourceErrorService.checkUnauthorized());
  }

  getItems(options) {
    // KLUDGE: for now, fix after Entity Pagers have been implemented
    return this.StudyCounts.get()
      .then((counts) => {
        this.counts = counts;
        return this.Study.list(options);
      });
  }

  getItemIcon(study) {
    if (study.isDisabled()) {
      return 'glyphicon-cog';
    }
    if (study.isEnabled()) {
      return 'glyphicon-ok-circle';
    }
    if (study.isRetired()) {
      return 'glyphicon-remove-sign';
    }
    throw new Error('invalid study state: ' + study.state);
  }
}

/**
 * Displays studies in a panel list.
 *
 * @return {object} An AngularJS component.
 */
const component = {
  template: require('./studiesPagedList.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('studiesPagedList', component)
