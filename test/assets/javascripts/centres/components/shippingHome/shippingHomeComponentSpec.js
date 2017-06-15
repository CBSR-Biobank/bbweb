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

    SuiteMixin.prototype.createScope = function () {
      this.element = angular.element('<shipping-home><shipping-home>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('shippingHome');
    };

    SuiteMixin.prototype.createCentreLocations = function (centres) {
      var self = this,
          locations = [];

      centres.forEach(function (centre) {
        var locs = _.map(_.range(2), function () {
          return self.factory.location();
        });
        locations = locations.concat(locs);
        centre.locations = locs;
      });
      return self.factory.centreLocations(centres);
    };

    return SuiteMixin;
  }

  describe('shippingHomeComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shippingHome/shippingHome.html',
        '/assets/javascripts/centres/components/selectCentre/selectCentre.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

    }));

    it('has valid scope', function() {
      var self = this,
          centres = _.map(_.range(2), function () { return self.factory.centre(); }),
          centreLocations = this.createCentreLocations(centres),
          args;

      spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when(centreLocations));
      spyOn(this.Centre, 'list').and.returnValue(this.$q.when(centres));
      this.createScope();

      expect(this.controller.hasValidCentres).toBeTrue();
      expect(this.controller.centreIcon).toBeDefined();

      args = this.Centre.list.calls.argsFor(0)[0];
      expect(args.filter).toEqual('state::enabled');
    });

    it('correct setting when no centres present', function() {
      spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when([]));
      spyOn(this.Centre, 'list').and.returnValue(this.$q.when([]));
      this.createScope();
      expect(this.controller.hasValidCentres).toBeFalse();
    });

    it('filter is updated correctly when retrieving centres', function() {
      var options = { filter: 'name:like:test' },
          args;

      spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when([]));
      spyOn(this.Centre, 'list').and.returnValue(this.$q.when([]));
      spyOn(this.$state, 'go').and.returnValue(null);
      this.createScope();
      this.controller.updateCentres(options);
      this.scope.$digest();

      args = this.Centre.list.calls.argsFor(0)[0];
      expect(args.filter).toEqual(options.filter + ';' + 'state::enabled');
    });

    it('changes state when a centre is selected', function() {
      var centre = new this.Centre(this.factory.centre());

      spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when([]));
      spyOn(this.Centre, 'list').and.returnValue(this.$q.when([]));
      spyOn(this.$state, 'go').and.returnValue(null);
      this.createScope();
      this.controller.centreSelected(centre);
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home.shipping.centre.incoming', { centreId: centre.id });
    });

  });

});
