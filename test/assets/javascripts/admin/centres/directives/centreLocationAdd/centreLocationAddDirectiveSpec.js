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

    var createController = function (centre) {
      this.element = angular.element(
        '<centre-location-add centre="vm.centre"></centre-location-add>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { centre: centre };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centreLocationAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($state, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Location',
                              'factory',
                              'domainEntityService',
                              'notificationsService');

      self.centre = new self.Centre(self.factory.centre());
      self.location = new self.Location(self.factory.location());
      self.returnStateName = 'home.admin.centres.centre.locations';

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreLocationAdd/centreLocationAdd.html',
        '/assets/javascripts/admin/directives/locationAdd/locationAdd.html');
    }));

    it('scope should be valid', function() {
      createController.call(this, this.centre);
      expect(this.controller.centre).toBe(this.centre);
      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();
    });

    it('should return to valid state on cancel', function() {
      createController.call(this, this.centre);
      spyOn(this.$state, 'go').and.callFake(function () {} );
      this.controller.cancel();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName);
    });

    it('should display failure information on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService');

      createController.call(this, this.centre);

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

      createController.call(this, this.centre);
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
