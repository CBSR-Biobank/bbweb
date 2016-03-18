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
  'faker',
  'biobankApp'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('Controller: PasswordSentCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $controller) {
      var self = this;

      self.createController = createController;

      function createController(stateParams) {
          self.scope = $rootScope.$new();

          $controller('PasswordSentCtrl as vm', {
            $scope:       self.scope,
            $stateParams: stateParams
          });
          self.scope.$digest();
      }
    }));

    it('has valid scope', function() {
      var email = faker.internet.email();

      this.createController({ email: email });
      expect(this.scope.vm.email).toEqual(email);
    });

  });

});
