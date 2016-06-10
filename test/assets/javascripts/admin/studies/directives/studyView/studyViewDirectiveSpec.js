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

    beforeEach(inject(function($rootScope, $compile, $state, templateMixin, testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.$window      = self.$injector.get('$window');
      self.$state       = self.$injector.get('$state');
      self.Study        = self.$injector.get('Study');
      self.factory = self.$injector.get('factory');

      self.study = new self.Study(self.factory.study());

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/studyView/studyView.html');

      self.createController = createController;

      //--

      function createController() {
        self.element = angular.element('<study-view study="vm.study"></study-view>');
        self.scope = $rootScope.$new();
        self.scope.vm = { study: self.study };

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('studyView');
      }
    }));

    it('should contain a valid study', function() {
      this.createController();
      expect(this.controller.study).toBe(this.study);
    });

    it('should contain initialized tabs', function() {
      this.createController();
      expect(this.controller.tabs).toBeArrayOfSize(4);
    });

    it('should contain initialized local storage', function() {
      this.createController();

      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.processingTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkAnnotationTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkTypes', true);
    });

    it('should initialize the tab of the current state', function() {
      var tab;

      this.$state.current.name = 'home.admin.studies.study.processing';
      this.createController();

      tab = _.findWhere(this.controller.tabs, { heading: 'Processing' });

      expect(tab.active).toBe(true);
    });

  });

});
