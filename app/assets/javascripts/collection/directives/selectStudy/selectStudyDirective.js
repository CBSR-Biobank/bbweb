/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * This directive allows the user to select a {@link Study} from the list provided by function getStudies().
   */
  function selectStudyDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        getHeader:              '&',
        getStudies:             '&',
        limit:               '=',
        messageNoResults:       '@',
        icon:                   '@',
        navigateStateName:      '@',
        navigateStateParamName: '@'
      },
      templateUrl : '/assets/javascripts/collection/directives/selectStudy/selectStudy.html',
      controller: SelectStudyCtr,
      controllerAs: 'vm'
    };

    return directive;
  }

  SelectStudyCtr.$inject = [
    '$state',
    'gettextCatalog',
    'modalService',
    'filterExpression'
  ];

  function SelectStudyCtr($state, gettextCatalog, modalService, filterExpression) {
    var vm = this;

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

    //---

    function getDisplayState() {
      return (vm.pagedResult.total > 0) ? vm.displayStates.HAVE_RESULTS : vm.displayStates.NO_RESULTS;
    }

    function updateStudies() {
      var filterElements = [
            { key: 'name',  value: vm.nameFilterWildcard },
            { key: 'state', value: 'enabled' }
          ];

      _.extend(vm.pagerOptions, { filter: filterExpression.create(filterElements) });

      vm.getStudies()(vm.pagerOptions).then(function (pagedResult) {
        vm.pagedResult = pagedResult;
        vm.displayState = getDisplayState();
        vm.showPagination = getShowPagination();
      });
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      if (!_.isUndefined(vm.nameFilter) && (vm.nameFilter !== '')) {
        vm.nameFilterWildcard = '*' + vm.nameFilter + '*';
      } else {
        vm.nameFilterWildcard = '';
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

    function studyGlyphicon(study) {
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

  return selectStudyDirective;
});
