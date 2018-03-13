/**
 * Domain model for filtering entity states.
 *
 * @namespace domain.filters.stateFilters
 */

import _ from 'lodash'

/* @ngInject */
function StateFilterFactory(SearchFilter,
                            gettextCatalog) {

  /**
   * StateFilter's aid in using the search API provided by the Biobank REST API.
   *
   * @memberOf domain.filters.stateFilters
   */
  class StateFilter extends SearchFilter {

    /**
     * @param {Array<domain.filters.StateChoice>} choices - the choices available to choose from.
     *
     * @param {string} defaultSelection - the ID of the state to select by default.
     *
     * @param {boolean} allAllowed - Set this to TRUE to allow selection of all choices.
     */
    constructor(choices, defaultSelection, allAllowed) {
      super(StateFilter.name);
      this.allAllowed = allAllowed;
      this.choices = choices;
      if (this.allAllowed) {
        this.choices.unshift({ id: 'all', label: function () { return gettextCatalog.getString('All'); } });
      }
      if (!_.isNil(defaultSelection)) {
        this.setValue(defaultSelection);
      }
    }

    setValue(value) {
      var found = _.find(this.choices, function (choice) {
        return choice.id === value;
      });
      if (_.isUndefined(found)) {
        throw new Error('state filter not valid: ' + value);
      }
      SearchFilter.prototype.setValue.call(this, value);
    }

    getValue() {
      if (this.value !== 'all') {
        return 'state::' + this.value;
      }
      return '';
    }

    allChoices() {
      return this.choices;
    }

    clearValue() {
      this.setValue('all');
    }

  }

  return StateFilter;
}

/**
 * @typedef domain.filters.StateChoice
 * @type object
 *
 * @property {string} id - the ID for the state.
 *
 * @property {function} label - a function that returns the translated string that can be displayed to the
 * user for this state.
 */

export default ngModule => ngModule.factory('StateFilter', StateFilterFactory)
