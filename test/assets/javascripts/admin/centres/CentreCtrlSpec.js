/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test', function($provide) {
      $provide.value('$window', {
        localStorage: {
          setItem: jasmine.createSpy('mockWindowService.setItem'),
          getItem: jasmine.createSpy('mockWindowService.getItem')
        }
      });
    }));

    beforeEach(inject(function($rootScope, $controller, $window, $timeout) {
      var self = this;

      self.$window          = self.$injector.get('$window');
      self.Centre           = self.$injector.get('Centre');
      self.jsonEntities     = self.$injector.get('jsonEntities');

      self.createController = createController;
      self.centre = new self.Centre(self.jsonEntities.centre());

      function createController(centre) {
        var state = {
          params: {centreId: centre.id},
          current: {name: 'home.admin.centres.centre.locations'}
        };

        self.scope = $rootScope.$new();

        $controller('CentreCtrl as vm', {
          $window:  $window,
          $scope:   self.scope,
          $state:   state,
          $timeout: $timeout,
          centre:   centre
        });
        self.scope.$digest();
      }
    }));

    it('should contain a valid centre', function() {
      this.createController(this.centre);
      expect(this.scope.vm.centre).toBe(this.centre);
    });

    it('should contain initialized panels', function() {
      this.createController(this.centre);
      expect(this.scope.vm.tabSummaryActive).toBe(false);
      expect(this.scope.vm.tabLocationsActive).toBe(false);
      expect(this.scope.vm.tabStudiesActive).toBe(false);
    });

    it('should contain initialized local storage', function() {
      this.createController(this.centre);
      expect(this.$window.localStorage.setItem)
        .toHaveBeenCalledWith('centre.panel.locations', true);
    });

    it('should initialize the tab of the current state', function() {
      var $timeout = this.$injector.get('$timeout');

      this.createController(this.centre);
      $timeout.flush();
      expect(this.scope.vm.tabLocationsActive).toBe(true);
    });

  });

});
