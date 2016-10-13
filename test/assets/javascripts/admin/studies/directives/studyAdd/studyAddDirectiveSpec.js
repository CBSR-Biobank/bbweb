/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: studyAddDirective', function() {

    var createController = function () {
      this.element = angular.element('<study-add study="vm.study"></study-add>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { study: this.study };

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('studyAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (TestSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              'Study',
                              'factory');

      self.study = new this.Study();
      self.titleContains = 'Add';
      self.returnState = 'home.admin.studies';

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/studyAdd/studyAdd.html');
    }));

    it('should contain valid settings to update a study', function() {
      createController.call(this);
      expect(this.controller.study).toEqual(this.study);
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      spyOn($state, 'go').and.callFake(function () {} );
      createController.call(this);
      this.controller.cancel();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState);
    });

    it('should return to valid state on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainNotificationService = this.$injector.get('domainNotificationService');

      spyOn(domainNotificationService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      createController.call(this);
      this.controller.submit(this.study);
      this.scope.$digest();
      expect(domainNotificationService.updateErrorModal)
        .toHaveBeenCalledWith('err', 'study');
    });

    it('should return to valid state on submit', function() {
      var $q     = this.$injector.get('$q'),
          $state = this.$injector.get('$state');

      spyOn($state, 'go').and.callFake(function () {} );
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        return $q.when('test');
      });

      createController.call(this);
      this.controller.submit(this.study);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState, {}, { reload: true });
    });

  });

});
