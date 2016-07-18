/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/selectCentre/selectCentre.html',
    controller: SelectCentreController,
    controllerAs: 'vm',
    bindings: {
      panelHeader:      '@',
      getCentres:       '&',
      onCentreSelected: '&',
      pageSize:         '<',
      messageNoResults: '@',
      icon:             '@'

    }
  };

  SelectCentreController.$inject = [];

  /**
   * Allows a user to select a centre based on criteria.
   */
  function SelectCentreController() {
    var vm = this;

    vm.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1
    };

    vm.updateCentres     = updateCentres;
    vm.pagedResult       = {};
    vm.nameFilterUpdated = nameFilterUpdated;
    vm.pageChanged       = pageChanged;
    vm.clearFilter       = clearFilter;
    vm.displayState      = getDisplayState();
    vm.centreGlyphicon   = centreGlyphicon;
    vm.showPagination    = getShowPagination();

    vm.pagerOptions = {
      filter:    '',
      status:    'EnabledCentre',
      page:      1,
      pageSize:  vm.pageSize,
      sortField: 'name' // must be lower case
    };

    updateCentres();

    //--

    function getDisplayState() {
      return (vm.pagedResult.total > 0) ? vm.displayStates.HAVE_RESULTS : vm.displayStates.NO_RESULTS;
    }

    function updateCentres() {
      vm.getCentres()(vm.pagerOptions).then(function (pagedResult) {
        vm.pagedResult = pagedResult;
        vm.displayState = getDisplayState();
        vm.showPagination = getShowPagination();
      });
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      vm.pagerOptions.page = 1;
      updateCentres();
    }

    function pageChanged() {
      updateCentres();
    }

    function clearFilter() {
      vm.pagerOptions.filter = null;
      updateCentres();
    }

    function centreGlyphicon(study) {
      return '<i class="glyphicon ' + vm.icon + '"></i>';
    }

    function getShowPagination() {
      return (vm.displayState === vm.displayStates.HAVE_RESULTS) &&
        (vm.pagedResult.maxPages > 1);
    }

  }

  return component;
});
