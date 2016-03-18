/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreEditCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $controller, $state) {
      var self = this;

      self.Centre               = self.$injector.get('Centre');
      self.jsonEntities         = self.$injector.get('jsonEntities');
      self.notificationsService = self.$injector.get('notificationsService');
      self.domainEntityService  = self.$injector.get('domainEntityService');

      self.centre = new self.Centre();
      self.returnState = {
        name: 'home.admin.centres',
        params: {}
      };

      self.createController = createController;

      //--

      function createController(centre) {
        self.scope = $rootScope.$new();
        $controller('CentreEditCtrl as vm', {
          $scope:               self.scope,
          $state:               $state,
          notificationsService: self.notificationsService,
          domainEntityService:  self.domainEntityService,
          centre:               self.centre
        });
        self.scope.$digest();
      }
    }));

    it('scope should be valid', function() {
      this.createController(this.centre);
      expect(this.scope.vm.centre).toEqual(this.centre);
      expect(this.scope.vm.returnState.name).toBe(this.returnState.name);
      expect(this.scope.vm.returnState.params).toEqual(this.returnState.params);
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      this.createController(this.centre);
      spyOn($state, 'go').and.callFake(function () {} );
      this.scope.vm.cancel();
      expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                             this.returnState.params,
                                             { reload: false });
    });

    it('should return to valid state on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService');

      this.createController(this.centre);
      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Centre.prototype, 'add').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.scope.vm.submit(this.centre);
      this.scope.$digest();
      expect(domainEntityService.updateErrorModal).toHaveBeenCalledWith('err', 'centre');
    });


    it('should return to valid state on submit', function() {
      var $q     = this.$injector.get('$q'),
          $state = this.$injector.get('$state');

      this.createController(this.centre);

      spyOn($state, 'go').and.callFake(function () {} );
      spyOn(this.Centre.prototype, 'add').and.callFake(function () {
        return $q.when('test');
      });

      this.scope.vm.submit(this.centre);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                             this.returnState.params,
                                             { reload: true });
    });

  });

});
