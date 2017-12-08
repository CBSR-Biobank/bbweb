/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: centreLocationAdd', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Location',
                              'Factory',
                              'domainNotificationService',
                              'notificationsService');

      this.centre = new this.Centre(this.Factory.centre());
      this.location = new this.Location(this.Factory.location());
      this.returnStateName = 'home.admin.centres.centre.locations';

      this.createController = (centre) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<centre-location-add centre="vm.centre"></centre-location-add>',
          { centre: centre },
          'centreLocationAdd');
    });
  });

  it('scope should be valid', function() {
    this.createController(this.centre);
    expect(this.controller.centre).toBe(this.centre);
    expect(this.controller.submit).toBeFunction();
    expect(this.controller.cancel).toBeFunction();
  });

  it('should return to valid state on cancel', function() {
    this.createController(this.centre);
    spyOn(this.$state, 'go').and.callFake(function () {} );
    this.controller.cancel();
    expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName);
  });

  it('should display failure information on invalid submit', function() {
    const err = { message: 'simulated error' },
          $q = this.$injector.get('$q'),
          domainNotificationService = this.$injector.get('domainNotificationService');

    this.createController(this.centre);

    spyOn(domainNotificationService, 'updateErrorModal').and.returnValue(null);
    spyOn(this.Centre.prototype, 'addLocation').and.returnValue($q.reject(err));

    this.controller.submit(this.location);
    this.scope.$digest();
    expect(domainNotificationService.updateErrorModal)
      .toHaveBeenCalledWith(err, 'location');
  });


  it('should return to valid state on submit', function() {
    var $q = this.$injector.get('$q');

    this.createController(this.centre);
    spyOn(this.$state, 'go').and.callFake(function () {} );
    spyOn(this.Centre.prototype, 'addLocation').and.callFake(function () {
      return $q.when('test');
    });

    this.controller.submit(this.location);
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName, {}, { reload: true });
  });

});
