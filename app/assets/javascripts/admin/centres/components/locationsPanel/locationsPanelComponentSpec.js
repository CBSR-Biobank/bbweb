/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: locationsPanel', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin, testUtils) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Centre',
                              'Location',
                              'modalService',
                              'domainNotificationService',
                              'factory');

      testUtils.addCustomMatchers();

      this.createScope = (scopeVars) => {
        var scope = ComponentTestSuiteMixin.createScope.call(this, scopeVars);
        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        scope.$on('tabbed-page-update', this.eventRxFunc);
        return scope;
      };

      this.createController = (centre) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<locations-panel centre="vm.centre"></locations-panel>',
          { centre: centre },
          'locationsPanel');
      };

      this.createEntities = () => ({
        centre: this.Centre.create(this.factory.centre()),
        locations: _.range(3).map(() => this.Location.create(this.factory.location()))
      });

    });
  });

  it('initialization is valid', function() {
    var entities = this.createEntities();
    this.createController(entities.centre);
    expect(this.controller.centre).toBe(entities.centre);
    expect(this.eventRxFunc).toHaveBeenCalled();
  });

  it('can add a location', function() {
    var $state = this.$injector.get('$state'),
        entities = this.createEntities();

    this.createController(entities.centre);
    spyOn($state, 'go').and.callFake(function () {});
    this.controller.add();
    this.scope.$digest();
    expect($state.go).toHaveBeenCalledWith(
      'home.admin.centres.centre.locations.locationAdd',
      {},
      { reload: true });
  });

  it('can remove a location', function() {
    var entities            = this.createEntities(),
        locationToRemove    = entities.locations[0];

    entities.centre.locations.push(locationToRemove);
    this.createController(entities.centre);

    spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
    spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(this.$q.when(entities.centre));

    this.controller.remove(locationToRemove);
    this.scope.$digest();
    expect(this.Centre.prototype.removeLocation).toHaveBeenCalled();
  });

  it('displays information modal when removal of a location fails', function() {
    var entities         = this.createEntities(),
        locationToRemove = entities.locations[0];

    entities.centre.locations.push(locationToRemove);
    spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));

    this.createController(entities.centre);
    spyOn(this.Centre.prototype, 'removeLocation').and.returnValue(this.$q.reject('simulated error'));
    this.controller.remove(locationToRemove);
    this.scope.$digest();
    expect(this.modalService.modalOkCancel.calls.count()).toBe(2);
  });

  it('allows a user to view a location', function() {
    var centre = new this.Centre(this.factory.centre()),
        location = new this.Location(this.factory.location());

    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController(centre);
    this.controller.view(location);
    expect(this.$state.go).toHaveBeenCalledWith(
      'home.admin.centres.centre.locations.locationView',
      { locationId: location.id });
  });

});
