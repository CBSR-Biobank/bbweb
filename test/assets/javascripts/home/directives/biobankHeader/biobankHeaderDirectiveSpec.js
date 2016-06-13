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

    var createController = function () {
      this.element = angular.element('<biobank-header></biobank-header>');
      this.scope = this.$rootScope.$new();

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('biobankHeader');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'User',
                              'usersService',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/home/directives/biobankHeader/biobankHeader.html',
        '/assets/javascripts/common/directives/uiBreadcrumbs.tpl.html');
    }));

    it('should have valid scope', function() {
      createController.call(this);
      this.scope.$digest();
      expect(this.controller.logout).toBeFunction();
    });

    it('update user on login', function() {
      var jsonUser = this.factory.user();

      createController.call(this);
      expect(this.controller.user).toBeUndefined();

      spyOn(this.usersService, 'getCurrentUser').and.returnValue(jsonUser);
      this.scope.$digest();
      expect(this.controller.user).toEqual(jsonUser);
    });

    it('changes to correct state on logout', function() {
      spyOn(this.usersService, 'logout').and.returnValue(this.$q.when(true));
      spyOn(this.$state, 'go').and.returnValue(true);

      createController.call(this);
      this.controller.logout();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home', {}, { reload: true});
    });

    it('changes to correct state on logout failure', function() {
      var deferred = this.$q.defer();

      spyOn(this.usersService, 'logout').and.returnValue(deferred.promise);
      spyOn(this.$state, 'go').and.returnValue(true);
      deferred.reject('simulated logout failure');

      createController.call(this);
      this.controller.logout();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home', {}, { reload: true});
    });


  });

});
