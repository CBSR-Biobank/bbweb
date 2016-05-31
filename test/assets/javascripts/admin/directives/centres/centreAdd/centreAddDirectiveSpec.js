/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: eAddDirective()', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, $state, templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.Centre               = self.$injector.get('Centre');
      self.factory         = self.$injector.get('factory');
      self.notificationsService = self.$injector.get('notificationsService');
      self.domainEntityService  = self.$injector.get('domainEntityService');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreAdd/centreAdd.html');

      self.centre = new self.Centre();
      self.returnState = {
        name: 'home.admin.centres',
        params: {}
      };

      self.createController = createController;

      //--

      function createController(centre) {
        self.element = angular.element('<centre-add centre="vm.centre"></centre-add>');
        self.scope = $rootScope.$new();
        self.scope.vm = { centre: centre };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('centreAdd');
      }
    }));

    it('scope should be valid', function() {
      this.createController(this.centre);
      expect(this.scope.vm.centre).toEqual(this.centre);
      expect(this.controller.returnState.name).toBe(this.returnState.name);
      expect(this.controller.returnState.params).toEqual(this.returnState.params);
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      this.createController(this.centre);
      spyOn($state, 'go').and.callFake(function () {} );
      this.controller.cancel();
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

      this.controller.submit(this.centre);
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

      this.controller.submit(this.centre);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                             this.returnState.params,
                                             { reload: true });
    });

  });

});
