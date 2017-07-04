/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: centreView', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (centre) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<centre-view centre="vm.centre"></centre-view>',
          { centre: centre },
          'centreView');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($window, $timeout, ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      $window.localStorage.setItem = jasmine.createSpy().and.returnValue(null);
      $window.localStorage.getItem = jasmine.createSpy().and.returnValue(null);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$window',
                              '$state',
                              'Centre',
                              'factory');

      this.centre = new this.Centre(this.factory.centre());

      this.putHtmlTemplates(
        '/assets/javascripts/admin/centres/components/centreView/centreView.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
    }));

    it('should contain a valid centre', function() {
      this.createController(this.centre);
      expect(this.scope.vm.centre).toBe(this.centre);
    });

    it('should contain initialized panels', function() {
      this.createController(this.centre);
      expect(this.controller.tabs).toBeArrayOfSize(3);
    });

    it('should contain initialized local storage', function() {
      this.createController(this.centre);
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
        self.createController(self.centre);
        childScope = self.element.isolateScope().$new();
        childScope.$emit('tabbed-page-update');
        self.scope.$digest();
        tab = _.find(self.controller.tabs, { sref: state });
        expect(tab.active).toBeTrue();
      });
    });

  });

});
