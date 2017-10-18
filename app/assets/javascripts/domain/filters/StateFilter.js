/**
 *
 */

import _ from 'lodash'

/* @ngInject */
function StateFilterFactory(SearchFilter,
                            gettextCatalog) {

  /**
   * StateFilter's aid in using the search API provided by the Biobank REST API.
   *
   * @param {boolean} allAllowed - Set this to TRUE to allow selection of all states. An additional
   */
  function StateFilter(allAllowed, choices, defaultSelection) {
    SearchFilter.call(this, StateFilter.name);
    this.allAllowed = allAllowed;
    this.choices = choices;
    if (this.allAllowed) {
      this.choices.unshift({ id: 'all', label: function () { return gettextCatalog.getString('All'); } });
    }
    if (!_.isNil(defaultSelection)) {
      this.setValue(defaultSelection);
    }
  }

  StateFilter.prototype = Object.create(SearchFilter.prototype);
  StateFilter.prototype.constructor = StateFilter;

  StateFilter.prototype.setValue = function (value) {
    var found = _.find(this.choices, function (choice) {
      return choice.id === value;
    });
    if (_.isUndefined(found)) {
      throw new Error('state filter not valid: ' + value);
    }
    SearchFilter.prototype.setValue.call(this, value);
  };

  StateFilter.prototype.getValue = function () {
    if (this.value !== 'all') {
      return 'state::' + this.value;
    }
    return '';
  };

  StateFilter.prototype.allChoices = function () {
    return this.choices;
  };

  StateFilter.prototype.clearValue = function () {
    this.setValue('all');
  };

  return StateFilter;
}

export default ngModule => ngModule.factory('StateFilter', StateFilterFactory)
