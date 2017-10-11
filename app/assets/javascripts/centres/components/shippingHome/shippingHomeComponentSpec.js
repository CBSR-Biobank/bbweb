/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('shippingHomeComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'factory');
      this.createController = () => {
        this.element = angular.element('<shipping-home><shipping-home>');
        this.scope = this.$rootScope.$new();
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('shippingHome');
      };

      this.createCentreLocations = (centres) => {
        centres.forEach((centre) => {
          var locs = _.range(2).map(() => this.factory.location());
          centre.locations = locs;
        });
        return this.factory.centreLocations(centres);
      };
    });
  });

  it('has valid scope', function() {
    var centres = _.range(2).map(() => this.factory.centre()),
        centreLocations = this.createCentreLocations(centres),
        args;

    spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when(centreLocations));
    spyOn(this.Centre, 'list').and.returnValue(this.$q.when(centres));
    this.createController();

    expect(this.controller.hasValidCentres).toBeTrue();
    expect(this.controller.centreIcon).toBeDefined();

    args = this.Centre.list.calls.argsFor(0)[0];
    expect(args.filter).toEqual('state::enabled');
  });

  it('correct setting when no centres present', function() {
    spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when([]));
    spyOn(this.Centre, 'list').and.returnValue(this.$q.when([]));
    this.createController();
    expect(this.controller.hasValidCentres).toBeFalse();
  });

  it('filter is updated correctly when retrieving centres', function() {
    var options = { filter: 'name:like:test' },
        args;

    spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when([]));
    spyOn(this.Centre, 'list').and.returnValue(this.$q.when([]));
    spyOn(this.$state, 'go').and.returnValue(null);
    this.createController();
    this.controller.updateCentres(options);
    this.scope.$digest();

    args = this.Centre.list.calls.argsFor(0)[0];
    expect(args.filter).toEqual(options.filter + ';state::enabled');
  });

  it('changes state when a centre is selected', function() {
    var centre = new this.Centre(this.factory.centre());

    spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when([]));
    spyOn(this.Centre, 'list').and.returnValue(this.$q.when([]));
    spyOn(this.$state, 'go').and.returnValue(null);
    this.createController();
    this.controller.centreSelected(centre);
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith('home.shipping.centre.incoming', { centreId: centre.id });
  });

});
