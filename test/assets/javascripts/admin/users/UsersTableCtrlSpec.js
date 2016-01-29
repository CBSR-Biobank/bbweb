/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: UsersTableCtrl', function() {
    var q,
        rootScope,
        controller,
        modalService,
        tableService,
        User,
        UserCounts,
        UserStatus,
        UserViewer,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $rootScope,
                               $controller,
                               _modalService_,
                               _tableService_,
                               _User_,
                               _UserCounts_,
                               _UserStatus_,
                               _UserViewer_,
                               fakeDomainEntities) {
      q            = $q;
      rootScope    = $rootScope;
      controller   = $controller;
      modalService = _modalService_;
      tableService = _tableService_;
      User         = _User_;
      UserCounts   = _UserCounts_;
      UserStatus   = _UserStatus_;
      UserViewer   = _UserViewer_;
      fakeEntities = fakeDomainEntities;
    }));

    function createUserCounts(registered, active, locked) {
      return new UserCounts({
        total:      registered + active + locked,
        registered: registered,
        active:     active,
        locked:     locked
      });
    }

    function createController(userCounts) {
      var scope = rootScope.$new();

      controller('UsersTableCtrl as vm', {
        $scope:       scope,
        modalService: modalService,
        tableService: tableService,
        User:         User,
        UserStatus:   UserStatus,
        UserViewer:   UserViewer,
        userCounts:   userCounts
      });

      scope.$digest();
      return scope;
    }

    it('scope is valid on startup', function() {
      var allStatuses = UserStatus.values(),
          counts = createUserCounts(1, 2, 3),
          scope = createController(counts);

      expect(scope.vm.users).toBeArrayOfSize(0);
      expect(scope.vm.haveUsers).toBe(counts.total > 0);
      expect(scope.vm.pagedResults).toBeDefined();
      expect(scope.vm.nameFilter).toBeDefined();
      expect(scope.vm.status).toEqual({ id: 'all', title: 'All'});
      expect(scope.vm.tableParams).toBeDefined();

      _.each(allStatuses, function(status) {
        expect(scope.vm.possibleStatuses).toContain({ id: status, title: UserStatus.label(status) });
      });
      expect(scope.vm.possibleStatuses).toContain({ id: 'all', title: 'All'});
    });

    it('table is reloaded when filters are updated', function() {
      var counts = createUserCounts(1, 2, 3),
          scope = createController(counts);

      spyOn(scope.vm.tableParams, 'reload');
      scope.vm.nameFilterUpdated();
      scope.$digest();
      expect(scope.vm.tableParams.reload).toHaveBeenCalled();

      scope.vm.emailFilterUpdated();
      scope.$digest();
      expect(scope.vm.tableParams.reload.calls.count()).toEqual(2);

      scope.vm.statusFilterUpdated();
      scope.$digest();
      expect(scope.vm.tableParams.reload.calls.count()).toEqual(3);
    });

    /**
     * Skip this for now, since running into ng-table bug
     */
    xit('changing a users status works', function() {
      var counts = createUserCounts(1, 2, 3),
          statusFnNames = ['activate', 'lock', 'unlock'],
          user = User.create(fakeEntities.user),
          scope;

      spyOn(User, 'list').and.callFake(function () {
        return q.when([ user ]);
      });

      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('xxx');
      });

      scope = createController(counts);
      scope.vm.nameFilterUpdated(); // force a table update
      scope.$digest();

      _.each(statusFnNames, function(statusFnName) {
        spyOn(User.prototype, statusFnName).and.callThrough();

        scope.vm[statusFnName](scope.vm.users[0]);
        scope.$digest();
        expect(User.prototype[statusFnName]).toHaveBeenCalled();
      });
    });

    /**
     * Skip this for now, since running into ng-table bug
     */
    xit('message is correct', function() {
      var scope, counts = createUserCounts(1, 2, 3);

      scope = createController(counts);
      expect(scope.vm.message).toBe('The following users have been configured.');

      scope.vm.nameFilter = 'abc';
      scope.$digest();
      expect(scope.vm.message).toBe('The following users match the criteria:');
    });


  });

});
