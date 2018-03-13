/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: centreView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Factory');

      this.centre = new this.Centre(this.Factory.centre());

      this.createController = (centre) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<centre-view centre="vm.centre"></centre-view>',
          { centre: centre },
          'centreView');

    });
  });

  it('should contain a valid centre', function() {
    this.createController(this.centre);
    expect(this.scope.vm.centre).toBe(this.centre);
  });

  it('should contain initialized panels', function() {
    this.createController(this.centre);
    expect(this.controller.tabs).toBeArrayOfSize(3);
  });

  it('should initialize the tab corresponding to the event that was emitted', function() {
    var self = this,
        tab,
        childScope,
        states = [
          'home.admin.centres.centre.summary',
          'home.admin.centres.centre.studies',
          'home.admin.centres.centre.locations',
        ];

    _(states).forEach(function (state) {
      self.$state.current.name = state;
      self.createController(self.centre);
      childScope = self.element.isolateScope().$new();
      childScope.$emit('tabbed-page-update');
      self.scope.$digest();
      tab = _.find(self.controller.tabs, { sref: state });
      expect(tab.active).toBeTrue();
    });
  });

});
