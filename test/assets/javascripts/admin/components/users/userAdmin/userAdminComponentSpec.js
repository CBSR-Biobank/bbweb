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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q         = self.$injector.get('$q');
      self.UserCounts = self.$injector.get('UserCounts');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/components/users/userAdmin/userAdmin.html',
        '/assets/javascripts/admin/components/users/usersTable/usersTable.html',
        '/assets/javascripts/common/directives/pagination.html');

      self.createUserCounts = createUserCounts;
      self.createController = createController;

      //---

      function createUserCounts(registered, active, locked) {
        registered = registered || faker.random.number();
        active = active || faker.random.number();
        locked = locked || faker.random.number();

        return new self.UserCounts({
          total:      registered + active + locked,
          registered: registered,
          active:     active,
          locked:     locked
        });
      }

      /**
       * Have to create controller as a directive so that $onInit() is fired on the controller.
       */
      function createController(userCounts) {
        self.UserCounts.get = jasmine.createSpy('get').and.returnValue(self.$q.when(userCounts));

        self.element = angular.element('<user-admin></user-admin>');
        self.scope = $rootScope.$new();
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('userAdmin');
      }
    }));

    it('scope is valid on startup', function() {
      var counts = this.createUserCounts(1, 2, 3);
      this.createController(counts);
      expect(this.controller.haveUsers).toBe(counts.total > 0);
    });

  });

});
