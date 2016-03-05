/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Directive: studyAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, testUtils) {
      var self = this;

      self.Study        = self.$injector.get('Study');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.study = new this.Study();
      self.titleContains = 'Add';
      self.returnState = 'home.admin.studies';

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/studyAdd/studyAdd.html');

      self.element = angular.element('<study-add study="vm.study"></study-add>');
      self.scope = $rootScope.$new();
      self.scope.vm = { study: self.study };

      $compile(self.element)(self.scope);
      self.scope.$digest();
      self.controller = self.element.controller('studyAdd');
    }));

    it('should contain valid settings to update a study', function() {
      expect(this.controller.study).toEqual(this.study);
      expect(this.controller.returnState).toBe(this.returnState);
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      spyOn($state, 'go').and.callFake(function () {} );
      this.controller.cancel();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState);
    });

    it('should return to valid state on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService');

      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.controller.submit(this.study);
      this.scope.$digest();
      expect(domainEntityService.updateErrorModal)
        .toHaveBeenCalledWith('err', 'study');
    });

    it('should return to valid state on submit', function() {
      var $q     = this.$injector.get('$q'),
          $state = this.$injector.get('$state');

      spyOn($state, 'go').and.callFake(function () {} );
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        return $q.when('test');
      });

      this.controller.submit(this.study);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState, {}, { reload: true });
    });

  });

});
