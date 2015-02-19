/**
 * Common tests for the Panel object.
 */
define(
  'biobank.panelTests',
  [
    'underscore',
    'faker',
    'moment',
    'biobank.testUtils'
  ],
  function(_, faker, moment, utils) {

    var panelTests = {
      information: information,
      addItem: addItem,
      panelInitialState: panelInitialState
    };

    return panelTests;

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

  }

);
