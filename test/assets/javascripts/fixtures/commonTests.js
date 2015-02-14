/**
 * Generates fake domain entities as returned by the server.
 */
define(
  'biobank.commonTests',
  [
    'underscore',
    'faker',
    'moment',
    'biobank.testUtils'
  ],
  function(_, faker, moment, utils) {

    var commonTests = {
      panelTests: {
        information: information,
        addItem: addItem,
        panelInitialState: panelInitialState,
        panelToggle: panelToggle
      }

    };

    return commonTests;

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

    /**
     * Panel's state should be open prior to calling this test.
     */
    function panelToggle(scope, Panel) {
      spyOn(Panel.prototype, 'panelToggle').and.callThrough();
      scope.vm.panelToggle();
      expect(scope.vm.panelOpen).toEqual(false);
      expect(Panel.prototype.panelToggle).toHaveBeenCalled();

      scope.vm.panelToggle();
      expect(scope.vm.panelOpen).toEqual(true);
    }

  }

);
