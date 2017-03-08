/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var angular         = require('angular'),
      mocks           = require('angularMocks'),
      _               = require('lodash');

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function (selectedStates, onSelection) {
      this.element = angular.element(
        '<shipment-states-selector selected-states="vm.selectedStates" on-selection="vm.onSelection">' +
          '</shipment-states-selector');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { selectedStates: selectedStates, onSelection: onSelection };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('shipmentStatesSelector');
    };

    return SuiteMixin;
  }

  describe('shipmentStatesSelectorComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, testUtils) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentStatesSelector/shipmentStatesSelector.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'ShipmentState');
      testUtils.addCustomMatchers();
    }));

    it('has valid scope', function() {
      var selectedStates = [ this.ShipmentState.CREATED ];
      this.createScope(selectedStates, this.onSelection);

      expect(this.controller.selectedStates).toBeArrayOfSize(selectedStates.length);
      expect(this.controller.selectedStates).toContain(selectedStates[0]);
      expect(this.controller.states).toBeArrayOfSize(_.keys(this.ShipmentState).length);
    });

    it('selected states passed to component can be undefined', function() {
      this.createScope(undefined, this.onSelection);
      expect(this.controller.selectedStates).toBeEmptyArray();
    });

    it('callback is invoked when selection is changed', function() {
      var selectedStates = [ this.ShipmentState.CREATED ];
      this.createScope(selectedStates, onSelection);

      this.controller.selectionChanged();
      this.scope.$digest();

      function onSelection(selections) {
        expect(selections).toBeArrayOfSize(selectedStates.length);
        expect(selections).toContain(selectedStates[0]);
      }
    });

    it('all states can be checked', function() {
      var shipmentStates = _.keys(this.ShipmentState);
      this.createScope([], onSelection);

      this.controller.checkAll(true);
      this.scope.$digest();

      function onSelection(selections) {
        expect(selections).toBeArrayOfSize(shipmentStates.length);
        expect(selections).toContainAll(shipmentStates);
      }
    });

    it('all states can be unchecked', function() {
      var shipmentStates = _.keys(this.ShipmentState);
      this.createScope(shipmentStates, onSelection);

      expect(this.controller.selectedStates).toBeArrayOfSize(shipmentStates.length);
      expect(this.controller.selectedStates).toContainAll(shipmentStates);

      this.controller.checkAll(false);
      this.scope.$digest();

      function onSelection(selections) {
        expect(selections).toBeEmptyArray();
      }
    });

    it('all FROM states can be checked', function() {
      var self = this,
          fromStates = [
            self.ShipmentState.CREATED,
            self.ShipmentState.PACKED,
            self.ShipmentState.SENT,
          ];

      this.createScope([], onSelection);

      expect(this.controller.selectedStates).toBeEmptyArray();

      this.controller.checkFromStates(false);
      this.scope.$digest();

      function onSelection(selections) {
        expect(selections).toBeArrayOfSize(3);
        expect(selections).toContainAll(fromStates);
        expect(self.controller.selectedStates).toContainAll(fromStates);
      }
    });

    it('all TO states can be checked', function() {
      var self = this,
          toStates = [
            self.ShipmentState.RECEIVED,
            self.ShipmentState.UNPACKED
          ];

      this.createScope([], onSelection);

      expect(this.controller.selectedStates).toBeEmptyArray();

      this.controller.checkToStates(false);
      this.scope.$digest();

      function onSelection(selections) {
        expect(selections).toBeArrayOfSize(2);
        expect(selections).toContainAll(toStates);
        expect(self.controller.selectedStates).toContainAll(toStates);
      }
    });

  });

});
