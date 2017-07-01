/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  /*eslint no-unused-vars: ["error", { "varsIgnorePattern": "angular" }]*/

  var angular = require('angular'),
      mocks   = require('angularMocks'),
      _       = require('lodash');

  fdescribe('createController', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (centre) {
        centre = centre || this.centre;

        if (!centre) {
          throw new Error('no centre to create component with');
        }

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<centre-shipments centre="vm.centre"></centre-shipments',
          { centre:  centre },
          'centreShipments');
      };

      return SuiteMixin;
    }


    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/centreShipments/centreShipments.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'factory');
    }));

    it('should have valid scope', function() {
      this.createController(new this.Centre(this.factory.centre()));
      expect(this.controller.tabs).toBeNonEmptyArray();
    });

  });

});
