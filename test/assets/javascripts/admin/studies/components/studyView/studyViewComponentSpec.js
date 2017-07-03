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

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<study-view study="vm.study"></study-view>',
          { study: this.study },
          'studyView');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($window, ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      $window.localStorage.setItem = jasmine.createSpy().and.returnValue(null);
      $window.localStorage.getItem = jasmine.createSpy().and.returnValue(null);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$window',
                              '$state',
                              'Study',
                              'factory');

      this.study = new this.Study(this.factory.study());

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/studyView/studyView.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
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
        self.createController();
        childScope = self.element.isolateScope().$new();
        childScope.$emit('tabbed-page-update');
        self.scope.$digest();
        tab = _.find(self.controller.tabs, { sref: state });
        expect(tab.active).toBeTrue();
      });
    });

  });

});
