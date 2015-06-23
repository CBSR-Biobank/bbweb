/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: StudyCtrl', function() {
    var windowService, Study, createController, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test', function($provide) {
      windowService = {
        localStorage: {
          setItem: function() {},
          getItem: function() { return {}; }
        }
      };

      spyOn(windowService.localStorage, 'setItem');
      $provide.value('$window', windowService);
    }));

    beforeEach(inject(function($q, _Study_, fakeDomainEntities) {
      Study = _Study_;
      fakeEntities = fakeDomainEntities;
      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope  = injector.get('$rootScope'),
          $controller = injector.get('$controller'),
          $window     = injector.get('$window'),
          $timeout    = injector.get('$timeout');

      return create;

      //--

      function create(study) {
        var scope = $rootScope.$new(),
            state = {
              params: {studyId: study.id},
              current: {name: 'home.admin.studies.study.processing'}
            };

        $controller('StudyCtrl as vm', {
          $window:  $window,
          $scope:   scope,
          $state:   state,
          $timeout: $timeout,
          study:    study
        });
        scope.$digest();
        return scope;
      }
    }

    it('should contain a valid study', function() {
      var study = new Study(fakeEntities.study()),
          scope = createController(study);

      expect(scope.vm.study).toBe(study);
    });

    it('should contain initialized panels', function() {
      var study = new Study(fakeEntities.study()),
          scope = createController(study);

      expect(scope.vm.tabSummaryActive).toBe(false);
      expect(scope.vm.tabParticipantsActive).toBe(false);
      expect(scope.vm.tabSpecimensActive).toBe(false);
      expect(scope.vm.tabCollectionActive).toBe(false);
      expect(scope.vm.tabProcessingActive).toBe(false);
    });

    it('should contain initialized local storage', function() {
      var study = new Study(fakeEntities.study());

      createController(study);

      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.collectionEventTypes', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.participantAnnotationTypes', true);
      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.participantAnnotationTypes', true);
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
      var $timeout = this.$injector.get('$timeout'),
          study = new Study(fakeEntities.study()),
          scope = createController(study);

      $timeout.flush();
      expect(scope.vm.tabProcessingActive).toBe(true);
    });

  });

});
