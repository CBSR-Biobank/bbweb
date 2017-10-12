/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import faker from 'faker';

describe('Component: userAdminComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'usersService',
                              'User',
                              'UserCounts',
                              'Factory');

      /*
       * Have to create controller as a directive so that $onInit() is fired on the controller.
       */
      this.createController = (userCounts) => {
        this.usersService.requestCurrentUser =
          jasmine.createSpy().and.returnValue(this.$q.when(new this.User()));
        this.UserCounts.get = jasmine.createSpy().and.returnValue(this.$q.when(userCounts));

        ComponentTestSuiteMixin.createController.call(
          this,
          '<user-admin></user-admin>',
          undefined,
          'userAdmin');
      };

      this.createUserCounts = (registered, active, locked) => {
        registered = registered || faker.random.number();
        active = active || faker.random.number();
        locked = locked || faker.random.number();

        return new this.UserCounts({
          total:      registered + active + locked,
          registered: registered,
          active:     active,
          locked:     locked
        });
      };

      this.createUserListSpy = (users) => {
        var reply = this.Factory.pagedResult(users);
        this.User.list = jasmine.createSpy().and.returnValue(this.$q.when(reply));
      };
    });
  });

  it('scope is valid on startup', function() {
    var counts = this.createUserCounts(1, 2, 3);
    this.createUserListSpy([]);
    this.createController(counts);
    expect(this.controller.haveUsers).toBe(counts.total > 0);
  });

});
