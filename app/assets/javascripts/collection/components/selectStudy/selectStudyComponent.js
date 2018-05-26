/**
 * AngularJS Component for {@link domain.participants.Specimen Specimen} collection.
 *
 * @namespace collection.components.selectStudy
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
function SelectStudyController($state, gettextCatalog, modalService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1
    };

    vm.nameFilter        = '';
    vm.updateStudies     = updateStudies;
    vm.studyNames        = [];
    vm.nameFilterUpdated = nameFilterUpdated;
    vm.pageChanged       = pageChanged;
    vm.clearFilter       = clearFilter;
    vm.displayState      = getDisplayState();
    vm.studyGlyphicon    = studyGlyphicon;
    vm.studySelected     = studySelected;
    vm.showPagination    = getShowPagination();

    vm.pagerOptions = {
      filter: '',
      sort:   'name', // must be lower case
      page:   1,
      limit:  vm.limit
    };

    updateStudies();
  }

  function getDisplayState() {
    return (vm.studyNames.length > 0) ? vm.displayStates.HAVE_RESULTS : vm.displayStates.NO_RESULTS;
  }

  function updateStudies() {
    if (vm.pagerOptions.filter) {
      vm.pagerOptions.filter += ';';
    } else {
      vm.pagerOptions.filter = '';
    }

    vm.pagerOptions.filter += 'state::enabled';
    vm.getStudies()(vm.pagerOptions)
      .then(studyNames => {
        vm.studyNames = studyNames;
        vm.displayState = getDisplayState();
        vm.showPagination = getShowPagination();
      });
  }

  /*
   * Called when user enters text into the 'name filter'.
   */
  function nameFilterUpdated() {
    if (!_.isUndefined(vm.nameFilter) && (vm.nameFilter !== '')) {
      vm.pagerOptions.filter = 'name:like:' + vm.nameFilter;
    } else {
      vm.pagerOptions.filter = '';
    }
    vm.pagerOptions.page = 1;
    updateStudies();
  }

  function pageChanged() {
    updateStudies();
  }

  function clearFilter() {
    vm.nameFilter = '';
    vm.nameFilterWildcard = '';
    vm.pagerOptions.filter = null;
    updateStudies();
  }

  function studyGlyphicon() {
    return '<i class="glyphicon ' + vm.icon + '"></i>';
  }

  function studySelected(study) {
    study.allLocations().then(function (reply) {
      if ((reply.length > 0) && vm.onStudySelected()) {
        vm.onStudySelected()(study);
      } else {
        modalService.modalOk(
          gettextCatalog.getString('Centre Configuration'),
          gettextCatalog.getString('There are no centres configured to participate in this study.' +
                                   '<p>Please configure centres for this study.'));
      }
    });
  }

  function getShowPagination() {
    return (vm.displayState === vm.displayStates.HAVE_RESULTS) && (vm.studyNames.length > 1);
  }
}

/**
 * An AngularJS component that allows the user to select a {@link domain.studies.Study Study} from the list
 * provided by function `getStudies()`.
 *
 * A Bootstrap *Panel* is used to display the studies.
 *
 * @memberOf collection.components.selectStudy
 *
 * @param {string} header - the string to display in the panel's header.
 *
 * @param {collection.components.selectStudy.GetStudies} getStudies - the function to use to retrieve
 * studies.
 *
 * @param {int} limit - the default value to use in parameter `options.limit` when invoking `getStudies`.
 *
 * @param {string} messageNoResults - the message to display if `getStudies` returns an empty result.
 *
 * @param {string} icon - the name of the Bootstrap Glyphicon to display for the studies.
 *
 * @param {collection.components.selectStudy.StudySelected} onStudySelected - the function called by this
 * component when the user has selected a study.
  */
const selectStudyComponent = {
  template: require('./selectStudy.html'),
  controller: SelectStudyController,
  controllerAs: 'vm',
  bindings: {
    header:           '@',
    getStudies:       '&',
    limit:            '<',
    messageNoResults: '@',
    icon:             '@',
    onStudySelected:  '&'
  }
};

/**
 * The callback function used by {@link collection.components.selectStudy.selectStudyComponent
 * selectStudyComponent} to retrieve the list of {@link domain.studies.Study Studies}.
 *
 * @callback collection.components.selectStudy.GetStudies
 *
 * @param {common.controllers.PagedListController.PagerOptions} options - the pager options used to retrieve
 * studies.
 *
 * @returns {Promise<Array<domain.studies.StudyName>>} A promise with items of type {@link
 * domain.studies.StudyName StudyName}.
 */

/**
 * The callback function used by {@link collection.components.selectStudy.selectStudyComponent
 * selectStudyComponent} when the user selects a {@link domain.studies.Study Study}.
 *
 * @callback collection.components.selectStudy.StudySelected
 *
 * @param {domain.studies.StudyName} study - the study selected by the user.
 *
 * @returns {undefined}
 */

export default ngModule => ngModule.component('selectStudy', selectStudyComponent)
