/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * Callback for component {@link centres.components.shipmentStateSelector shipmentStateSelector}.
   *
   * @callback onSelectionCallback
   * @param {domain.centres.ShipmentState[]} states - The currently selected states.
   */

  /**
   * @class centres.components.shipmentStateSelector
   * @memberof centres.components
   *
   * @description An AngularJS Component that allows the user to select one or more {@link
   * domain.centres.ShipmentState ShipmentState}s using checkboxes.
   *
   * @param {domain.centres.ShipmentState[]} selectedStates - The initial states that should be selected.
   *
   * @param {onSelectionCallback} onSelection - The function that is called when the user
   *        selects or deselects a state.
   */
  var component = {
    templateUrl: '/assets/javascripts/centres/components/shipmentStatesSelector/shipmentStatesSelector.html',
    controller: ShipmentStatesSelectorController,
    controllerAs: 'vm',
    bindings: {
      selectedStates: '<',
      onSelection:    '&'
    }
  };

  ShipmentStatesSelectorController.$inject = [
    'ShipmentState',
    'shipmentStateLabelService'
  ];

  /*
   * Controller for this component.
   */
  function ShipmentStatesSelectorController(ShipmentState,
                                            shipmentStateLabelService) {
    var vm = this;

    vm.selectedStates = vm.selectedStates || [];
    vm.states = _.map(_.values(ShipmentState), function (key) {
      return {
        id: key,
        label: shipmentStateLabelService.stateToLabel(key),
        checked: vm.selectedStates.includes(key)
      };
    });

    vm.selectionChanged = selectionChanged;
    vm.checkAll = checkAll;
    vm.checkFromStates = checkFromStates;
    vm.checkToStates = checkToStates;

    //--

    function selectionChanged() {
      var result = _(vm.states)
          .filter(function (state) { return state.checked; })
          .map(function (state) { return state.id; })
          .value();
      vm.onSelection()(result);
    }

    function checkAll(checked) {
      _.each(vm.states, function (state) {
        state.checked = checked;
      });
      selectionChanged();
    }

    function checkFromStates() {
      _.each(vm.states, function (state) {
        state.checked = [
          ShipmentState.CREATED,
          ShipmentState.PACKED,
          ShipmentState.SENT,
        ].includes(state.id);
      });
      selectionChanged();
    }

    function checkToStates() {
      _.each(vm.states, function (state) {
        state.checked = [
          ShipmentState.RECEIVED,
          ShipmentState.UNPACKED
        ].includes(state.id);
      });
      selectionChanged();
    }

  }

  return component;
});
