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

  describe('Controller: CentresCtrl', function() {
    var CentreCounts, fakeEntities, createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_CentreCounts_,
                               fakeDomainEntities) {
      CentreCounts = _CentreCounts_;
      fakeEntities = fakeDomainEntities;
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();
    }));

    function createCentreCounts(disabled, enabled, retired) {
      return new CentreCounts({
        total:    disabled + enabled + retired,
        disabled: disabled,
        enabled:  enabled,
        retired:  retired
      });
    }

    function setupController(injector) {
      var $rootScope = injector.get('$rootScope'),
          $controller = injector.get('$controller'),
          Centre = injector.get('Centre'),
          CentreStatus = injector.get('CentreStatus');

      return create;

      //---

      function create(centreCounts) {
        var scope = $rootScope.$new();

        $controller('CentresCtrl as vm', {
          $scope:      scope,
          Centre:       Centre,
          CentreStatus: CentreStatus,
          centreCounts: centreCounts
        });

        scope.$digest();
        return scope;
      }
    }

    it('scope is valid on startup', function() {
      var CentreStatus = this.$injector.get('CentreStatus'),
          allStatuses = CentreStatus.values().concat('All'),
          counts = createCentreCounts(1, 2, 3),
          scope = createController(counts);

      expect(scope.vm.centreCounts).toEqual(counts);
      expect(scope.vm.pageSize).toBeDefined();

      _.each(allStatuses, function(status) {
        expect(scope.vm.possibleStatuses).toContain({ id: status.toLowerCase(), label: status});
      });
    });

    it('updateCentres retrieves new list of centres', function() {
      var Centre = this.$injector.get('Centre'),
          counts = createCentreCounts(1, 2, 3),
          listOptions = {},
          scope;

      spyOn(Centre, 'list').and.callFake(function () {});

      scope = createController(counts);
      scope.vm.updateCentres(listOptions);
      scope.$digest();

      expect(Centre.list).toHaveBeenCalledWith(listOptions);
    });


  });

});
