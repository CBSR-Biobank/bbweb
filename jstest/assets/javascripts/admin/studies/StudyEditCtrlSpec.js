// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudyEditCtrl', function() {

    beforeEach(mocks.module('biobankApp', function($provide) {
      // windowService = {
      //   localStorage: {
      //     setItem: function() {},
      //     getItem: function() { return {}; }
      //   }
      // };

      // spyOn(windowService.localStorage, 'setItem');
      // $provide.value('$window', windowService);

      // stateService = {
      //   current: {
      //     name: 'admin.studies.study.processing'
      //   }
      // };

      // $provide.value('$state', stateService);
    }));

    beforeEach(inject(function($controller, $rootScope, $window, $state, $timeout) {
      // timeout = $timeout;
      // scope = $rootScope.$new();

      // $controller('StudyCtrl as vm', {
      //   $window:  $window,
      //   $scope:   scope,
      //   $state:   $state,
      //   $timeout: $timeout,
      //   study:    study
      // });
      // scope.$digest();
    }));

  });

});
