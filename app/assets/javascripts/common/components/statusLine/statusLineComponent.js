/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.statusLine
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
function StatusLineController() {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    if (_.isUndefined(vm.useLabels) || vm.useLabels) {
      vm.useLabels = true;

      if (_.isUndefined(vm.class)) {
        vm.class = 'label-default';
      }
    } else if (_.isUndefined(vm.class)) {
      vm.class = 'text-info';
    }

    vm.hasState =!_.isUndefined(vm.stateLabelFunc());

    if (typeof vm.timeAdded === 'string') {
      vm.timeAdded = new Date(vm.timeAdded);
    }
    if (typeof vm.timeModified === 'string') {
      vm.timeModified = new Date(vm.timeModified);
    }
    if (vm.timeAdded.getFullYear() < 2000) {
      // timestamp will display as 'on system initialization'
      vm.timeAdded = undefined;
    }
  }
}

/**
 * An AngularJS Component that displays a {@link domain|Domain Entity's} *status* line in one of two formats:
 * using Bootstrap labels or as a line of text.
 *
 * @memberOf common.components.statusLine
 *
 * @param {function} stateLabelFuc a function that calls gettextCatalog to display the entity's
 *         state. Note that this has to be a function in order to support chaning languages in a
 *         dynamic fashion.
 *
 * @param {Date|string} timeAdded the time the entity was added to the system. If the time is
 *        before the year 2000, then the string 'on system initialization' is displayed. This
 *        value can be a Date object or a string.
 *
 * @param {Date|string} timeModified the time the entity was last modified. If the time is
 *        undefined, then the string 'never' is displayed. This value can be a Date object or a
 *        string.
 *
 * @param {string} class - The class to use to display each of the items listed above. If labels are used,
 *        then one of the following can be used: `label-default`, `label-primary`, `label-success`,
 *        `label-info`, `label-warning`, or `label-danger`. If labels are not used, then one of the following
 *        can be used: `text-muted`, `text-primary`, `text-success`, `text-info`, `text-warning`, or
 *        `text-danger`.
 *
 * @param {boolean} useLabels if `TRUE` then Bootstrap labels are used. Otherwise, a line of text
 *        with property `text-info` is used.
 */
const statusLineComponent = {
  template: require('./statusLine.html'),
  controller: StatusLineController,
  controllerAs: 'vm',
  bindings: {
    stateLabelFunc: '&',
    timeAdded:      '<',
    timeModified:   '<',
    class:          '@',
    useLabels:      '<'
  }
};

export default ngModule => ngModule.component('statusLine', statusLineComponent)
