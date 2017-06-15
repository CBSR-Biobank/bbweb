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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'modalService',
                              'User',
                              'UserCounts',
                              'UserState',
                              'EntityViewer',
                              'UserViewer',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/admin/users/components/usersTable/usersTable.html',
        '/assets/javascripts/common/directives/pagination.html');

      this.createScope = function (userCounts) {
        ComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<users-table user-counts="vm.userCounts"></users-table>',
          { userCounts:  userCounts },
          'usersTable');
      };

      this.createUserCounts = function (registered, active, locked) {
        return new this.UserCounts({
          total:      registered + active + locked,
          registered: registered,
          active:     active,
          locked:     locked
        });
      };
    }));

    it('scope is valid on startup', function() {
      var self      = this,
          allStates = _.values(self.UserState),
          counts    = self.createUserCounts(1, 2, 3),
          nonHashedPossibleStates;

      self.createScope(counts);

      expect(self.controller.users).toBeArrayOfSize(0);
      expect(self.controller.state).toEqual('all');

      nonHashedPossibleStates = angular.copy(self.controller.possibleStates);
      _.each(allStates, function(state) {
        expect(nonHashedPossibleStates).toContain({ id: state, title: state.toUpperCase() });
      });
      expect(nonHashedPossibleStates).toContain({ id: 'all', title: 'All'});
    });

    describe('changing user state', function() {

      beforeEach(function() {
        this.counts       = this.createUserCounts(1, 2, 3);
        this.stateFnNames = ['activate', 'lock', 'unlock'];
        this.user         = new this.User(this.factory.user());
        spyOn(this.User, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([ this.user ])));
      });

      it('changing a users state works', function() {
        var self = this;

        spyOn(self.modalService, 'modalOkCancel').and.returnValue(self.$q.when('OK'));

        self.createScope(this.counts);
        self.controller.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
        self.scope.$digest();
        expect(self.controller.users).toBeArrayOfSize(1);

        this.stateFnNames.forEach(function(stateFnName) {
          spyOn(self.User.prototype, stateFnName).and.returnValue(self.$q.when(self.user));

          self.controller[stateFnName](self.user);
          self.scope.$digest();
          expect(self.User.prototype[stateFnName]).toHaveBeenCalled();
        });
      });

      it('throw error if user not in table', function() {
        var self = this,
            user = new this.User(this.factory.user());

        this.createScope(this.counts);
        this.stateFnNames.forEach(function(stateFnName) {
          expect(function () {
            self.controller[stateFnName](user);
          }).toThrowError(/user not found/);
        });
      });


    });

    it('can view user information', function() {
      var counts = this.createUserCounts(1, 2, 3),
          user   = this.User.create(this.factory.user());

      spyOn(this.EntityViewer.prototype, 'showModal').and.returnValue(null);
      spyOn(this.User, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([ user ])));

      this.createScope(counts);
      this.controller.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      this.scope.$digest();
      expect(this.controller.users).toBeArrayOfSize(1);

      this.controller.userInformation(user);
      this.scope.$digest();
      expect(this.EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

  });

});
