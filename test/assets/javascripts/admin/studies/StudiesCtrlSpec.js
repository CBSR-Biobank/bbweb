/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
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
    var StudyCounts, fakeEntities, createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_StudyCounts_,
                               fakeDomainEntities) {
      StudyCounts = _StudyCounts_;
      fakeEntities = fakeDomainEntities;
      createController = setupController(this.$injector);
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

    function setupController(injector) {
      var $rootScope = injector.get('$rootScope'),
          $controller = injector.get('$controller'),
          Study = injector.get('Study'),
          StudyStatus = injector.get('StudyStatus');

      return create;

      //---

      function create(studyCounts) {
        var scope = $rootScope.$new();

        $controller('StudiesCtrl as vm', {
          $scope:      scope,
          Study:       Study,
          StudyStatus: StudyStatus,
          studyCounts: studyCounts
        });

        scope.$digest();
        return scope;
      }
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
