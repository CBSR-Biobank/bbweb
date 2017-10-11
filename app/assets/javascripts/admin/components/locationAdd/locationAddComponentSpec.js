/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: locationAdd', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Location',
                              'factory',
                              'domainNotificationService',
                              'notificationsService');

      this.centre = new this.Centre(this.factory.centre());
      this.location = new this.Location();

      this.currentState = {
        current: { name: 'home.admin.centres.centre.locationAdd'}
      };

      this.onSubmit = jasmine.createSpy('onSubmit');
      this.onCancel = jasmine.createSpy('onCancel');
      this.createController = (onSubmit, onCancel) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<location-add on-submit="vm.onSubmit" on-cancel="vm.onCancel"> </location-add>',
          {
            onSubmit: onSubmit,
            onCancel: onCancel
          },
          'locationAdd');
      };
    });
  });

  it('scope should be valid', function() {
    this.createController(this.onSubmit, this.onCancel);
    expect(this.controller.onSubmit).toBeFunction();
    expect(this.controller.onCancel).toBeFunction();
  });

  it('should invoke function on submit', function() {
    var location = new this.Location();

    this.createController(this.onSubmit, this.onCancel);
    this.controller.submit(location);
    this.scope.$digest();
    expect(this.onSubmit).toHaveBeenCalledWith(location);
  });

  it('should invoke function on cancel', function() {
    this.createController(this.onSubmit, this.onCancel);
    this.controller.cancel();
    expect(this.onCancel).toHaveBeenCalled();
  });

});
