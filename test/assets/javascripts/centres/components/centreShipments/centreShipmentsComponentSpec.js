/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  /*eslint no-unused-vars: ["error", { "varsIgnorePattern": "angular" }]*/

  var angular         = require('angular'),
      mocks           = require('angularMocks'),
      _               = require('lodash');

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function (centre) {
      centre = centre || this.centre;

      if (!centre) {
        throw new Error('no centre to create component with');
      }

      this.element = angular.element('<centre-shipments centre="vm.centre"></centre-shipments');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { centre:  centre };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centreShipments');
    };

    return SuiteMixin;
  }

  describe('centreShipmentsComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/centreShipments/centreShipments.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'factory');
    }));

    it('should have valid scope', function() {
      this.createScope(new this.Centre(this.factory.centre()));

      expect(this.controller.tabs).toBeNonEmptyArray();
    });

  });

});
