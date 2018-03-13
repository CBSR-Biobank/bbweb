/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studiesPagedList
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { PagedListController } from '../../../../../common/controllers/PagedListController';
import _ from 'lodash';

/*
 * Controller for this component.
 */
class StudiesPagedListController extends PagedListController {

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
 * An AngularJS component that displays {@link domain.studies.Study Studies} in a panel list.
 *
 * The list of studies can be filtered and sorted by different fields. The studies are displayed in a paged
 * fashion, allowing the user to page through all the studies in the system.
 *
 * @memberOf admin.studies.components.studiesPagedList
 */
const studiesPagedListComponent = {
  template: require('./studiesPagedList.html'),
  controller: StudiesPagedListController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('studiesPagedList', studiesPagedListComponent)
