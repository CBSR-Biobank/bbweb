/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.selectCentre
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component
 */
function SelectCentreController() {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1
    };

    vm.nameFilter         = '';
    vm.nameFilterWildcard = '';
    vm.updateCentres      = updateCentres;
    vm.pagedResult        = {};
    vm.nameFilterUpdated  = nameFilterUpdated;
    vm.pageChanged        = pageChanged;
    vm.clearFilter        = clearFilter;
    vm.displayState       = getDisplayState();
    vm.centreGlyphicon    = centreGlyphicon;
    vm.showPagination     = getShowPagination();

    vm.pagerOptions = {
      filter: '',
      sort:   'name', // must be lower case
      page:   1,
      limit:  vm.limit
    };

    updateCentres();
  }

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
    if (!_.isUndefined(vm.nameFilter) && (vm.nameFilter !== '')) {
      vm.pagerOptions.filter = 'name:like:' + vm.nameFilter;
    } else {
      vm.pagerOptions.filter = '';
    }
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

  function centreGlyphicon() {
    return '<i class="glyphicon ' + vm.icon + '"></i>';
  }

  function getShowPagination() {
    return (vm.displayState === vm.displayStates.HAVE_RESULTS) &&
      (vm.pagedResult.maxPages > 1);
  }

}

/**
 * An AngularJS component that allows a user to select a {@link domain.centres.Centre Centre} from the list
 * provided by function `getCentres()`.
 *
 * A Bootstrap *Panel* is used to display the centres.
 *
 * @memberOf centres.components.selectCentre
 *
 * @param {string} panelHeader - the string to display in the panel's header.
 *
 * @param {centres.components.selectCentre.GetCentres} getCentres - the function to use to retrieve centres.
 *
 * @param {centres.components.selectCentre.CentreSelected} onCentreSelected - the function called by this
 * component when the user has selected a centre.
 *
 * @param {int} limit - the default value to use in parameter `options.limit` when invoking `getCentres`.
 *
 * @param {string} messageNoResults - the message to display if `getCentres` returns an empty result.
 *
 * @param {string} icon - the name of the Bootstrap Glyphicon to display for the centres.
 */
const selectCentreComponent = {
  template: require('./selectCentre.html'),
  controller: SelectCentreController,
  controllerAs: 'vm',
  bindings: {
    panelHeader:      '@',
    getCentres:       '&',
    onCentreSelected: '&',
    limit:            '<',
    messageNoResults: '@',
    icon:             '@'
  }
};

/**
 * The callback function used by {@link centres.components.selectCentre.selectCentreComponent
 * selectCentreComponent} to retrieve {@link domain.centres.Centre Centres}.
 *
 * @callback centres.components.selectCentre.GetCentres
 *
 * @param {common.controllers.PagedListController.PagerOptions} options - the pager options used to retrieve
 * centres.
 *
 * @returns {Promise<common.controllers.PagedListController.PagedResult<domain.centres.Centre>>} A promise
 * with items of type {@link domain.centres.Centre Centre}.
 */

/**
 * The callback function used by {@link centres.components.selectCentre.selectCentreComponent
 * selectCentreComponent} when the user selects a {@link domain.centres.Centre Centre}.
 *
 * @callback centres.components.selectCentre.CentreSelected
 *
 * @param {domain.centres.Centre} centre - the centre selected by the user.
 *
 * @returns {undefined}
 */

export default ngModule => ngModule.component('selectCentre', selectCentreComponent)
