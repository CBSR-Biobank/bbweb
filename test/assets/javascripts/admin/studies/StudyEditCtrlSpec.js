/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: StudyEditCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_Study_, jsonEntities) {
      var self = this;

      self.Study        = self.$injector.get('Study');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.study = new this.Study();
      self.titleContains = 'Add';
      self.returnState = {
        name: 'home.admin.studies',
        params: {}
      };

      self.createController = setupController();

      //--

      function setupController() {
        var $rootScope           = self.$injector.get('$rootScope'),
            $controller          = self.$injector.get('$controller'),
            $state               = self.$injector.get('$state'),
            notificationsService = self.$injector.get('notificationsService'),
            domainEntityService  = self.$injector.get('domainEntityService');

        return create;

        //--

        function create(study) {
          self.scope = $rootScope.$new();
          $controller('StudyEditCtrl as vm', {
            $scope:               self.scope,
            $state:               $state,
            notificationsService: notificationsService,
            domainEntityService:  domainEntityService,
            study:                study
          });
          self.scope.$digest();
        }
      }
    }));

   it('should contain valid settings to update a study', function() {
      this.createController(this.study);
      expect(this.scope.vm.study).toEqual(this.study);
      expect(this.scope.vm.returnState.name).toBe(this.returnState.name);
      expect(this.scope.vm.returnState.params).toEqual(this.returnState.params);
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      this.createController(this.study);
      spyOn($state, 'go').and.callFake(function () {} );
      this.scope.vm.cancel();
      expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                             this.returnState.params,
                                             { reload: false });
    });

    it('should return to valid state on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService');

      this.createController(this.study);

      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.scope.vm.submit(this.study);
      this.scope.$digest();
      expect(domainEntityService.updateErrorModal)
        .toHaveBeenCalledWith('err', 'study');
    });


    it('should return to valid state on submit', function() {
      var $q     = this.$injector.get('$q'),
          $state = this.$injector.get('$state');

      this.createController(this.study);
      spyOn($state, 'go').and.callFake(function () {} );
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        return $q.when('test');
      });

      this.scope.vm.submit(this.study);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                             this.returnState.params,
                                             { reload: true });
    });

  });

});
