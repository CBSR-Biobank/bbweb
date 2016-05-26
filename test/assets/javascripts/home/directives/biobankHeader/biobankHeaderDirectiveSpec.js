/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('biobankHeaderDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin, testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q           = self.$injector.get('$q');
      self.$state       = self.$injector.get('$state');
      self.User         = self.$injector.get('User');
      self.usersService = self.$injector.get('usersService');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/home/directives/biobankHeader/biobankHeader.html',
        '/assets/javascripts/common/directives/uiBreadcrumbs.tpl.html');

      self.createController = createController;

      ///--

      function createController() {
        self.element = angular.element('<biobank-header></biobank-header>');
        self.scope = $rootScope.$new();

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('biobankHeader');
      }
    }));

    it('should have valid scope', function() {
      this.createController();
      expect(this.controller.user).toBeNull();
      expect(this.controller.logout).toBeFunction();
    });

    it('update user on login', function() {
      var jsonUser = this.jsonEntities.user();

      this.createController();
      expect(this.controller.user).toBeNull();

      spyOn(this.usersService, 'getCurrentUser').and.returnValue(jsonUser);
      this.scope.$digest();
      expect(this.controller.user).toEqual(jsonUser);
    });

    it('changes to correct state on logout', function() {
      spyOn(this.usersService, 'logout').and.returnValue(this.$q.when(true));
      spyOn(this.$state, 'go').and.returnValue(true);

      this.createController();
      this.controller.logout();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home', {}, { reload: true});
    });

    it('changes to correct state on logout failure', function() {
      var deferred = this.$q.defer();

      spyOn(this.usersService, 'logout').and.returnValue(deferred.promise);
      spyOn(this.$state, 'go').and.returnValue(true);
      deferred.reject('simulated logout failure');

      this.createController();
      this.controller.logout();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home', {}, { reload: true});
    });


  });

});
