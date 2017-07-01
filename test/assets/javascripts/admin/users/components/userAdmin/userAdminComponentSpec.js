/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('Component: userAdminComponent', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createUserCounts = function (registered, active, locked) {
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

      /*
       * Have to create controller as a directive so that $onInit() is fired on the controller.
       */
      SuiteMixin.prototype.createController = function (userCounts) {
        this.UserCounts.get = jasmine.createSpy('get').and.returnValue(this.$q.when(userCounts));

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<user-admin></user-admin>',
          undefined,
          'userAdmin');
      };

      SuiteMixin.prototype.createUserListSpy = function (users) {
        var reply = this.factory.pagedResult(users);
        spyOn(this.User, 'list').and.returnValue(this.$q.when(reply));
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'User',
                              'UserCounts',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/admin/users/components/userAdmin/userAdmin.html',
        '/assets/javascripts/admin/users/components/usersPagedList/usersPagedList.html',
        '/assets/javascripts/admin/users/components/nameEmailStateFilters/nameEmailStateFilters.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
    }));

    it('scope is valid on startup', function() {
      var counts = this.createUserCounts(1, 2, 3);
      this.createUserListSpy([]);
      this.createController(counts);
      expect(this.controller.haveUsers).toBe(counts.total > 0);
    });

  });

});
