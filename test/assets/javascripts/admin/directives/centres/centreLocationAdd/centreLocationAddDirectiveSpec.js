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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: centreLocationAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, $state, directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$state               = self.$injector.get('$state');
      self.Centre               = self.$injector.get('Centre');
      self.Location             = self.$injector.get('Location');
      self.jsonEntities         = self.$injector.get('jsonEntities');
      self.domainEntityService  = self.$injector.get('domainEntityService');
      self.notificationsService = self.$injector.get('notificationsService');

      self.centre = new self.Centre(self.jsonEntities.centre());
      self.location = new self.Location(self.jsonEntities.location());

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreLocationAdd/centreLocationAdd.html',
        '/assets/javascripts/admin/directives/locationAdd/locationAdd.html');

      self.returnStateName = 'home.admin.centres.centre.locations';
      self.createController = createController;

      //--

      function createController(centre) {
        self.element = angular.element(
          '<centre-location-add centre="vm.centre"></centre-location-add>');
        self.scope = $rootScope.$new();
        self.scope.vm = { centre: centre };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('centreLocationAdd');
      }

    }));

    it('scope should be valid', function() {
      this.createController(this.centre);
      expect(this.controller.centre).toBe(this.centre);
      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();
    });

    it('should return to valid state on cancel', function() {
      this.createController(this.centre);
      spyOn(this.$state, 'go').and.callFake(function () {} );
      this.controller.cancel();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName);
    });

    it('should display failure information on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService');

      this.createController(this.centre);

      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Centre.prototype, 'addLocation').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.controller.submit(this.location);
      this.scope.$digest();
      expect(domainEntityService.updateErrorModal)
        .toHaveBeenCalledWith('err', 'location');
    });


    it('should return to valid state on submit', function() {
      var $q = this.$injector.get('$q');

      this.createController(this.centre);
      spyOn(this.$state, 'go').and.callFake(function () {} );
      spyOn(this.Centre.prototype, 'addLocation').and.callFake(function () {
        return $q.when('test');
      });

      this.controller.submit(this.location);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName, {}, { reload: true });
    });

  });

});
