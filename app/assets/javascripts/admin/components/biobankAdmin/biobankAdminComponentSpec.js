/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: biobankAdmin', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'User',
                              'adminService',
                              'usersService',
                              'factory');
      this.createController = () => {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<biobank-admin></biobank-admin>',
          undefined,
          'biobankAdmin');
      };

    });
  });

  it('has valid scope', function() {
    var user = this.User.create(this.factory.user()),
        counts = {
          studies: 1,
          centres: 2,
          users: 3
        };

    this.usersService.requestCurrentUser =
      jasmine.createSpy().and.returnValue(this.$q.when(user));
    spyOn(this.adminService, 'aggregateCounts').and.returnValue(this.$q.when(counts));

    this.createController();
    expect(this.controller.user).toEqual(jasmine.any(this.User));
    expect(this.controller.counts).toEqual(counts);
  });

});