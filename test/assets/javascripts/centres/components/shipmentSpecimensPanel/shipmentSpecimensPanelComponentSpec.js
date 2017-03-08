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

    SuiteMixin.prototype.createScope = function (heading) {
      heading = heading || '';

      this.element = angular.element(
        '<shipment-specimens-panel heading="' + heading + '"></shipment-specimens-panel');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('shipmentSpecimensPanel');
    };

    return SuiteMixin;
  }

  describe('shipmentSpecimensPanelComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentSpecimensPanel/shipmentSpecimensPanel.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'factory');
    }));

    it('should have valid scope', function() {
      var heading = this.factory.stringNext();
      this.createScope(heading);
      expect(this.controller.heading).toBe(heading);
      expect(this.controller.panelOpen).toBeTrue();
      expect(this.controller.panelButtonClicked).toBeFunction();
    });

    it('user can close the panel', function() {
      var panelOpen;

      this.createScope();
      panelOpen = this.controller.panelOpen;
      this.controller.panelButtonClicked();
      this.scope.$digest();
      expect(this.controller.panelOpen).toBe(!panelOpen);
    });


  });

});
