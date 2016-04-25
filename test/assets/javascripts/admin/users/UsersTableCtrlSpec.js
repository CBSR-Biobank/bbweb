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
  'moment',
  'biobankApp'
], function(angular, mocks, _, moment) {
  'use strict';

  describe('Controller: UsersTableCtrl', function() {
    var q,
        rootScope,
        controller,
        modalService,
        User,
        UserCounts,
        UserStatus,
        userStatusLabel,
        UserViewer,
        jsonEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $rootScope,
                               $controller,
                               _modalService_,
                               _User_,
                               _UserCounts_,
                               _UserStatus_,
                               _userStatusLabel_,
                               _UserViewer_,
                               _jsonEntities_) {
      q               = $q;
      rootScope       = $rootScope;
      controller      = $controller;
      modalService    = _modalService_;
      User            = _User_;
      UserCounts      = _UserCounts_;
      UserStatus      = _UserStatus_;
      userStatusLabel = _userStatusLabel_;
      UserViewer      = _UserViewer_;
      jsonEntities    = _jsonEntities_;
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
        User:         User,
        UserStatus:   UserStatus,
        UserViewer:   UserViewer,
        userCounts:   userCounts
      });

      scope.$digest();
      return scope;
    }

    it('scope is valid on startup', function() {
      var allStatuses = _.values(UserStatus),
          counts = createUserCounts(1, 2, 3),
          scope = createController(counts);

      expect(scope.vm.users).toBeArrayOfSize(0);
      expect(scope.vm.haveUsers).toBe(counts.total > 0);
      expect(scope.vm.status).toEqual('all');

      _.each(allStatuses, function(status) {
        expect(scope.vm.possibleStatuses).toContain({
          id: status,
          title: userStatusLabel.statusToLabel(status)
        });
      });
      expect(scope.vm.possibleStatuses).toContain({ id: 'all', title: 'All'});
    });

    it('changing a users status works', function() {
      var counts = createUserCounts(1, 2, 3),
          statusFnNames = ['activate', 'lock', 'unlock'],
          user = User.create(jsonEntities.user()),
          scope;

      spyOn(User, 'get').and.callFake(function () {
        return q.when(user);
      });

      spyOn(User, 'list').and.callFake(function () {
        return q.when(jsonEntities.pagedResult([ user ]));
      });

      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('--dont-care--');
      });

      scope = createController(counts);
      scope.vm.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      scope.$digest();
      expect(scope.vm.users).toBeArrayOfSize(1);

      _.each(statusFnNames, function(statusFnName) {
        spyOn(User.prototype, statusFnName).and.callFake(function () {
          return q.when('status changed');
        });

        scope.vm[statusFnName](user);
        scope.$digest();
        expect(User.prototype[statusFnName]).toHaveBeenCalled();
      });
    });

    it('can view user information', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          counts = createUserCounts(1, 2, 3),
          user = User.create(jsonEntities.user()),
          scope;

      spyOn(User, 'list').and.callFake(function () {
        return q.when(jsonEntities.pagedResult([ user ]));
      });

      scope = createController(counts);
      scope.vm.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      scope.$digest();
      expect(scope.vm.users).toBeArrayOfSize(1);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.userInformation(user);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can retrieve user local time added', function() {
      var bbwebConfig = this.$injector.get('bbwebConfig'),
          counts = createUserCounts(1, 2, 3),
          user = User.create(jsonEntities.user()),
          scope;

      spyOn(User, 'list').and.callFake(function () {
        return q.when(jsonEntities.pagedResult([ user ]));
      });

      scope = createController(counts);
      scope.vm.getTableData({ pagination: { start: 0 }, search: {}, sort: {} });
      scope.$digest();
      expect(scope.vm.users).toBeArrayOfSize(1);

      expect(scope.vm.getTimeAddedLocal(user))
        .toBe(moment(user.timeAdded).format(bbwebConfig.dateTimeFormat));
    });

  });

});
