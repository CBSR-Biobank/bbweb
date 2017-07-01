/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('biobankHeaderDirective', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<biobank-header></biobank-header>',
          undefined,
          'biobankHeader');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'User',
                              'usersService',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/home/components/biobankHeader/biobankHeader.html');
    }));

    it('should have valid scope', function() {
      this.createController();
      this.scope.$digest();
      expect(this.controller.logout).toBeFunction();
    });

    it('update user on login', function() {
      var jsonUser = this.factory.user();

      this.createController();
      expect(this.controller.user).toBeUndefined();

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
