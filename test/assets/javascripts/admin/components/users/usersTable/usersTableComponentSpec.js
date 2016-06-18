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
], function(angular, mocks, _) {
  'use strict';

  describe('Component: usersTableComponent', function() {

    var createUserCounts = function (registered, active, locked) {
      return new this.UserCounts({
        total:      registered + active + locked,
        registered: registered,
        active:     active,
        locked:     locked
      });
    };

    var createController = function (userCounts) {
      this.scope = this.$rootScope.$new();
      this.controller = this.$componentController('usersTable',
                                                  null,
                                                  { userCounts: userCounts });
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(this, testSuiteMixin);
      self.injectDependencies('$q',
                              '$rootScope',
                              '$componentController',
                              'modalService',
                              'User',
                              'UserCounts',
                              'UserStatus',
                              'userStatusLabel',
                              'EntityViewer',
                              'UserViewer',
                              'factory');
    }));

    it('scope is valid on startup', function() {
      var self        = this,
          allStatuses = _.values(self.UserStatus),
          counts      = createUserCounts.call(self, 1, 2, 3);

      createController.call(self, counts);

      expect(self.controller.users).toBeArrayOfSize(0);
      expect(self.controller.status).toEqual('all');

      _.each(allStatuses, function(status) {
        expect(self.controller.possibleStatuses).toContain({
          id: status,
          title: self.userStatusLabel.statusToLabel(status)
        });
      });
      expect(self.controller.possibleStatuses).toContain({ id: 'all', title: 'All'});
    });

    it('changing a users status works', function() {
      var self          = this,
          counts        = createUserCounts.call(self, 1, 2, 3),
          statusFnNames = ['activate', 'lock', 'unlock'],
          user          = self.User.create(self.factory.user());

      spyOn(self.User, 'get').and.returnValue(self.$q.when(user));
      spyOn(self.User, 'list').and.returnValue(self.$q.when(self.factory.pagedResult([ user ])));
      spyOn(self.modalService, 'showModal').and.returnValue(self.$q.when('--dont-care--'));

      createController.call(self, counts);
      self.controller.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      self.scope.$digest();
      expect(self.controller.users).toBeArrayOfSize(1);

      _.each(statusFnNames, function(statusFnName) {
        spyOn(self.User.prototype, statusFnName).and.returnValue(self.$q.when('status changed'));

        self.controller[statusFnName](user);
        self.scope.$digest();
        expect(self.User.prototype[statusFnName]).toHaveBeenCalled();
      });
    });

    it('can view user information', function() {
      var counts       = createUserCounts.call(this, 1, 2, 3),
          user         = this.User.create(this.factory.user());

      spyOn(this.EntityViewer.prototype, 'showModal').and.returnValue(null);
      spyOn(this.User, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([ user ])));

      createController.call(this, counts);
      this.controller.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      this.scope.$digest();
      expect(this.controller.users).toBeArrayOfSize(1);

      this.controller.userInformation(user);
      this.scope.$digest();
      expect(this.EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

  });

});
