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

  describe('Directive: centreAddDirective()', function() {

    var createController = function (centre) {
      this.element = angular.element('<centre-add centre="vm.centre"></centre-add>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { centre: centre };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centreAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($state, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              'Centre',
                              'factory',
                              'notificationsService',
                              'domainEntityService');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreAdd/centreAdd.html');

      self.centre = new self.Centre();
      self.returnState = {
        name: 'home.admin.centres',
        params: {}
      };
    }));

    it('scope should be valid', function() {
      createController.call(this, this.centre);
      expect(this.scope.vm.centre).toEqual(this.centre);
      expect(this.controller.returnState.name).toBe(this.returnState.name);
      expect(this.controller.returnState.params).toEqual(this.returnState.params);
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      createController.call(this, this.centre);
      spyOn($state, 'go').and.callFake(function () {} );
      this.controller.cancel();
      expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                             this.returnState.params,
                                             { reload: false });
    });

    it('should return to valid state on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService');

      createController.call(this, this.centre);
      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Centre.prototype, 'add').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.controller.submit(this.centre);
      this.scope.$digest();
      expect(domainEntityService.updateErrorModal).toHaveBeenCalledWith('err', 'centre');
    });


    it('should return to valid state on submit', function() {
      var $q     = this.$injector.get('$q'),
          $state = this.$injector.get('$state');

      createController.call(this, this.centre);

      spyOn($state, 'go').and.callFake(function () {} );
      spyOn(this.Centre.prototype, 'add').and.callFake(function () {
        return $q.when('test');
      });

      this.controller.submit(this.centre);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                             this.returnState.params,
                                             { reload: true });
    });

  });

});
