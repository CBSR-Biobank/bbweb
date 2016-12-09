/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: studyViewDirective', function() {

    var createController = function () {
      this.element = angular.element('<study-view study="vm.study"></study-view>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { study: this.study };

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('studyView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($window, $state, TestSuiteMixin, testUtils) {
      var self = this;

      $window.localStorage.setItem = jasmine.createSpy().and.returnValue(null);
      $window.localStorage.getItem = jasmine.createSpy().and.returnValue(null);

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$window',
                              '$state',
                              'Study',
                              'factory');

      self.study = new self.Study(self.factory.study());

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/studyView/studyView.html');
    }));

    it('should contain a valid study', function() {
      createController.call(this);
      expect(this.controller.study).toBe(this.study);
    });

    it('should contain initialized tabs', function() {
      createController.call(this);
      expect(this.controller.tabs).toBeArrayOfSize(4);
    });

    it('should contain initialized local storage', function() {
      createController.call(this);

      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.processingTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkAnnotationTypes', true);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('study.panel.specimenLinkTypes', true);
    });

    it('should initialize the tab corresponding to the event that was emitted', function() {
      var self = this,
          tab,
          childScope,
          states = [
            'home.admin.studies.study.summary',
            'home.admin.studies.study.participants',
            'home.admin.studies.study.collection',
            'home.admin.studies.study.processing',
          ];

      _(states).forEach(function (state) {
        self.$state.current.name = state;
        createController.call(self);
        childScope = self.element.isolateScope().$new();
        childScope.$emit('tabbed-page-update');
        self.scope.$digest();
        tab = _.find(self.controller.tabs, { sref: state });
        expect(tab.active).toBeTrue();
      });
    });

  });

});
