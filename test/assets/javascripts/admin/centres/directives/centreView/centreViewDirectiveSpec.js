/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: centreViewDirective', function() {

    var createController = function (centre) {
      this.element = angular.element('<centre-view centre="vm.centre"></centre-view>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { centre: centre };

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centreView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($window, $timeout, TestSuiteMixin) {
      var self = this;

      $window.localStorage.setItem = jasmine.createSpy().and.returnValue(null);
      $window.localStorage.getItem = jasmine.createSpy().and.returnValue(null);

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$window',
                              '$state',
                              'Centre',
                              'factory');

      self.centre = new self.Centre(self.factory.centre());

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreView/centreView.html');
    }));

    it('should contain a valid centre', function() {
      createController.call(this, this.centre);
      expect(this.scope.vm.centre).toBe(this.centre);
    });

    it('should contain initialized panels', function() {
      createController.call(this, this.centre);
      expect(this.controller.tabs).toBeArrayOfSize(3);
    });

    it('should contain initialized local storage', function() {
      createController.call(this, this.centre);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('centre.panel.locations', true);
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
        createController.call(self, self.centre);
        childScope = self.element.isolateScope().$new();
        childScope.$emit('tabbed-page-update');
        self.scope.$digest();
        tab = _.find(self.controller.tabs, { sref: state });
        expect(tab.active).toBeTrue();
      });
    });

  });

});
