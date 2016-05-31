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
], function(angular, mocks, _) {
  'use strict';

  describe('Component: usersTableComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $componentController) {
      var self = this;

      self.$q              = self.$injector.get('$q');
      self.modalService    = self.$injector.get('modalService');
      self.User            = self.$injector.get('User');
      self.UserCounts      = self.$injector.get('UserCounts');
      self.UserStatus      = self.$injector.get('UserStatus');
      self.userStatusLabel = self.$injector.get('userStatusLabel');
      self.UserViewer      = self.$injector.get('UserViewer');
      self.factory    = self.$injector.get('factory');

      self.createUserCounts = createUserCounts;
      self.createController = createController;

      function createUserCounts(registered, active, locked) {
        return new self.UserCounts({
          total:      registered + active + locked,
          registered: registered,
          active:     active,
          locked:     locked
        });
      }

      function createController(userCounts) {
        self.scope = $rootScope.$new();
        self.controller = $componentController('usersTable',
                                               null,
                                               { userCounts: userCounts });
      }
    }));

    it('scope is valid on startup', function() {
      var self        = this,
          allStatuses = _.values(self.UserStatus),
          counts      = self.createUserCounts(1, 2, 3);

      self.createController(counts);

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
          counts        = self.createUserCounts(1, 2, 3),
          statusFnNames = ['activate', 'lock', 'unlock'],
          user          = self.User.create(self.factory.user());

      spyOn(self.User, 'get').and.returnValue(self.$q.when(user));
      spyOn(self.User, 'list').and.returnValue(self.$q.when(self.factory.pagedResult([ user ])));
      spyOn(self.modalService, 'showModal').and.returnValue(self.$q.when('--dont-care--'));

      self.createController(counts);
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
      var EntityViewer = this.$injector.get('EntityViewer'),
          counts       = this.createUserCounts(1, 2, 3),
          user         = this.User.create(this.factory.user());

      spyOn(EntityViewer.prototype, 'showModal').and.returnValue(null);
      spyOn(this.User, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([ user ])));

      this.createController(counts);
      this.controller.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      this.scope.$digest();
      expect(this.controller.users).toBeArrayOfSize(1);

      this.controller.userInformation(user);
      this.scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

  });

});
