/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * This component allows the user to select a {@link Study} from the list provided by function getStudies().
 */
var component = {
  template: require('./selectStudy.html'),
  controller: SelectStudyController,
  controllerAs: 'vm',
  bindings: {
    getHeader:              '&',
    getStudies:             '&',
    limit:                  '<',
    messageNoResults:       '@',
    icon:                   '@',
    navigateStateName:      '@',
    navigateStateParamName: '@'
  }
};

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
    vm.pagedResult       = {};
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
    return (vm.pagedResult.total > 0) ? vm.displayStates.HAVE_RESULTS : vm.displayStates.NO_RESULTS;
  }

  function updateStudies() {
    if (vm.pagerOptions.filter) {
      vm.pagerOptions.filter += ';';
    } else {
      vm.pagerOptions.filter = '';
    }

    vm.pagerOptions.filter += 'state::enabled';
    vm.getStudies()(vm.pagerOptions).then(function (pagedResult) {
      vm.pagedResult = pagedResult;
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
      var stateParam = {};
      if (reply.length > 0) {
        stateParam[vm.navigateStateParamName] = study.id;
        $state.go(vm.navigateStateName, stateParam);
      } else {
        modalService.modalOk(
          gettextCatalog.getString('Centre Configuration'),
          gettextCatalog.getString('There are no centres configured to participate in this study.' +
                                   '<p>Please configure centres for this study.'));
      }
    });
  }

  function getShowPagination() {
    return (vm.displayState === vm.displayStates.HAVE_RESULTS) &&
      (vm.pagedResult.maxPages > 1);
  }
}

export default ngModule => ngModule.component('selectStudy', component)
