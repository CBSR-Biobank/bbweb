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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: AdminCtrl', function() {
    var createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function() {
      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope  = injector.get('$rootScope'),
          $controller = injector.get('$controller');

      return create;

      //--

      function create(counts) {
        var scope = $rootScope.$new();

        $controller('AdminCtrl as vm', {
          $scope: scope,
          aggregateCounts: counts
        });
        scope.$digest();
        return scope;
      }
    }

    it('has valid scope', function() {
      var counts = { studies: 1,
                     centres: 2,
                     users: 3
                   },
          scope = createController(counts);

      expect(scope.vm.counts).toEqual(counts);
    });

  });

});
