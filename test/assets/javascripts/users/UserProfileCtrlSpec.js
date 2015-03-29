// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: UserUpdateCtrl', function() {
    //   var scope, stateHelper, usersService, domainEntityService;
  //   var state = {current: {data: {returnState: 'admin.users'}}};
  //   var user  = {name: 'User1', email: 'admin@admin.com'};

    beforeEach(mocks.module('biobankApp'));

  //   beforeEach(inject(
  //     function($rootScope,
  //              $controller,
  //              $state,
  //              $filter,
  //              _domainEntityService_,
  //              _usersService_,
  //              modalService,
  //              _stateHelper_) {
  //       domainEntityService = _domainEntityService_;
  //       usersService = _usersService_;
  //       stateHelper = _stateHelper_;

  //       spyOn(stateHelper, 'reloadStateAndReinit');
  //       spyOn(domainEntityService, 'handleError');

  //       scope = $rootScope.$new();
  //       $controller('UserUpdateCtrl as vm', {
  //         $scope:                  scope,
  //         $state:                  state,
  //         $filter:                 $filter,
  //         domainEntityService: domainEntityService,
  //         usersService:            usersService,
  //         modalService:            modalService,
  //         stateHelper:             stateHelper,
  //         user:                    user
  //       });
  //       scope.$digest();
  //     }
  //   ));

  //   it('should contain valid settings to update a user', function() {
  //     expect(scope.vm.user).toBe(user);
  //     expect(scope.vm.password).toContain('');
  //     expect(scope.vm.confirmPassword).toContain('');
  //   });

  //   it('should return to valid state on cancel', function() {
  //     scope.vm.cancel();
  //     expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
  //       state.current.data.returnState);
  //   });

  });

});
