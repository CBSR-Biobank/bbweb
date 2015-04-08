/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: UserProfileCtrl', function() {
    var createController;

    //   var scope, stateHelper, usersService, domainEntityService;
    //   var state = {current: {data: {returnState: 'admin.users'}}};
    //   var user  = {name: 'User1', email: 'admin@admin.com'};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function() {
      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope = injector.get('$rootScope'),
          $controller = injector.get('$controller'),
          $state = injector.get('$state'),
          $modal = injector.get('$modal'),
          notificationsService = injector.get('notificationsService');

      return create;

      //--

      function create() {
        var scope = $rootScope.$new();

        $controller('UserProfileCtrl as vm', {
          $scope:                  scope,
          $state:                  $state,
          $modal:                  $modal,
          notificationsService: notificationsService
          // stateHelper:             stateHelper,
          // user:                    user
        });
        scope.$digest();
      }
    }

    //   it('should contain valid settings to update a user', function() {
    //     expect(scope.vm.user).toBe(user);
    //     expect(scope.vm.password).toContain('');
    //     expect(scope.vm.confirmPassword).toContain('');
    //   });

    //   it('should return to valid state on cancel', function() {
    //     scope.vm.cancel();
    //     expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
    //       state.current.data.returnState);
  });

});
