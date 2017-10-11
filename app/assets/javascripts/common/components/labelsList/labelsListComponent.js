/**
 *
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * @typedef common.components.labelList.LabelInfo
   * @type object
   *
   * @property {String} label - the label to display to the user
   *
   * @property {string} tooltip - the tooltip text to display for the label.
   *
   * @property {object} obj - the object to return when a label is selected.
   */

  /**
   * Displays a list of strings using Bootstrap labels.
   *
   * @param {Array<common.components.labelList.LabelInfo>} labelData - the label data.
   *
   * @param {string} labelClass - the class to use to display the strings. The following can be used:
   *        label-default, label-primary, label-success, label-info, label-warning, or label-danger. If no
   *        class is specified, then 'label-primary' is used.
   *
   * @param {function} onLabelSelected - the function to invoke when a label is selected.
   */
  var component = {
    template: require('./labelsList.html'),
    controller: LabelsListController,
    controllerAs: 'vm',
    bindings: {
      labelData:       '<',
      labelClass:      '@',
      onLabelSelected: '&'
    }
  };

  //LabelsListController.$inject = [];

  var DefaultLabel = 'label-info';

  /*
   *
   */
  function LabelsListController() {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      if (_.isUndefined(vm.class)) {
        vm.labelClass = DefaultLabel;
      }
      vm.removeLabel = removeLabel;
    }

    function removeLabel(label) {
      if (vm.onLabelSelected()) {
        vm.onLabelSelected()(label.obj);
      }
    }

  }

  return component;
});
