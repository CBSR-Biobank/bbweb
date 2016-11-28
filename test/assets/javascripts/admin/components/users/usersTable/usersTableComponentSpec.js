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

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function (userCounts) {
      this.element = angular.element('<users-table user-counts="vm.userCounts"></users-table>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { userCounts:  userCounts };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('usersTable');
    };

    SuiteMixin.prototype.createUserCounts = function (registered, active, locked) {
      return new this.UserCounts({
        total:      registered + active + locked,
        registered: registered,
        active:     active,
        locked:     locked
      });
    };

    return SuiteMixin;
  }

  describe('Component: usersTableComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

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
        '/assets/javascripts/admin/components/users/usersTable/usersTable.html',
        '/assets/javascripts/common/directives/pagination.html');
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

    it('changing a users state works', function() {
      var self          = this,
          counts        = self.createUserCounts(1, 2, 3),
          stateFnNames = ['activate', 'lock', 'unlock'],
          user          = self.User.create(self.factory.user());

      spyOn(self.User, 'get').and.returnValue(self.$q.when(user));
      spyOn(self.User, 'list').and.returnValue(self.$q.when(self.factory.pagedResult([ user ])));
      spyOn(self.modalService, 'showModal').and.returnValue(self.$q.when('--dont-care--'));

      self.createScope(counts);
      self.controller.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      self.scope.$digest();
      expect(self.controller.users).toBeArrayOfSize(1);

      _.each(stateFnNames, function(stateFnName) {
        spyOn(self.User.prototype, stateFnName).and.returnValue(self.$q.when('state changed'));

        self.controller[stateFnName](user);
        self.scope.$digest();
        expect(self.User.prototype[stateFnName]).toHaveBeenCalled();
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
