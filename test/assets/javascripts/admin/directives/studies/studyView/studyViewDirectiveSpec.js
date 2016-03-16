/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: studyViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'ui.router', 'biobank.test', function($provide) {
      $provide.value('$window', {
        localStorage: {
          setItem: jasmine.createSpy('mockWindowService.setItem'),
          getItem: jasmine.createSpy('mockWindowService.getItem')
        }
      });
    }));

    beforeEach(inject(function($rootScope, $compile, $state, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$window      = self.$injector.get('$window');
      self.$state       = self.$injector.get('$state');
      self.Study        = self.$injector.get('Study');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.study = new self.Study(self.jsonEntities.study());

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/studyView/studyView.html');

      self.element = angular.element('<study-view study="vm.study"></study-view>');
      self.scope = $rootScope.$new();
      self.scope.vm = { study: self.study };

      $compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('studyView');
    }));

    it('should contain a valid study', function() {
      expect(this.controller.study).toBe(this.study);
    });

    it('should contain initialized panels', function() {
      expect(this.controller.tabSummaryActive).toBe(false);
      expect(this.controller.tabParticipantsActive).toBe(false);
      expect(this.controller.tabSpecimensActive).toBe(false);
      expect(this.controller.tabCollectionActive).toBe(false);
      expect(this.controller.tabProcessingActive).toBe(false);
    });

    it('should contain initialized local storage', function() {
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
      this.$state.current.name = 'home.admin.studies.study.processing';
      this.$injector.get('$timeout').flush();
      expect(this.controller.tabProcessingActive).toBe(true);
    });

  });

});
