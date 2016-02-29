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

  describe('Controller: LocationEditCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($state) {
      var self = this;

      self.$state       = self.$injector.get('$state');
      self.Centre       = self.$injector.get('Centre');
      self.Location     = self.$injector.get('Location');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.centre = new self.Centre(self.jsonEntities.centre());
      self.location = new self.Location();

      self.currentState = {
        current: { name: 'home.admin.centres.centre.locationAdd'}
      };

      self.returnState = {
        name: 'home.admin.centres.centre.locations',
        params: {}
      };

      self.createController = setupController();

      //--

      function setupController() {
        var $rootScope           = self.$injector.get('$rootScope'),
            $controller          = self.$injector.get('$controller'),
            domainEntityService  = self.$injector.get('domainEntityService'),
            notificationsService = self.$injector.get('notificationsService');

        return create;

        //--

        function create(location) {
          self.scope = $rootScope.$new();

          $controller('LocationEditCtrl as vm', {
            $scope:               self.scope,
            $state:               $state,
            Location:             self.Location,
            domainEntityService:  domainEntityService,
            notificationsService: notificationsService,
            centre:               self.centre
          });
          self.scope.$digest();
        }
      }

    }));

    it('scope should be valid', function() {
      this.createController(this.location);
      expect(this.scope.vm.centre).toBe(this.centre);
      expect(this.scope.vm.location).toEqual(this.location);
    });

    it('should return to valid state on cancel', function() {
      this.createController(this.location);
      spyOn(this.$state, 'go').and.callFake(function () {} );
      this.scope.vm.cancel();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnState.name,
                                                  this.returnState.params,
                                                  { reload: false });
    });

    it('should display failure information on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService');

      this.createController(this.location);

      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Centre.prototype, 'addLocation').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.scope.vm.submit(this.location);
      this.scope.$digest();
      expect(domainEntityService.updateErrorModal)
        .toHaveBeenCalledWith('err', 'location');
    });


    it('should return to valid state on submit', function() {
      var $q = this.$injector.get('$q');

      this.createController(this.location);
      spyOn(this.$state, 'go').and.callFake(function () {} );
      spyOn(this.Centre.prototype, 'addLocation').and.callFake(function () {
        return $q.when('test');
      });

      this.scope.vm.submit(this.location);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnState.name,
                                                  this.returnState.params,
                                                  { reload: true });
    });

  });

});
