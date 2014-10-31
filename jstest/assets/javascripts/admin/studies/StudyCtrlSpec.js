// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudyCtrl', function() {
    var windowService, stateService, scope, timeout;
    var study = {name: 'ST1'};

    beforeEach(mocks.module('biobankApp', function($provide) {
      windowService = {
        localStorage: {
          setItem: function() {},
          getItem: function() { return {}; }
        }
      };

      spyOn(windowService.localStorage, 'setItem');
      $provide.value('$window', windowService);

      stateService = {
        current: {
          name: 'admin.studies.study.processing'
        }
      };

      $provide.value('$state', stateService);
    }));

    beforeEach(inject(function($controller, $rootScope, $window, $state, $timeout) {
      timeout = $timeout;
      scope = $rootScope.$new();

      $controller('StudyCtrl as vm', {
        $window:  $window,
        $scope:   scope,
        $state:   $state,
        $timeout: $timeout,
        study:    study
      });
      scope.$digest();
    }));

    it('should contain a valid study', function() {
      expect(scope.vm.study).toBe(study);
    });

    it('should contain initialized panels', function() {
      expect(scope.vm.tabSummaryActive).toBe(false);
      expect(scope.vm.tabParticipantsActive).toBe(false);
      expect(scope.vm.tabSpecimensActive).toBe(false);
      expect(scope.vm.tabCollectionActive).toBe(false);
      expect(scope.vm.tabProcessingActive).toBe(false);
    });

    it('should contain initialized local storage', function() {
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.collectionEventTypes', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.participantAnnotationTypes', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.participantAnnottionTypes', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.processingTypes', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenGroups', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkAnnotationTypes', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkTypes', true);
    });

    it('should initialize the tab of the current state', function() {
      timeout.flush();
      expect(scope.vm.tabProcessingActive).toBe(true);
    });

  });

});
