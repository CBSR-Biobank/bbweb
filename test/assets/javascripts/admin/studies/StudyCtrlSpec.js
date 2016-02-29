/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: StudyCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test', function($provide) {
      $provide.value('$window', {
        localStorage: {
          setItem: jasmine.createSpy('mockWindowService.setItem'),
          getItem: jasmine.createSpy('mockWindowService.getItem')
        }
      });
    }));

    beforeEach(inject(function($q, _Study_, jsonEntities) {
      var self = this;

      self.$window      = self.$injector.get('$window');
      self.Study        = self.$injector.get('Study');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.createController = setupController();
      self.study = new self.Study(self.jsonEntities.study());

      //--

      function setupController() {
        var $rootScope  = self.$injector.get('$rootScope'),
            $controller = self.$injector.get('$controller'),
            $window     = self.$injector.get('$window'),
            $timeout    = self.$injector.get('$timeout');

        return create;

        //--

        function create(study) {
          var state = {
            params:  { studyId: study.id },
            current: { name: 'home.admin.studies.study.processing' }
          };

          self.scope = $rootScope.$new();
          $controller('StudyCtrl as vm',
                      {
                        $window:  $window,
                        $scope:   self.scope,
                        $state:   state,
                        $timeout: $timeout,
                        study:    study
                      });
          self.scope.$digest();
        }
      }
    }));

    it('should contain a valid study', function() {
      this.createController(this.study);
      expect(this.scope.vm.study).toBe(this.study);
    });

    it('should contain initialized panels', function() {
      this.createController(this.study);
      expect(this.scope.vm.tabSummaryActive).toBe(false);
      expect(this.scope.vm.tabParticipantsActive).toBe(false);
      expect(this.scope.vm.tabSpecimensActive).toBe(false);
      expect(this.scope.vm.tabCollectionActive).toBe(false);
      expect(this.scope.vm.tabProcessingActive).toBe(false);
    });

    it('should contain initialized local storage', function() {
      this.createController(this.study);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.collectionEventTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.participantAnnotationTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.participantAnnotationTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.processingTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenGroups', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkAnnotationTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkTypes', true);
    });

    it('should initialize the tab of the current state', function() {
      var $timeout = this.$injector.get('$timeout');

      this.createController(this.study);
      $timeout.flush();
      expect(this.scope.vm.tabProcessingActive).toBe(true);
    });

  });

});
