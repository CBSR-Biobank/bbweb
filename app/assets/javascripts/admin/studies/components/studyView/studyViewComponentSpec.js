/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Directive: studyViewDirective', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function($window, ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);

      $window.localStorage.setItem = jasmine.createSpy().and.returnValue(null);
      $window.localStorage.getItem = jasmine.createSpy().and.returnValue(null);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$window',
                              '$state',
                              'Study',
                              'factory');

      this.study = new this.Study(this.factory.study());

      this.createController = () =>
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<study-view study="vm.study"></study-view>',
          { study: this.study },
          'studyView');
    });
  });

  it('should contain a valid study', function() {
    this.createController();
    expect(this.controller.study).toBe(this.study);
  });

  it('should contain initialized tabs', function() {
    this.createController();
    expect(this.controller.tabs).toBeArrayOfSize(4);
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
