/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('Component: userAdminComponent', function() {

    var createUserCounts = function (registered, active, locked) {
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

    /**
     * Have to create controller as a directive so that $onInit() is fired on the controller.
     */
    var createController = function (userCounts) {
      this.UserCounts.get = jasmine.createSpy('get').and.returnValue(this.$q.when(userCounts));

      this.element = angular.element('<user-admin></user-admin>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('userAdmin');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'UserCounts');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/components/users/userAdmin/userAdmin.html',
        '/assets/javascripts/admin/components/users/usersTable/usersTable.html',
        '/assets/javascripts/common/directives/pagination.html');
    }));

    it('scope is valid on startup', function() {
      var counts = createUserCounts.call(this, 1, 2, 3);
      createController.call(this, counts);
      expect(this.controller.haveUsers).toBe(counts.total > 0);
    });

  });

});
