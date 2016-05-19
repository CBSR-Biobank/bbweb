/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: centreViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test', function($provide) {
      $provide.value('$window', {
        localStorage: {
          setItem: jasmine.createSpy('mockWindowService.setItem'),
          getItem: jasmine.createSpy('mockWindowService.getItem')
        }
      });
    }));

    beforeEach(inject(function($rootScope, $compile, $timeout, directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$window          = self.$injector.get('$window');
      self.$state       = self.$injector.get('$state');
      self.Centre           = self.$injector.get('Centre');
      self.jsonEntities     = self.$injector.get('jsonEntities');

      self.centre = new self.Centre(self.jsonEntities.centre());

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreView/centreView.html');
      self.createController = createController;

      //---

      function createController(centre) {
        self.element = angular.element('<centre-view centre="vm.centre"></centre-view>');
        self.scope = $rootScope.$new();
        self.scope.vm = { centre: centre };

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('centreView');
      }
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

    it('should initialize the tab of the current state', function() {
      var tab;

      this.$state.current.name = 'home.admin.centres.centre.studies';
      this.createController(this.centre);

      tab = _.findWhere(this.controller.tabs, { heading: 'Studies' });
      expect(tab.active).toBe(true);
    });

  });

});
