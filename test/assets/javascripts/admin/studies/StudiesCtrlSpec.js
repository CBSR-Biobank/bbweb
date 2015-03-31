// Jasmine test suite
//
define([
  'angular',
  'underscore',
  'angularMocks',
  'biobank.testUtils',
  'biobankApp'
], function(angular, _, mocks, testUtils) {
  'use strict';

  describe('Controller: StudiesCtrl', function() {
    var rootScope, controller, StudyCounts;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($controller,
                               $rootScope,
                               _StudyCounts_,
                               fakeDomainEntities) {
      rootScope = $rootScope;
      controller = $controller;
      StudyCounts = _StudyCounts_;

      testUtils.addCustomMatchers();
    }));

    function createStudyCounts(disabled, enabled, retired) {
      return new StudyCounts({
        total:    disabled + enabled + retired,
        disabled: disabled,
        enabled:  enabled,
        retired:  retired
      });
    }

    function createController(studyCounts) {
      var scope = rootScope.$new();

      controller('StudiesCtrl as vm', {
        $scope: scope,
        studyCounts: studyCounts
      });

      scope.$digest();
      return scope;
    }

    it('scope is valid on startup', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          allStatuses = StudyStatus.values().concat('All'),
          counts = createStudyCounts(1, 2, 3),
          scope = createController(counts);

      expect(scope.vm.studyCounts).toEqual(counts);
      expect(scope.vm.pageSize).toBeDefined();

      _.each(allStatuses, function(status) {
        expect(scope.vm.possibleStatuses).toContain({ id: status.toLowerCase(), label: status});
      });
    });

    it('updateStudies retrieves new list of studies', function() {
      var Study = this.$injector.get('Study'),
          counts = createStudyCounts(1, 2, 3),
          listOptions = {},
          scope;

      spyOn(Study, 'list').and.callFake(function () {});

      scope = createController(counts);
      scope.vm.updateStudies(listOptions);
      scope.$digest();

      expect(Study.list).toHaveBeenCalledWith(listOptions);
    });


  });

});
