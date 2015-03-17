/**
 * Common tests for the Panel object.
 */
define([
  'underscore',
  'faker',
  'moment'
], function(_, faker, moment) {
  'use strict';

  var panelTests = {
    information: information,
    addItem: addItem,
    panelInitialState: panelInitialState
  };

  function information(scope, modal, item) {
    spyOn(modal, 'open').and.callThrough();
    scope.vm.information(item);
    expect(modal.open).toHaveBeenCalled();
  }

  function addItem(scope, Panel) {
    spyOn(Panel.prototype, 'add').and.callThrough();
    scope.vm.add();
    expect(Panel.prototype.add).toHaveBeenCalled();
  }

  function panelInitialState(scope) {
    expect(scope.vm.panelOpen).toEqual(true);
  }

  return panelTests;

});
