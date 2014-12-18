// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  ddescribe('Controller: UserUpdateCtrl', function() {
    var scope, stateHelper, usersService, domainEntityUpdateError;
    var state = {current: {data: {returnState: 'admin.users'}}};
    var user  = {name: 'User1', email: 'admin@admin.com'};

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(
      function($rootScope,
               $controller,
               $state,
               $filter,
               _domainEntityUpdateError_,
               _usersService_,
               modalService,
               _stateHelper_) {
        domainEntityUpdateError = _domainEntityUpdateError_;
        usersService = _usersService_;
        stateHelper = _stateHelper_;

        spyOn(stateHelper, 'reloadStateAndReinit');
        spyOn(domainEntityUpdateError, 'handleError');

        scope = $rootScope.$new();
        $controller('UserUpdateCtrl as vm', {
          $scope:                  scope,
          $state:                  state,
          $filter:                 $filter,
          domainEntityUpdateError: domainEntityUpdateError,
          usersService:            usersService,
          modalService:            modalService,
          stateHelper:             stateHelper,
          user:                    user
        });
        scope.$digest();
      }
    ));

    it('should contain valid settings to update a user', function() {
      expect(scope.vm.user).toBe(user);
      expect(scope.vm.password).toContain('');
      expect(scope.vm.confirmPassword).toContain('');
    });

    it('should return to valid state on cancel', function() {
      scope.vm.cancel();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        state.current.data.returnState);
    });

    it('should return to valid state on submit', inject(function($q) {
      spyOn(usersService, 'addOrUpdate').and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('xxx');
        return deferred.promise;
      });

      scope.vm.submit(study);
      scope.$digest();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        'admin.studies', {}, {reload: true});
    }));

  });

});
