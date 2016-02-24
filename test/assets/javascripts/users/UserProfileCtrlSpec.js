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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: UserProfileCtrl', function() {
    var ctrlMethods = ['updateName', 'updateEmail', 'updateAvatarUrl'],
        createController,
        jsonEntities;

    //   var scope, stateHelper, usersService, domainEntityService;
    //   var state = {current: {data: {returnState: 'admin.users'}}};
    //   var user  = {name: 'User1', email: 'admin@admin.com'};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(jsonEntities) {
      createController = setupController(this.$injector);
      jsonEntities = jsonEntities;
    }));

    function setupController(injector) {
      var $rootScope           = injector.get('$rootScope'),
          $controller          = injector.get('$controller'),
          $uibModal            = injector.get('$uibModal'),
          modalService         = injector.get('modalService'),
          User                 = injector.get('User'),
          notificationsService = injector.get('notificationsService');

      return create;

      //--

      function create(user) {
        var scope = $rootScope.$new();

        $controller('UserProfileCtrl as vm', {
          $scope:               scope,
          $uibModal:            $uibModal,
          modalService:         modalService,
          notificationsService: notificationsService,
          User:                 User,
          user:                 user
        });
        scope.$digest();
        return scope;
      }
    }

    it('should have valid scope', function() {
      var User = this.$injector.get('User'),
          user = jsonEntities.user(),
          scope = createController(user);

      expect(scope.vm.user).toEqual(new User(user));
    });

    it('should update a users name, email and avatar URL', function() {
      var notificationsService = this.$injector.get('notificationsService'),
          User                 = this.$injector.get('User');

      spyOn(notificationsService, 'success').and.callFake(function () {});
      updateUserCommon(this.$injector, fakeUserUpdate, expectClause);

      function fakeUserUpdate($q) {
        return $q.when(new User());
      }

      function expectClause() {
        expect(notificationsService.success).toHaveBeenCalled();
      }
    });

    it('should display a notification error when update fails', function() {
      var notificationsService = this.$injector.get('notificationsService');

      spyOn(notificationsService, 'error').and.callFake(function () {});
      updateUserCommon(this.$injector, fakeUserUpdate, expectClause);

      function fakeUserUpdate($q) {
        var deferred = $q.defer();
        deferred.reject({ data: { message: 'update failed' } });
        return deferred.promise;
      }

      function expectClause() {
        expect(notificationsService.error).toHaveBeenCalled();
      }
    });

    function updateUserCommon(injector, fakeUserUpdate, expectClause) {
      var $q           = injector.get('$q'),
          modalService = injector.get('modalService'),
          User         = injector.get('User'),
          user         = jsonEntities.user(),
          scope;

      spyOn(modalService, 'modalStringInput').and.callFake(function () {
        return $q.when('OK');
      });
      scope = createController(user);

      _.each(ctrlMethods, function (ctrlMethod) {
        spyOn(User.prototype, ctrlMethod).and.callFake(function () {
          return fakeUserUpdate($q);
        });

        scope.vm[ctrlMethod]();
        scope.$digest();
        expectClause();
      });
    }

    it('can remove a users avatar', function() {
      var $q                   = this.$injector.get('$q'),
          modalService         = this.$injector.get('modalService'),
          notificationsService = this.$injector.get('notificationsService'),
          User                 = this.$injector.get('User'),
          user                 = jsonEntities.user(),
          scope;

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn(User.prototype, 'updateAvatarUrl').and.callFake(function () {
        return $q.when(new User());
      });
      spyOn(notificationsService, 'success').and.callFake(function () {});

      scope = createController(user);
      scope.vm.removeAvatarUrl();
      scope.$digest();
      expect(notificationsService.success).toHaveBeenCalled();
    });

    it('should display a notification error when removing avatar URL fails', function() {
      var $q                   = this.$injector.get('$q'),
          modalService         = this.$injector.get('modalService'),
          notificationsService = this.$injector.get('notificationsService'),
          User                 = this.$injector.get('User'),
          user                 = jsonEntities.user(),
          scope;

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn(User.prototype, 'updateAvatarUrl').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ data: { message: 'xxx' } });
        return deferred.promise;
      });
      spyOn(notificationsService, 'error').and.callFake(function () {});

      scope = createController(user);
      scope.vm.removeAvatarUrl();
      scope.$digest();
      expect(notificationsService.error).toHaveBeenCalled();
    });

    it('can update users password', function() {
      var $q                   = this.$injector.get('$q'),
          modalService         = this.$injector.get('modalService'),
          notificationsService = this.$injector.get('notificationsService'),
          User                 = this.$injector.get('User'),
          user                 = jsonEntities.user(),
          scope;

      spyOn(modalService, 'passwordUpdateModal').and.callFake(function () {
        return $q.when({ currentPassword: 'xx', newPassword: 'xx' });
      });
      spyOn(User.prototype, 'updatePassword').and.callFake(function () {
        return $q.when(new User());
      });
      spyOn(notificationsService, 'success').and.callFake(function () {});

      scope = createController(user);
      scope.vm.updatePassword();
      scope.$digest();
      expect(notificationsService.success).toHaveBeenCalled();
    });

    it('should display a notification error when current password is invalid', function() {
      var $q                   = this.$injector.get('$q'),
          modalService         = this.$injector.get('modalService'),
          notificationsService = this.$injector.get('notificationsService'),
          User                 = this.$injector.get('User'),
          user                 = jsonEntities.user(),
          scope;

      spyOn(modalService, 'passwordUpdateModal').and.callFake(function () {
        return $q.when({ currentPassword: 'xx', newPassword: 'xx' });
      });
      spyOn(User.prototype, 'updatePassword').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ data: { message: 'invalid password' } });
        return deferred.promise;
      });
      spyOn(notificationsService, 'error').and.callFake(function () {});

      scope = createController(user);
      scope.vm.updatePassword();
      scope.$digest();
      expect(notificationsService.error).toHaveBeenCalled();
    });

    it('should display a notification error when updating password fails', function() {
      var $q                   = this.$injector.get('$q'),
          modalService         = this.$injector.get('modalService'),
          notificationsService = this.$injector.get('notificationsService'),
          User                 = this.$injector.get('User'),
          user                 = jsonEntities.user(),
          scope;

      spyOn(modalService, 'passwordUpdateModal').and.callFake(function () {
        return $q.when({ currentPassword: 'xx', newPassword: 'xx' });
      });
      spyOn(User.prototype, 'updatePassword').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ data: { message: 'xxx' } });
        return deferred.promise;
      });
      spyOn(notificationsService, 'error').and.callFake(function () {});

      scope = createController(user);
      scope.vm.updatePassword();
      scope.$digest();
      expect(notificationsService.error).toHaveBeenCalled();
    });

  });

});
