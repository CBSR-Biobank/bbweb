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
    var createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function() {
      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope           = injector.get('$rootScope'),
          $controller          = injector.get('$controller');

      return create;

      //--

      function create(stateParams) {
        var scope = $rootScope.$new();

        $controller('PasswordSentCtrl as vm', {
          $scope:       scope,
          $stateParams: stateParams
        });
        scope.$digest();
        return scope;
      }
    }

    it('has valid scope', function() {
      var email = faker.internet.email(),
          scope = createController({ email: email });

      expect(scope.vm.email).toEqual(email);
    });

  });

});
